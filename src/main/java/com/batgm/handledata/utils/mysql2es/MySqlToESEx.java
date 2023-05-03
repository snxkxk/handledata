package com.batgm.handledata.utils.mysql2es;


import com.batgm.handledata.elasticsearch.BulkProcessorUtils;
import com.batgm.handledata.utils.C3P0Inner;
import com.batgm.handledata.utils.muti.ITask;
import com.batgm.handledata.utils.muti.MultiThreadUtils;
import com.batgm.handledata.utils.muti.ResultBean;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 具体执行业务任务 需要 实现ITask接口  在execute中重写业务逻辑
 * TestTask<BR>
 * 创建人:yqq <BR>
 * 时间：2020年03月25日-下午8:40:32 <BR>
 *
 * @version 2.0
 */
@Configuration
public class MySqlToESEx implements ITask<ResultBean, Object> {
    private static Logger logger = LoggerFactory.getLogger(MySqlToESEx.class);
    private DataSource dataSource;

    private RestHighLevelClient client;

    private StringRedisTemplate stringRedisTemplate;

    public MySqlToESEx(StringRedisTemplate stringRedisTemplate,RestHighLevelClient client, DataSource dataSource){
        this.stringRedisTemplate = stringRedisTemplate;
        this.client = client;
        this.dataSource = dataSource;
    }

    @Override
    public ResultBean execute(Object e, ConcurrentHashMap<String, Object> params) {
        /**
         * 具体业务逻辑：将list中的元素加上辅助参数中的数据返回
         */

        AtomicInteger total = (AtomicInteger) params.get("count");
        String tableName = params.get("tableName").toString();
        //String threadName = Thread.currentThread().getName();
        List<Integer> data = (List) e;
        Connection connect = null;
        try {
            connect = dataSource.getConnection();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        BulkProcessor bulkProcessor = BulkProcessorUtils.getBulkProcessor(client);
        ResultSet rs = null;
        PreparedStatement st = null;
        int ct=0;
        for (int x = 0; x < data.size(); x++) {
            int project_id = data.get(x);
            //String indexName = project_id + "_"+tableName;
            String indexName = tableName;


            String sql = "select * from "+tableName+" where project_id = ? ";
            //String sql = "select * from job where project_id = ? ";
            try {
                st = connect.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                st.setInt(1, project_id);
                rs = st.executeQuery();
                ResultSetMetaData colData = rs.getMetaData();

                ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
                HashMap<String, String> map = null;
                int count = 0;
                String c = null;
                String v = null;
                while (rs.next()) {
                    total.getAndIncrement();
                    count++;
                    ct++;
                    map = new HashMap<String, String>(100);
                    for (int i = 1; i <= colData.getColumnCount(); i++) {
                        c = colData.getColumnName(i);
                        v = rs.getString(c);
                        map.put(c, v);
                    }
                    dataList.add(map);
                    // 每2万条写一次，不足的批次的最后再一并提交
                    if (count % 20000 == 0) {
                        logger.info("Mysql handle data number : " + count);
                        // 写入ES
                        for (HashMap<String, String> hashMap2 : dataList) {
                            bulkProcessor.add(new IndexRequest(indexName).source(hashMap2));
                        }
                        // 每提交一次便将map与list清空
                        map.clear();
                        dataList.clear();
                    }
                }


                // count % 200000 处理未提交的数据
                for (HashMap<String, String> hashMap2 : dataList) {
                    bulkProcessor.add(
                            new IndexRequest(indexName).source(hashMap2));
                }
                logger.info("-------------------------- Finally insert number total : " + count);
                // 将数据刷新到es, 注意这一步执行后并不会立即生效，取决于bulkProcessor设置的刷新时间
                bulkProcessor.flush();
            } catch (Exception e1) {
                e1.printStackTrace();
            }


        }
        try {
            st.close();
            rs.close();
            connect.close();
            boolean terminatedFlag = bulkProcessor.awaitClose(10, TimeUnit.SECONDS);
            //Thread.sleep(120000);
            //client.close();
        } catch (Exception e2) {
            e2.printStackTrace();
            logger.error(e2.getMessage());
        }
        ResultBean resultBean = ResultBean.newInstance();
        resultBean.setData(data);
        resultBean.setCode(ct);
        return resultBean;
    }
}