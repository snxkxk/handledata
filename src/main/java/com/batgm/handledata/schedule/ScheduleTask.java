package com.batgm.handledata.schedule;

import com.batgm.handledata.elasticsearch.BulkProcessorUtils;
import com.batgm.handledata.elasticsearch.EsDao;
import com.batgm.handledata.entity.EsMysqlRecord;
import com.batgm.handledata.service.EsMysqlRecordService;
import com.batgm.handledata.utils.DateFormatUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * "0 0 12 * * ?"每天中午十二点触发
 * "0 15 10 ? * *"每天早上10:15触发
 * "0 15 10 * * ?"每天早上10:15触发
 * "0 15 10 * * ? *"每天早上10:15触发
 * "0 15 10 * * ? 2005"2005年的每天早上10:15触发
 * "0 * 14 * * ?"每天从下午2点开始到2点59分每分钟一次触发
 * "0 0/5 14 * * ?"每天从下午2点开始到2:55分结束每5分钟一次触发
 * "0 0/5 14,18 * * ?"每天下午2点至2:55和6点至6点55分两个时间段内每5分钟一次触发
 * "0 0-5 14 * * ?"每天14:00至14:05每分钟一次触发
 * "0 10,44 14 ? 3 WED"三月的每周三的14:10和14:44触发
 * "0 15 10 ? * MON-FRI"每个周一,周二,周三,周四,周五的10:15触发
 * "0 15 10 15 * ?"每月15号的10:15触发
 * "0 15 10 L * ?"每月的最后一天的10:15触发
 * "0 15 10 ? * 6L"每月最后一个周五的10:15触发
 * "0 15 10 ? * 6L 2002-2005"2002年至2005年的每月最后一个周五的10:15触发
 * "0 15 10 ? * 6#3"每月的第三个周五的10:15触发
 */
 // "0 0 */1 * * ?" 每小时执行一次
 //"0 0/5 * * * ?"每五钟执行一次


//@Component
//@EnableScheduling
public class ScheduleTask {
    @Autowired
    private EsDao esDao;
    @Autowired
    private DataSource dataSource;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private EsMysqlRecordService esMysqlRecordService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleTask.class);
    @Scheduled(cron = "0 0/30 * * * ?")
    public void test() {
        LOGGER.info("定时任务测试############test:"+new Date().toString());
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void EsToMysql() {
        LOGGER.info("ElasticSearch Data to Mysql Task start");
        String tableName = "study_action_record";
        EsMysqlRecord bean= new EsMysqlRecord();
        bean.setTableName(tableName);
        bean = esMysqlRecordService.getOneEsMysqlRecord(bean);
        Date now =new Date();
        Map qmap =new HashMap<String,Object>();
        String gte = "2017-01-01 00:00:00";
        qmap.put("tableName",tableName);
        Map conditions = new HashMap<String,Object>();
        String nowStr = DateFormatUtils.getData(now);
        conditions.put("lt",nowStr);
        qmap.put("create_time",conditions);
        if(bean != null){
            gte = DateFormatUtils.getData(bean.getUpdateTime());
            conditions.put("gte",gte);
            long ct =BulkProcessorUtils.importDataToMysql(stringRedisTemplate,esDao,dataSource,qmap);
                bean.setNum((int) ct);
                bean.setUpdateTime(now);
                bean.setContent("最近一次同步["+ct+"]条");
                bean.setCt(bean.getCt()+1);
                bean.setTotal(bean.getTotal()+(int)ct);
                esMysqlRecordService.update(bean);
        }else {
            bean= new EsMysqlRecord();
            conditions.put("gte",gte);
            long ct =BulkProcessorUtils.importDataToMysql(stringRedisTemplate,esDao,dataSource,qmap);
                bean.setNum((int)ct);
                bean.setTableName(tableName);
                bean.setCreateTime(now);
                bean.setUpdateTime(now);
                bean.setCt(1);
                bean.setTotal((int)ct);
                bean.setContent("第一次同步["+ct+"]条");
                esMysqlRecordService.save(bean);
        }


        LOGGER.info("ElasticSearch Data to Mysql Task end");
    }


}
