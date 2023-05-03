package com.batgm.handledata.utils.muti;


import com.batgm.handledata.utils.C3P0Inner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * 具体执行业务任务 需要 实现ITask接口  在execute中重写业务逻辑
 * TestTask<BR>
 * 创建人:yqq <BR>
 * 时间：2020年3月16日-下午8:40:32 <BR>
 * @version 2.0
 *
 */
public class TestTask implements ITask<ResultBean, Object> {
    private static Logger logger = LoggerFactory.getLogger(TestTask.class);
    @Override
    public ResultBean execute(Object e, ConcurrentHashMap<String, Object> params) {
        /**
         * 具体业务逻辑：将list中的元素加上辅助参数中的数据返回
         */
        AtomicInteger count = (AtomicInteger)params.get("count");
        Lock lock = (ReentrantLock)params.get("lock");
        String threadName = Thread.currentThread().getName();
        List<Integer> data=(List)e;
        Connection connect =(Connection)params.get("connect");
        ResultSet rs = null;
         PreparedStatement st = null;
        for (int i = 0; i < data.size(); i++) {
            int ct =0;
            // 需要处理的数据
            int user_id= data.get(i);
           // String testsql = "select * from action_study_record_20191114 where user_id=? ";
            String sql = "select * from study_action_record where user_id=? ";
            try {
                st =  connect.prepareStatement(sql);
                st.setInt(1,user_id);
                rs = st.executeQuery();
                int id ,father_table_id,subject_table_id;
                String action = null;
                //String testupdateSQL = "delete from action_study_record_20191114  where id = ?";
                String updateSQL = "delete from study_action_record  where id = ?";
                List<Integer> ids=new ArrayList();
                ConcurrentHashMap<String,String> map =new ConcurrentHashMap<>();
                while (rs.next()){
                    id = rs.getInt("id");
                    father_table_id = rs.getInt("father_table_id");
                    subject_table_id = rs.getInt("subject_table_id");
                    action =  rs.getString("action");
                    String key =father_table_id+action+subject_table_id;
                    if(map.containsKey(key)){
                        st = connect.prepareStatement(updateSQL);
                        st.setInt(1,id);
                        st.executeUpdate();
                        count.incrementAndGet();
                        ct++;
                        //logger.info("id:"+id+" action:"+action+" subject_table_id:"+subject_table_id+" father_table_id:"+father_table_id);


                    }else {
                        map.put(key,"has");
                    }
                }
                if(st!=null){
                    st.close();
                }
                if(rs!=null){
                    rs.close();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            logger.info("线程：{},第{}个用户#{}#处理完成,此用户共处理[{}]条", threadName, (i + 1),data.get(i),ct);
        }
        ResultBean resultBean  = ResultBean.newInstance();
        resultBean.setData(data);
        return resultBean;
    }

    public static void main(String[] args) {
        // 需要多线程处理的大量数据list
        List<Integer> data = new ArrayList<>();
        for(int i = 606; i < 708; i ++){//1801636
            data.add(i);
        }
        Connection connect = C3P0Inner.getConnection();

        // 创建多线程处理任务
        MultiThreadUtils<Integer> threadUtils = MultiThreadUtils.newInstance(10);
        ITask<ResultBean, Object> task = new TestTask();
        // 辅助参数  加数
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        params.put("connect",connect);
        // 执行多线程处理，并返回处理结果
        ResultBean resultBean = threadUtils.execute(data, params, task);
        if(connect!=null){
            try {
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println(resultBean.getData());
    }


}