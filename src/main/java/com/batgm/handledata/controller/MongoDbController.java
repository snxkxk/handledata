package com.batgm.handledata.controller;

import com.alibaba.fastjson.JSON;
import com.batgm.handledata.elasticsearch.BulkProcessorUtils;
import com.batgm.handledata.entity.StudyActionRecord;
import com.batgm.handledata.entity.StudyActionRecordBean;
import com.batgm.handledata.service.MongoDbService;
import com.batgm.handledata.service.StudyActionRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

/**
 * @author yqq
 * @createdate 2021/4/20
 */
@RestController
@RequestMapping("/mongo")
public class MongoDbController {
    @Autowired
    public MongoDbService mongoDbService;

    @Autowired
    private StudyActionRecordService studyActionRecordService;
    private static   final Logger logger = LoggerFactory.getLogger(StudyActionRecordController.class);


    @GetMapping("save")
    public String saveObj() {
        StudyActionRecord bean= studyActionRecordService.findById(new BigInteger("66666"));
        String msg = "save:";
        if(bean!=null){
            String json= JSON.toJSONString(bean);
            StudyActionRecordBean sbean =  JSON.parseObject(json,StudyActionRecordBean.class);
            sbean.setId(null);
            mongoDbService.saveObj(sbean);
            msg += "success["+sbean.toString()+"]";
            logger.info(msg);
        }
        return msg;
    }


    /**
     * 多线程压力测试Mongdb insert
     *
     * @param total
     * @param pool
     * @return
     */
    @GetMapping("stressTestingMongdb")
    public String stressTestingMongdb( Integer total, Integer pool) {
        long startTime = System.currentTimeMillis(); //获取结束时间
        String str = null;
        if (total == null) {
            total = 1000;
        }
        if (pool == null) {
            pool = 5;
        }
        BulkProcessorUtils.stressTestingMongdb(mongoDbService,studyActionRecordService,total, pool);
        long endTime = System.currentTimeMillis(); //获取结束时间
        long time = (endTime - startTime);
        if (time > 1000) {
            str = String.format("执行成功,写入 %d 条数据,启用 %d 个线程，总耗时 %d 秒[%d毫秒],并发#%d#", total, pool, time / 1000, time, total / (time / 1000));
        } else {
            str = "执行完成，时间低于一秒，无法计算具体数据";
        }
        return str;
    }

}