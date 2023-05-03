package com.batgm.handledata.utils.stresstest;


import com.alibaba.fastjson.JSON;
import com.batgm.handledata.entity.StudyActionRecordBean;
import com.batgm.handledata.service.MongoDbService;
import com.batgm.handledata.service.StudyActionRecordService;
import com.batgm.handledata.utils.muti.ITask;
import com.batgm.handledata.utils.muti.ResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 具体执行业务任务 需要 实现ITask接口  在execute中重写业务逻辑
 * TestTask<BR>
 * 创建人:yqq <BR>
 * 时间：2021年04月21日-下午8:40:32 <BR>
 *
 * @version 2.0
 */
@Configuration
public class RedisStressTest implements ITask<ResultBean, Object> {
    private static Logger logger = LoggerFactory.getLogger(RedisStressTest.class);
    private StringRedisTemplate stringRedisTemplate;


    public RedisStressTest(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ResultBean execute(Object e, ConcurrentHashMap<String, Object> params) {
        AtomicInteger total = (AtomicInteger) params.get("count");
        Long expire = (Long) params.get("expire");
        List<Integer> data = (List) e;

         String json="{\"courseCode\":\"setCourseCode\",\"deviceType\":\"3\",\"fatherTableId\":333,\"ipAddress\":\"192.168.0.1\",\"loginPalce\":\"setLoginPalce\",\"projectId\":954,\"studyCircleId\":222,\"studyPlanId\":333,\"studyType\":\"setStudyType\",\"subjectTableId\":333,\"userId\":333}";
        HashOperations<String, Object, Object> ops = stringRedisTemplate.opsForHash();
        String key = "stress_test:";
        Long timeout=200000L;
        if(expire!=null){
            timeout = expire;
        }
         for (int i = 0; i < data.size(); i++) {
                try {
                    ops.put(key+data.get(i), key+data.get(i), json);
                    stringRedisTemplate.expire(key+data.get(i), timeout, TimeUnit.SECONDS);
                } catch (Exception e1) {
                    logger.error("批量插入单条出错抛弃,数据:{},异常:{}", json, e1);
                    continue;
                }
            }

        ResultBean resultBean = ResultBean.newInstance();
        resultBean.setData(data);
        resultBean.setCode(data.size());
        total.set(total.intValue() + data.size());
        return resultBean;
    }

    }
