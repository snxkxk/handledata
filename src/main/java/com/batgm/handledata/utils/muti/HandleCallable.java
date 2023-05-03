package com.batgm.handledata.utils.muti;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *
 *
 * HandleCallable<BR>
 * 创建人:yqq <BR>
 * 时间：2020年3月4日-上午11:55:41 <BR>
 *
 * @version 2.0
 *
 */
public class HandleCallable<E> implements Callable<ResultBean> {
    private static Logger logger = LoggerFactory.getLogger(HandleCallable.class);
    // 线程名称
    private String threadName = "";
    // 需要处理的数据
    private List<E> data;
    // 辅助参数
    private ConcurrentHashMap<String, Object> params;
    // 具体执行任务
    private ITask<ResultBean, Object> task;

    public HandleCallable(String threadName, List<E> data, ConcurrentHashMap params,
                          ITask<ResultBean, Object> task) {
        this.threadName = threadName;
        this.data = data;
        this.params = params;
        this.task = task;
    }

    @Override
    public ResultBean call() throws Exception {
        // 该线程中所有数据处理返回结果
        ResultBean result = null;
        if (data != null && data.size() > 0) {
            logger.info("线程：{},共处理:{}个项目，开始处理......", threadName, data.size());
            // 返回结果集
            result = task.execute(data, params);
            AtomicInteger total=  (AtomicInteger) params.get("count");
            // 循环处理每个数据
            logger.info("线程：{},共处理:{}个项目，处理完成......总计:{}", threadName, data.size(),total.intValue());
        }
        return result;
    }

}