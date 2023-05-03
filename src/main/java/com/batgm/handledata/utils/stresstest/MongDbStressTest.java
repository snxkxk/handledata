package com.batgm.handledata.utils.stresstest;


import com.alibaba.fastjson.JSON;
import com.batgm.handledata.entity.StudyActionRecord;
import com.batgm.handledata.entity.StudyActionRecordBean;
import com.batgm.handledata.service.MongoDbService;
import com.batgm.handledata.service.StudyActionRecordService;
import com.batgm.handledata.utils.muti.ITask;
import com.batgm.handledata.utils.muti.ResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
public class MongDbStressTest implements ITask<ResultBean, Object> {
    private static Logger logger = LoggerFactory.getLogger(MongDbStressTest.class);
    private MongoDbService mongoDbService;
    private StudyActionRecordService studyActionRecordService;


    public MongDbStressTest(MongoDbService mongoDbService,StudyActionRecordService studyActionRecordService){
        this.mongoDbService = mongoDbService;
        this.studyActionRecordService = studyActionRecordService;
    }

    @Override
    public ResultBean execute(Object e, ConcurrentHashMap<String, Object> params) {
        AtomicInteger total = (AtomicInteger) params.get("count");

        List<Integer> data = (List) e;
        //StudyActionRecord bean = studyActionRecordService.findById(new BigInteger("66666"));
        //if (bean != null) {
            //String json = JSON.toJSONString(bean);
         String json="{\"courseCode\":\"setCourseCode\",\"deviceType\":\"3\",\"fatherTableId\":333,\"ipAddress\":\"192.168.0.1\",\"loginPalce\":\"setLoginPalce\",\"projectId\":954,\"studyCircleId\":222,\"studyPlanId\":333,\"studyType\":\"setStudyType\",\"subjectTableId\":333,\"userId\":333}";
         StudyActionRecordBean sbean = JSON.parseObject(json, StudyActionRecordBean.class);
            for (int i = 0; i < data.size(); i++) {
                try {
                    sbean.setId(null);
                    mongoDbService.saveObj(sbean);
                } catch (Exception e1) {
                    logger.error("批量插入单条出错抛弃,数据:{},异常:{}", sbean.toString(), e1);
                    continue;
                }
            }
        //}
        ResultBean resultBean = ResultBean.newInstance();
        resultBean.setData(data);
        resultBean.setCode(data.size());
        total.set(total.intValue() + data.size());
        return resultBean;
    }

    }
