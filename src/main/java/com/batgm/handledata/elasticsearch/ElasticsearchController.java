package com.batgm.handledata.elasticsearch;


import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(value = "es 工具类")
@RestController
@RequestMapping("estools")
public class ElasticsearchController {
    @Autowired
    private EsDao esDao;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 关键词检索
     *
     * @param key
     * @param value
     * @param index
     * @return
     */
    @GetMapping("searchByKeyWord")
    public String searchByKeyWord(@RequestParam String key, @RequestParam String value, @RequestParam(value = "index") String index) {
        List<Map<String, Object>> result = esDao.searchByKeyWord(index, key, value, 1, 5);
        return result.toString();
    }

    /**
     * 根据条件查询
     *
     * @param map
     * @param index
     * @param pageSize
     * @return
     */
    @ApiOperation(value ="查询数据列表")
    @GetMapping("list")
    public String list(@RequestBody(required = false) Map<String, Object> map, @RequestParam(value = "index") String index, Integer pageSize, Integer pageNum) {
        if (pageSize == null) {
            pageSize = 5;
        }
        if (pageNum == null) {
            pageNum = 1;
        }
        List<Map<String, Object>> list = esDao.searchByConditions(index, map, pageNum, pageSize);
        return list.toString();
    }


    @PostMapping("save")
    public String indexDoc(@RequestBody Map<String, Object> map, @RequestParam(value = "index") String index) {
        int rst = esDao.insertDoc(index, map);
        return "保存成功";
    }

    /**
     * 根据Id更新
     *
     * @param map
     * @param index
     * @param id
     * @return
     */
    @PostMapping("update")
    public String update(@RequestBody Map<String, Object> map, @RequestParam(value = "index") String index, @RequestParam(value = "id") String id) {
        try {
            esDao.updateDoc(index, id, map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "更新成功";
    }


    /**
     * 根据Id删除
     *
     * @param index
     * @param id
     * @return
     */
    @GetMapping("delete")
    public String delete(@RequestParam(value = "index") String index, @RequestParam(value = "id") String id) {

        esDao.deleteDoc(index, id);

        return "删除成功";
    }

    /**
     * 单条数据更新压力测试
     *
     * @param map
     * @return
     */
    @PostMapping("updateTest")
    public String updateTest(@RequestBody Map<String, Object> map) {
        long startTime = System.currentTimeMillis();   //获取开始时间

        String index = map.get("index").toString();
        map.remove("index");
        int ct = 1;
        int rt = 0;
        int rsv = 0;
        if (map.containsKey("ct")) {
            ct = (int) map.get("ct");
            map.remove("ct");
        }
        if (StringUtils.isNotBlank(index)) {
            try {
                Map<String, Object> qmap = new HashMap();
                qmap.put("study_plan_id", map.get("study_plan_id"));
                qmap.put("father_table_id", map.get("father_table_id"));
                qmap.put("subject_table_id", map.get("subject_table_id"));
                qmap.put("user_id", map.get("user_id"));
                for (int i = 0; i < ct; i++) {
                    List<Map<String, Object>> list = esDao.searchByConditions(index, qmap, 1, 1);
                    String id = list.get(0).get("_id").toString();
                    rsv = esDao.updateDoc(index, id, map);
                    if (rsv == 1) {
                        rt++;
                    }
                }
            } catch (Exception e) {
                return e.getMessage();
            }
            long endTime = System.currentTimeMillis(); //获取结束时间
            return "#单条数据更新测试#执行总数: " + ct + "次,成功更新次数:" + rt + " 执行时间[" + (endTime - startTime) + "ms]";
        } else {
            return "参数无效";
        }

    }


    /**
     * 批量压力测试
     *
     * @param index
     * @param pageSize
     * @param pageNum
     * @return
     */
    @GetMapping("updateAll")
    public String updateAll(@RequestParam String index, Integer pageSize, Integer pageNum) {
        if (pageSize == null) {
            pageSize = 5;
        }
        if (pageNum == null) {
            pageNum = 1;
        }
        int rsv = 0;
        int rs = 0;
        long startTime = System.currentTimeMillis();   //获取开始时间
        List<Map<String, Object>> list = esDao.searchByConditions(index, null, pageNum, pageSize);
        String id = null;
        Map<String, Object> map = null;
        for (int i = 0; i < list.size(); i++) {
            map = list.get(i);
            id = map.get("_id").toString();
            map.remove("_id");
            try {
                rsv = esDao.updateDoc(index, id, map);
                if (rsv == 1) {
                    rs++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis(); //获取结束时间

        return "批量更新成功,更新总数量：" + rs + " 条,执行时间：" + (endTime - startTime) + "ms";


    }


    /**
     * 多线程压力测试更新
     *
     * @param index
     * @param total
     * @param pool
     * @return
     */
    @GetMapping("stressTestingES")
    public String stressTestingES(String index, Integer total, Integer pool) {
        long startTime = System.currentTimeMillis(); //获取结束时间
        String str = null;
        if (total == null) {
            total = 1000;
        }
        if (pool == null) {
            pool = 5;
        }
        if (index == null) {
            index = "study_action_record";
        }
        BulkProcessorUtils.stressTestingES(stringRedisTemplate, esDao, total, index, pool);
        long endTime = System.currentTimeMillis(); //获取结束时间
        long time = (endTime - startTime);
        if (time > 1000) {
            str = String.format("执行成功,更新 %d 条数据,启用 %d 个线程，总耗时 %d 秒[%d毫秒],并发#%d#", total, pool, time / 1000, time, total / (time / 1000));
        } else {
            str = "执行完成，时间低于一秒，无法计算具体数据";
        }
        return str;
    }


    /**
     * 多线程压力测试MySql insert
     *
     * @param tableName
     * @param total
     * @param pool
     * @return
     */
    @GetMapping("stressTestingMySql")
    public String stressTestingMySql(String tableName, Integer total, Integer pool,String type) {
        long startTime = System.currentTimeMillis(); //获取结束时间
        String str = null;
        if (total == null) {
            total = 1000;
        }
        if (pool == null) {
            pool = 5;
        }
        if (tableName == null) {
            tableName = "action_study_record_copy";
        }
        //params.put("type","batch");
        if (type == null) {
            type = "notbatch";
        }
        BulkProcessorUtils.stressTestingMySql(dataSource,total, tableName, pool,type);
        long endTime = System.currentTimeMillis(); //获取结束时间
        long time = (endTime - startTime);
        if (time > 1000) {
            str = String.format("执行成功,插入 %d 条数据,启用 %d 个线程，总耗时 %d 秒[%d毫秒],并发#%d#", total, pool, time / 1000, time, total / (time / 1000));
        } else {
            str = "执行完成，时间低于一秒，无法计算具体数据";
        }
        return str;
    }


    /**
     * mysql 数据导入es
     *
     * @param table
     * @return
     */
    @GetMapping("importData")
    public String importData(@RequestParam(value = "table") String table,Integer start,Integer end,Integer pool,@RequestParam(value = "action",required = false)String action) {
        HashOperations<String, Object, Object> ops = stringRedisTemplate.opsForHash();
        if (stringRedisTemplate.hasKey(table)) {
            String data = ops.get(table, table).toString();
            return table + "正在执行数据，请勿重复操作。。。。\n" + "开始时间：" + data;
        }
        if (pool == null){
            pool = 5;
        }
        if (start == null){
            start = 319;
        }
        if (end == null){
            end = 950;
        }
        ops.put(table, table, new Date().toString());
        Long timeout = 2L;
        stringRedisTemplate.expire(table, timeout, TimeUnit.DAYS);
        BulkProcessorUtils.createIndex(client, table);//先创建索引
        if("data".equals(action)){
        BulkProcessorUtils.importData(stringRedisTemplate, client, dataSource, table,start,end,pool);
        }
        //导入完成后删除redis数据
        if (stringRedisTemplate.hasKey(table)) {
            ops.delete(table, table);
        }
        return "执行成功";
    }

    @GetMapping("testRedis")
    public String testRedis(@RequestParam(value = "size") Integer size,String action) {
        HashOperations<String, Object, Object> ops = stringRedisTemplate.opsForHash();
        String key_pre=null;
        String value = "testRedisData:test";
        if (size == null){
            size = 100;
        }
        long startTime = System.currentTimeMillis(); //获取结束时间
        for(int i=0;i<size;i++){
            key_pre="test:";
            key_pre += i;
            ops.put(key_pre, key_pre, value);
            stringRedisTemplate.expire(key_pre, 5, TimeUnit.MINUTES);
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        long saveTime = endTime-startTime;
        if(action==null){


        for (int i = 0; i < size; i++) {
            key_pre = "test:";
            key_pre += i;
            if (stringRedisTemplate.hasKey(key_pre)) {
                ops.delete(key_pre, key_pre);
            }
        }
        }else{
            stringRedisTemplate.delete(stringRedisTemplate.keys("test:*"));
        }
        long delTime = System.currentTimeMillis()-endTime;
        return  String.format("执行成功,%d 条数据,保存总耗时 %d 毫秒,删除总耗时 %d 毫秒[%s]", size,saveTime, delTime,action);
    }
    /**
     * 根据Mysql数据删除Es数据-删除2.33版本提前导入的分离数据
     *
     * @param table
     * @return
     */
    @GetMapping("deleteEsDataBySqlData")
    public String deleteEsDataBySqlData(@RequestParam(value = "table") String table,Integer start,Integer end) {
        HashOperations<String, Object, Object> ops = stringRedisTemplate.opsForHash();
        if (stringRedisTemplate.hasKey(table)) {
            String data = ops.get(table, table).toString();
            return table + "正在执行数据，请勿重复操作。。。。\n" + "开始时间：" + data;
        }
        if (start == null){
            start = 319;
        }
        if (end == null){
            end = 860;
        }
        ops.put(table, table, new Date().toString());
        Long timeout = 2L;
        stringRedisTemplate.expire(table, timeout, TimeUnit.DAYS);
        BulkProcessorUtils.deleteEsData(stringRedisTemplate, esDao, dataSource, table,start,end);
        //导入完成后删除redis数据
        if (stringRedisTemplate.hasKey(table)) {
            ops.delete(table, table);
        }
        return "执行成功";
    }


    /**
     * 根据Mysql数据删除Es数据-删除 天津项目选错阶段数据删除course_study_time
     *
     * @return
     */
    @GetMapping("deleteEsDataBySqlDataTJ")
    public String deleteEsDataBySqlDataTJ(@RequestParam(value = "index") String index,@RequestParam(value = "projectId") Integer projectId) {

        BulkProcessorUtils.deleteEsDataTj(esDao,dataSource,index,projectId);

        return "执行成功";
    }
    @GetMapping("deleteEsData")
    public String deleteEsData(@RequestParam(value = "index") String index) {
        Map qmap = new HashMap();
        qmap.put("project_id",887);
        qmap.put("father_table_id",8942);
        qmap.put("subject_table_id",13284);
        String user_idS= "2224694,2225609,2227789,2224458,2230678,2223509,2224685,2227453,2216717,2225298,2220150,2230643,2226322,2226049,2227265,2227270,2228052,2227367,2227445,2234406,2226785,2226009,2227246,2225599,2229235,2225316,2225469,2227254,2227273,2224804,2227232,2229523";
        String _id=null;
        Map<String,Object>  map = null;
        String [] users=user_idS.split(",");
        System.out.println(""+users.length);
        for(int i=0;i< users.length;i++){
            qmap.put("user_id",users[i]);
            List<Map<String, Object>> list = esDao.searchByConditions(index,qmap,1,5);
            if(list.size()>0){
                map = list.get(0);
                _id = map.get("_id").toString();
                //System.out.println(_id);
                esDao.deleteDoc(index,_id);
            }else{
                System.out.println("users not find:" +users[i]);
            }
        }



        return "执行成功";
    }


    /**
     * 将job_comment 表中的数据写入到 ES索引为job 的 jobComment属性中
     *
     * @return
     */
    @GetMapping("updateJob")
    public String updateJob(@RequestParam(value = "index",required = false)String index) {
        String updateJob = "updateJob";

        HashOperations<String, Object, Object> ops = stringRedisTemplate.opsForHash();
        if (stringRedisTemplate.hasKey(updateJob)) {
            String data = ops.get(updateJob, updateJob).toString();
            return updateJob + "正在执行数据，请勿重复操作。。。。\n" + "开始时间：" + data;
        }

        ops.put(updateJob, updateJob, new Date().toString());
        Long timeout = 1L;
        stringRedisTemplate.expire(updateJob, timeout, TimeUnit.DAYS);
        if(index == null){
            index = "job";
        }
        BulkProcessorUtils.updateJob(stringRedisTemplate, esDao, dataSource,index);
        //导入完成后删除redis数据
        if (stringRedisTemplate.hasKey(updateJob)) {
            ops.delete(updateJob, updateJob);
        }
        return "执行成功";
    }

    /**
     * 根据学员课程所选视频学习完成时间更新课程完成
     * 30分钟=1学时
     * 天津项目 887,895,931
     * 更新ES 中CourseStudyTime 索引中的数据
     * @return
     */
    @GetMapping("updateCST")
    public String updateCourseStudyTimeES(@RequestParam(value = "projectId") Integer projectId) {
        String updateCST = "updateCST"+projectId;
        if(projectId!=887&&projectId!=895&&projectId!=931){
            return "get out ....";
        }

        HashOperations<String, Object, Object> ops = stringRedisTemplate.opsForHash();
        if (stringRedisTemplate.hasKey(updateCST)) {
            String data = ops.get(updateCST, updateCST).toString();
            return updateCST + "正在执行数据，请勿重复操作。。。。\n" + "开始时间：" + data;
        }

        ops.put(updateCST, updateCST, new Date().toString());
        Long timeout = 1L;
        stringRedisTemplate.expire(updateCST, timeout, TimeUnit.DAYS);
        BulkProcessorUtils.updateCourseStudyTime(stringRedisTemplate, esDao, dataSource,projectId);
        //导入完成后删除redis数据
        if (stringRedisTemplate.hasKey(updateCST)) {
            ops.delete(updateCST, updateCST);
        }
        return "执行成功";
    }

    /**
     * 创建普通索引
     *
     * @param indexName
     */
    @GetMapping("createIndex")
    public void createIndex(@RequestParam String indexName) {
        esDao.CreateIndex(indexName);
    }

}




