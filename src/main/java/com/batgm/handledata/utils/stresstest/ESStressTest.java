package com.batgm.handledata.utils.stresstest;


import com.batgm.handledata.elasticsearch.EsDao;
import com.batgm.handledata.utils.muti.ITask;
import com.batgm.handledata.utils.muti.ResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 具体执行业务任务 需要 实现ITask接口  在execute中重写业务逻辑
 * StressTest<BR>
 * 创建人:yqq <BR>
 * 时间：2020年05月11日-下午14:40:32 <BR>
 *
 * @version 2.0
 */
@Configuration
public class ESStressTest implements ITask<ResultBean, Object> {
    private static Logger logger = LoggerFactory.getLogger(ESStressTest.class);

    private EsDao esDao;

    private StringRedisTemplate stringRedisTemplate;


    public ESStressTest(StringRedisTemplate stringRedisTemplate, EsDao esDao){
        this.stringRedisTemplate = stringRedisTemplate;
        this.esDao = esDao;
    }

    @Override
    public ResultBean execute(Object e, ConcurrentHashMap<String, Object> params) {
        AtomicInteger total = (AtomicInteger) params.get("count");
        String index = params.get("index").toString();
        int pageSize = (int)params.get("pageSize");
        List<Integer> data = (List) e;
        int pageNum = 1;
        int ct= 0;
        for(int i = 0 ;i < data.size();i++){
        pageNum = data.get(i);
        List<Map<String, Object>> list = esDao.searchByConditions(index,null,pageNum,pageSize);
            String id = null;
            Map<String,Object>  map = null;
            for(int j = 0;j< list.size();j++){
                map = list.get(j);
                id = map.get("_id").toString();
                map.remove("_id");
                try {
                    esDao.updateDoc(index,id,map);
                    ct++;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        ResultBean resultBean = ResultBean.newInstance();
        resultBean.setData(data);
        resultBean.setCode(ct);
        total.set(total.intValue()+ct);
        return resultBean;
    }




}