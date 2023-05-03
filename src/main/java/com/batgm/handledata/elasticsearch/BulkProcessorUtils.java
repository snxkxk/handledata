package com.batgm.handledata.elasticsearch;

import com.batgm.handledata.service.MongoDbService;
import com.batgm.handledata.service.StudyActionRecordService;
import com.batgm.handledata.utils.es2mysql.ESToMySqlEx;
import com.batgm.handledata.utils.stresstest.ESStressTest;
import com.batgm.handledata.utils.muti.ITask;
import com.batgm.handledata.utils.muti.MultiThreadUtils;
import com.batgm.handledata.utils.muti.ResultBean;
import com.batgm.handledata.utils.mysql2es.*;
import com.batgm.handledata.utils.stresstest.MongDbStressTest;
import com.batgm.handledata.utils.stresstest.MySqlStressTest;
import com.batgm.handledata.utils.stresstest.RedisStressTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * @author yqq
 * @createdate 2020/3/23
 */
public class BulkProcessorUtils {

    private static final int TIME_OUT = 5 * 60 * 1000;

    private static   final Logger logger = LoggerFactory.getLogger(BulkProcessorUtils.class);

    public static RestHighLevelClient getClient(){

        HttpHost httphost=new HttpHost("www.batgm.com", 9200, "http");
        RestClientBuilder builder = RestClient.builder(httphost);
        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setSocketTimeout(20601000).setConnectTimeout(30000);
            }
        });
        RestHighLevelClient client = new RestHighLevelClient(builder);// 初始化

         return client;
    }


    public static BulkProcessor getBulkProcessor(RestHighLevelClient client) {

        BulkProcessor bulkProcessor = null;
        try {

            BulkProcessor.Listener listener = new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId, BulkRequest request) {
                    logger.info("Try to insert data number : " + request.numberOfActions());
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                    logger.info("************** Success insert data number : " + request.numberOfActions() + " , id: "
                            + executionId);
                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                    logger.error("Bulk is unsuccess : " + failure + ", executionId: " + executionId);
                }
            };

            BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer = (request, bulkListener) -> client
                    .bulkAsync(request, RequestOptions.DEFAULT, bulkListener);

            BulkProcessor.Builder builder = BulkProcessor.builder(bulkConsumer, listener);

            // 每添加10000个request，执行一次bulk操作
            builder.setBulkActions(10000);
            // 每达到5M的请求size时，执行一次bulk操作
            builder.setBulkSize(new ByteSizeValue(20L, ByteSizeUnit.MB));
            // 设置并发请求数。默认是1，表示允许执行1个并发请求，积累bulk requests和发送bulk是异步的，其数值表示发送bulk的并发线程数（可以为2、3、...）；若设置为0表示二者同步。
            builder.setConcurrentRequests(10);
            // 每50s执行一次bulk操作
            builder.setFlushInterval(TimeValue.timeValueSeconds(50L));
            // 最大重试次数为3次，启动延迟为100ms。
            builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(100L), 3));
            // 注意点：在这里感觉有点坑，官网样例并没有这一步，而笔者因一时粗心也没注意，在调试时注意看才发现，上面对builder设置的属性没有生效
            bulkProcessor = builder.build();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                bulkProcessor.awaitClose(100L, TimeUnit.SECONDS);
                client.close();
            } catch (Exception e1) {
                logger.error(e1.getMessage());
            }

        }
        return bulkProcessor;
    }



    /**
     * 创建索引
     * @param restClient
     * @param indexName
     */
    public static void createIndex(RestHighLevelClient restClient, String indexName){


        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        try {
            if (restClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
                //已存在索引
                return;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        //CreateIndexRequest 实例， 需要注意包的版本 我这里用的7.6的版本 org.elasticsearch.client.indices;
        CreateIndexRequest request = new CreateIndexRequest(indexName);

        //封装属性 类似于json格式
        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> properties =getIndexPropertiesByTableName(indexName);
         if(null!=properties){
             jsonMap.put("properties", properties);
         }
        //设置分片
        request.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );
        request.mapping(jsonMap);
        //我使用的同步的方式 异步请参考官方文档
        CreateIndexResponse createIndexResponse = null;
        try {
            createIndexResponse = restClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        createIndexResponse.isShardsAcknowledged();
    }


    public static Map<String,Object> getIndexPropertiesByTableName(String tableName){

        if(StringUtils.isBlank(tableName)){
            return null;
        }


        Map<String, Object> properties = new HashMap<>();
        //1.study_action_record
        if("study_action_record".equals(tableName)){
            Map<String, Object> integer = new HashMap<>();
            integer.put("type", "integer");
            Map<String, Object> Long = new HashMap<>();
            Long.put("type", "long");
            Map<String, Object> keyword = new HashMap<>();
            keyword.put("type", "keyword");
            Map<String, Object> date = new HashMap<>();
            date.put("type", "date");
            date.put("format","yyyy-MM-dd HH:mm:ss");
            properties.put("id", integer);
            properties.put("project_id", integer);
            properties.put("study_circle_id", integer);
            properties.put("user_id", integer);
            properties.put("subject_table_id", integer);
            properties.put("father_table_id", integer);
            properties.put("study_type", keyword);
            properties.put("login_palce", keyword);
            properties.put("video_time", integer);
            properties.put("study_time", integer);
            properties.put("action", keyword);
            properties.put("device_type", keyword);
            properties.put("ip_address", keyword);
            properties.put("create_time", date);
            properties.put("study_plan_id", integer);
            properties.put("course_code", keyword);
            properties.put("detail_table_id", Long);
            properties.put("update_time", date);
            return properties;
        }
        //2.course_study_time
        if("course_study_time".equals(tableName)){
            Map<String, Object> integer = new HashMap<>();
            integer.put("type", "integer");
            Map<String, Object> keyword = new HashMap<>();
            keyword.put("type", "keyword");
            Map<String, Object> date = new HashMap<>();
            date.put("type", "date");
            date.put("format","yyyy-MM-dd HH:mm:ss");
            properties.put("id", integer);
            properties.put("user_id", integer);
            properties.put("project_id", integer);
            properties.put("study_plan_id", integer);
            properties.put("course_code", keyword);
            properties.put("study_time", integer);
            properties.put("is_complete", integer);
            properties.put("create_time", date);
            properties.put("update_time", date);
            return properties;
        }
        //3.job&jobComments
        if("job".equals(tableName)){
            Map<String, Object> integer = new HashMap<>();
            integer.put("type", "integer");
            Map<String, Object> Long = new HashMap<>();
            Long.put("type", "long");
            Map<String, Object> text = new HashMap<>();
            text.put("type", "text");
            Map<String, Object> keyword = new HashMap<>();
            keyword.put("type", "keyword");
            Map<String, Object> date = new HashMap<>();
            date.put("type", "date");
            date.put("format","yyyy-MM-dd HH:mm:ss");
            Map<String, Object> nested = new HashMap<>();
            nested.put("type", "nested");
            Map<String, Object> jobcommentsproperties = new HashMap<>();
            nested.put("properties",jobcommentsproperties);

            //job properties
            properties.put("study_circle_id", Long);
            properties.put("appeal_content", text);
            properties.put("circle_id", Long);
            properties.put("content", text);
            properties.put("createtime", date);
            properties.put("id", Long);
            properties.put("is_appeal", integer);
            properties.put("is_great", integer);
            properties.put("jobComments", nested);
             // jobcomments
            jobcommentsproperties.put("comment",text);
            jobcommentsproperties.put("comment_time",date);
            jobcommentsproperties.put("id",Long);
            jobcommentsproperties.put("job_id",Long);
            jobcommentsproperties.put("user_id",Long);

            properties.put("jobCommentsCount", integer);
            properties.put("job_submit_id", Long);
            properties.put("name", keyword);
            properties.put("project_id", integer);
            properties.put("review_type", integer);
            properties.put("status", text);
            //properties.put("study_plan_id", Long);
            properties.put("title", text);
            properties.put("user_id", Long);
            return properties;
        }
        return null;
    }

    /**
     * 查看指定索引下所有数据
     * @param restClient
     * @param index
     * @return
     */
    public static   List<Map<String, Object>>  getAllRowsByScroll(RestHighLevelClient restClient, String index) {
        List<Map<String, Object>> collect = new ArrayList<>();
        final Scroll scroll = new Scroll(TimeValue.timeValueSeconds(60));
        SearchResponse resp = null;
        SearchRequest search = new SearchRequest(index);
        search.scroll(scroll);
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.size(100);
            sourceBuilder.sort("_id", SortOrder.ASC);
            search.source(sourceBuilder);
            resp = restClient.search(search, RequestOptions.DEFAULT);
            assert resp != null;
            String scrollId;
            int count = 0;
            do {

                Arrays.stream(resp.getHits().getHits()).forEach(hit->{
                    Map<String,Object> map=hit.getSourceAsMap();
                    map.put("_id",hit.getId());
                    collect.add(map);
                });
                scrollId = resp.getScrollId();
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                resp = restClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            } while (resp.getHits().getHits().length != 0);
            //清除滚屏
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            //也可以选择setScrollIds()将多个scrollId一起使用
            clearScrollRequest.addScrollId(scrollId);
            restClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            if (collect.size() == 0 || collect == null) {
                return  null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return collect;
    }

    /**
     * 删除索引
     * @param restClient
     * @param index
     * @throws IOException
     */
    public static void deleteIndex(RestHighLevelClient restClient, String index) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);

        if(restClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(index);
            restClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        }
    }



    /**
     * 根据条件查询
     * @param restClient
     * @param index
     * @param query
     * @return
     */
    public static List<Map<String, Object>> searchByConditions(RestHighLevelClient restClient, String index, Map<String, Object> query) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        SearchRequest searchRequest = new SearchRequest(index);
        if (query != null && !query.keySet().isEmpty()) {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            query.keySet().forEach(key -> {
                boolBuilder.must(QueryBuilders.matchQuery(key, query.get(key)));

            });
            searchSourceBuilder.query(boolBuilder);
            searchRequest.source(searchSourceBuilder);
        }
        try {
            SearchResponse response = restClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> map = hit.getSourceAsMap();
                map.put("_id",hit.getId());
                result.add(map);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void queryBuilder(Map<String, Object> query,
                              SearchRequest searchRequest) {
        if (query != null && !query.keySet().isEmpty()) {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            query.keySet().forEach(key -> {
                boolBuilder.must(QueryBuilders.matchQuery(key, query.get(key)));

            });
            searchSourceBuilder.query(boolBuilder);
            searchRequest.source(searchSourceBuilder);
        }
    }
    /**
     * 添加文档
     * @param restClient
     * @param index
     * @param jsonMap
     * @return
     */
    public static int insertDoc(RestHighLevelClient restClient, String index, Map<String,Object> jsonMap){
        //不设置id时，id会自动生成。
        IndexRequest indexRequest = new IndexRequest(index).source(jsonMap);
        //opType must be 'create' or 'index'.
        // optype=index时，如果某ID对应的document已经存在，它将被替换。
        //NOTE: 要么需要判断该id是否存在，已存在则不新增；要么让id自动生成。
        indexRequest.opType(DocWriteRequest.OpType.INDEX);
        //indexRequest.timeout(TimeValue.timeValueSeconds(1));
        //强制刷新，消除延迟
        indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        /*
         *几何类型的字段，可能报错：需要按几何对象格式去存储。
         *Elasticsearch exception [type=mapper_parsing_exception,
         * reason=failed to parse field [location] of type [geo_point]]]
         */
        IndexResponse indexResponse = null;
        try {
            indexResponse = restClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int result =  indexResponse.status().getStatus();
        if(result== RestStatus.OK.getStatus() || result == RestStatus.CREATED.getStatus()){
            return 1; //新增成功
        }else {
            return 0;
        }
    }

    /**
     * 更新文档
     * @param restClient
     * @param index
     * @param id
     * @param kvs
     * @return
     */
    public static int updateDoc(RestHighLevelClient restClient, String index, String id, Map<String, Object> kvs) {
        try {

            UpdateRequest updateRequest = new UpdateRequest(index, id);

            updateRequest.doc(kvs);
            //NOTE:查询document，如果不存在，就不更新。
            UpdateResponse updateResp = restClient.update(updateRequest, RequestOptions.DEFAULT);
            //强制刷新，消除延迟
            updateResp.setForcedRefresh(true);
            int result = updateResp.status().getStatus();
            if(result== RestStatus.OK.getStatus() || result == RestStatus.CREATED.getStatus()){
                return 1; //更新成功
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 从mysql 将数据导入 es
     * @param tableName 要导入的表名
     */
    public static void importData(StringRedisTemplate stringRedisTemplate, RestHighLevelClient client, DataSource dataSource, String tableName ,int start,int end,int pool) {
        // 需要多线程处理的大量数据list
        List<Integer> data = new ArrayList<>();
        for (int i = start; i < end; i++) {//319 850
            data.add(i);
        }


        // 创建多线程处理任务
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(pool);
        ITask<ResultBean, Object> task = new MySqlToESEx(stringRedisTemplate,client,dataSource);
        // 辅助参数  加数
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        params.put("tableName",tableName);
        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);

        logger.info(resultBean.getData().toString());
    }


    /**
     * 从mysql 将数据导入 es
     * @param tableName 要导入的表名
     */
    public static void deleteEsData(StringRedisTemplate stringRedisTemplate, EsDao esDao, DataSource dataSource, String tableName ,int start,int end) {
        // 需要多线程处理的大量数据list
        List<Integer> data = new ArrayList<>();
        for (int i = start; i < end; i++) {//319 850
            data.add(i);
        }


        // 创建多线程处理任务
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(5);
        ITask<ResultBean, Object> task = new MySqlDeleteESEx(stringRedisTemplate,esDao,dataSource);
        // 辅助参数  加数
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        params.put("tableName",tableName);
        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);

        logger.info(resultBean.getData().toString());
    }


    /**
     * 从mysql 将数据导入 es
     */
    public static void deleteEsDataTj(EsDao esDao, DataSource dataSource,String index,Integer projectId) {
        // 需要多线程处理的大量数据list
        List<Integer> data = new ArrayList<>();
        data.add(887);//占位，无用
        // 创建多线程处理任务
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(1);
        ITask<ResultBean, Object> task = new MySqlDeleteESExTJ(esDao,dataSource);
        // 辅助参数  加数
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        params.put("index",index);
        params.put("projectId",projectId);
        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);
        logger.info(resultBean.getData().toString());
    }

    /**
     * 从mysql 将job_comment g表中的数据导入到ES job 索引 字段 jobComment中
     */
    public static void updateJob(StringRedisTemplate stringRedisTemplate, EsDao esDao, DataSource dataSource,String index) {
        // 需要多线程处理的大量数据list
        List<Integer> data = new ArrayList<>();
        Connection connect = null;
        try {
            connect = dataSource.getConnection();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        ResultSet rs = null;
        PreparedStatement st = null;
        String sql = " SELECT jc.job_id from job_comment jc,job j where jc.job_id=j.id GROUP BY jc.job_id ";
        try {
                st = connect.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                rs = st.executeQuery();
                HashMap<String, String> map = null;
                Integer v = null;
                while (rs.next()) {
                    v = rs.getInt("job_id");
                    data.add(v);
                }
        } catch (Exception e1) {
            e1.printStackTrace();
        }


        // 创建多线程处理任务
        logger.info("job_idList:"+data.size());
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(5);
        ITask<ResultBean, Object> task = new MySqlUpdateESExJob(stringRedisTemplate,esDao,dataSource);
        // 辅助参数  加数
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        params.put("tableName","job_comment");
        params.put("index",index);
        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);
        // logger.info(resultBean.getData().toString());
    }


    /**
     * 从mysql 将project_course 表中的数据导入到ES CourseStudyTime 索引 字段 study_time中
     */
    public static void updateCourseStudyTime(StringRedisTemplate stringRedisTemplate, EsDao esDao, DataSource dataSource,Integer project_id) {
        HashOperations<String, Object, Object> ops = stringRedisTemplate.opsForHash();
        // 需要多线程处理的大量数据list
        List<Integer> data = new ArrayList<>();
        data.add(project_id);
        Connection connect = null;
        try {
            connect = dataSource.getConnection();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        ResultSet rs = null;
        PreparedStatement st = null;
        String sql = "SELECT course_code,video_len,period from project_course where project_id = ?  group by course_code";
        try {
            st = connect.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setInt(1, project_id);
            rs = st.executeQuery();
            HashMap<String, String> map = null;
            String v = null;
            while (rs.next()) {
                v = "tjxm-course_code:"+project_id+rs.getString("course_code");
                if(!stringRedisTemplate.hasKey(v)){
                    ops.put(v, v, rs.getString("video_len")+"#"+rs.getString("period"));
                    Long timeout = 1L;
                    stringRedisTemplate.expire(v, timeout, TimeUnit.DAYS);
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        // 创建多线程处理任务
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(1);
        ITask<ResultBean, Object> task = new MySqlUpdateESExCST(stringRedisTemplate,esDao);
        // 辅助参数  加数
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);
        logger.info(resultBean.getData().toString());
    }



    /**
     *
     * @param stringRedisTemplate
     * @param esDao
     * @param ct 压力测试条数
     * @param index 索引名
     * @param pool 线程数量
     * @return
     */
    public static long stressTestingES(StringRedisTemplate stringRedisTemplate, EsDao esDao,long ct,String index,Integer pool) {
        // 需要多线程处理的大量数据list
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        int pageSize = 500;
        if(pageSize>ct){
            pageSize = (int)ct;
        }
        List<Integer> data = getPageInfo(ct,pageSize);
        // 创建多线程处理任务
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(pool);
        ITask<ResultBean, Object> task = new ESStressTest(stringRedisTemplate,esDao);
        // 辅助参数  加数
        params.put("index",index);
        params.put("pageSize",pageSize);
        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);

        logger.info(resultBean.getData().toString());
        return ct;
    }


    /**
     *
     * @param ct 压力测试条数
     * @param tableName 表名
     * @param pool 线程数量
     * @return
     */
    public static long stressTestingMySql(DataSource dataSource,long ct,String tableName,Integer pool,String type) {
        // 需要多线程处理的大量数据list
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        List<Integer> data = new ArrayList<>();
        for(int i=1;i<=ct;i++){
            data.add(i);
        }
        // 创建多线程处理任务
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(pool);
        ITask<ResultBean, Object> task = new MySqlStressTest(dataSource);
        // 辅助参数  加数
        params.put("tableName",tableName);
        params.put("type",type);
        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);
        logger.info(resultBean.getData().toString());
        return ct;
    }


    /**
     *
     * @param ct 压力测试条数
     * @param pool 线程数量
     * @return
     */
    public static long stressTestingMongdb(MongoDbService mongoDbService, StudyActionRecordService studyActionRecordService, long ct, Integer pool) {
        // 需要多线程处理的大量数据list
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        List<Integer> data = new ArrayList<>();
        for(int i=1;i<=ct;i++){
            data.add(i);
        }
        // 创建多线程处理任务
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(pool);
        ITask<ResultBean, Object> task = new MongDbStressTest(mongoDbService,studyActionRecordService);
        // 辅助参数  加数
        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);
        logger.info(resultBean.getData().toString());
        return ct;
    }

    /**
     *
     * @param ct 压力测试条数
     * @param pool 线程数量
     * @return
     */
    public static long stressTestingRedis(StringRedisTemplate stringRedisTemplate, Long expire, long ct, Integer pool) {
        // 需要多线程处理的大量数据list
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        params.put("expire",expire);
        List<Integer> data = new ArrayList<>();
        for(int i=1;i<=ct;i++){
            data.add(i);
        }
        // 创建多线程处理任务
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(pool);
        ITask<ResultBean, Object> task = new RedisStressTest(stringRedisTemplate);
        // 辅助参数  加数
        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);
        logger.info(resultBean.getData().toString());
        return ct;
    }


    /**
     * 从es 将数据增量导入 到mysql 为大数据分析时使用
     * @param qmap 要导入的信息
     */
    public static long importDataToMysql(StringRedisTemplate stringRedisTemplate, EsDao esDao, DataSource dataSource, Map qmap) {
        // 需要多线程处理的大量数据list
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        int pageSize = 200;
        String tableName = qmap.get("tableName").toString();
        qmap.remove("tableName");
        long ct =esDao.totalCountOfCondition(tableName,qmap);
        //如果没有查到数据返回
        if(ct == 0){
            return 0;
        }
        List<Integer> data = getPageInfo(ct,pageSize);

        // 创建多线程处理任务
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(4);
        ITask<ResultBean, Object> task = new ESToMySqlEx(stringRedisTemplate,esDao,dataSource);
        // 辅助参数  加数
        params.put("tableName",tableName);
        params.put("pageSize",pageSize);
        params.put("qmap",qmap);

        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);

        logger.info(resultBean.getData().toString());
        return ct;
    }

    private static   List<Integer>  getPageInfo (long ct,int pageSize){
        List<Integer> pageNums = new ArrayList<>();
        int pageNum = 1;
        if( ct <= pageSize){
            pageNums.add(pageNum);
        }else{
            for( int i = 0, count =1; i < ct; i += pageSize){
                pageNums.add(count++);
            }
        }
        System.out.println(pageNums);
        return pageNums;
    }
}
