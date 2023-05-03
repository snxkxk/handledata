package com.batgm.handledata.utils.mysql2es;


import com.batgm.handledata.elasticsearch.EsDao;
import com.batgm.handledata.utils.muti.ITask;
import com.batgm.handledata.utils.muti.ResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
public class MySqlUpdateESExCST implements ITask<ResultBean, Object> {
    private static Logger logger = LoggerFactory.getLogger(MySqlUpdateESExCST.class);

    private EsDao esDao;

    private StringRedisTemplate stringRedisTemplate;

    public MySqlUpdateESExCST(StringRedisTemplate stringRedisTemplate, EsDao esDao) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.esDao = esDao;
    }

    @Override
    public ResultBean execute(Object e, ConcurrentHashMap<String, Object> params) {
        /**
         * 具体业务逻辑：将list中的元素加上辅助参数中的数据返回
         */
        String course_study_time = "course_study_time";
        AtomicInteger total = (AtomicInteger) params.get("count");
        HashOperations<String, Object, Object> ops = stringRedisTemplate.opsForHash();
        List<Integer> data = (List) e;
        int project_id = data.get(0);
        int ct = 0;
        Map qmap = new HashMap();
        qmap.put("project_id", project_id);
        qmap.put("is_complete", "1");
        List<Map<String, Object>> list = esDao.searchByConditions(course_study_time, qmap, 1, 200000);
        Map<String, Object> map = null;
        Map<String, Object> updatemap = new HashMap<>();
        String _id = null;
        String course_code = null;
        Integer study_time = null;
        String[] redisValue = null;
        logger.info("project:"+project_id+"start=======================");
        for (int i = 0; i < list.size(); i++) {
            map = list.get(i);
            course_code = "tjxm-course_code:"+project_id+map.get("course_code").toString();
            study_time = (Integer.parseInt(map.get("study_time").toString()));
            redisValue = ops.get(course_code, course_code).toString().split("#");
            total.getAndIncrement();
            if (study_time >= Integer.parseInt(redisValue[0])) {
                int tempST= (int) (Float.parseFloat(redisValue[1]) * 30);
                if(study_time < tempST){
                _id = map.get("_id").toString();
                //if("NNRXQ3MBUiqgsFPNaAuU".equals(_id)){
                try {
                    updatemap.clear();
                    updatemap.put("study_time", tempST);
                    updatemap.put("update_time", "2020-07-18 13:30:30");
                    updatemap.put("is_complete", "2");
                    esDao.updateDoc(course_study_time, _id, updatemap);
                    logger.info("tjxmdata"+i+":"+map.toString());
                    ct++;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                }
                //}
            }
        }
        logger.info("project:"+project_id+"end=======================");
        ResultBean resultBean = ResultBean.newInstance();
        resultBean.setData(data);
        resultBean.setCode(ct);
        return resultBean;
    }


}