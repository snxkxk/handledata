package com.batgm.handledata.utils.mysql2es;


import com.batgm.handledata.elasticsearch.EsDao;
import com.batgm.handledata.utils.muti.ITask;
import com.batgm.handledata.utils.muti.ResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
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
public class MySqlDeleteESExTJ implements ITask<ResultBean, Object> {
    private static Logger logger = LoggerFactory.getLogger(MySqlDeleteESExTJ.class);
    private DataSource dataSource;

    private EsDao esDao;


    public MySqlDeleteESExTJ(EsDao esDao, DataSource dataSource){
        this.esDao = esDao;
        this.dataSource = dataSource;
    }

    @Override
    public ResultBean execute(Object e, ConcurrentHashMap<String, Object> params) {
        /**
         * 具体业务逻辑：将list中的元素加上辅助参数中的数据返回
         */

        AtomicInteger total = (AtomicInteger) params.get("count");
        String index = params.get("index").toString();
        int project_id = (int)params.get("projectId");
        List<Integer> data = (List) e;
        Connection connect = null;
        try {
            connect = dataSource.getConnection();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        ResultSet rs = null;
        PreparedStatement st = null;
        int ct=0;
        String sql = " SELECT * from user_select ";
            try {
                st = connect.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                rs = st.executeQuery();
                ResultSetMetaData colData = rs.getMetaData();
                ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
                HashMap<String, String> map = null;
                int count = 0;
                String c = null;
                String v = null;
                while (rs.next()) {
                    count++;
                    map = new HashMap<String, String>();
                    for (int i = 1; i <= colData.getColumnCount(); i++) {
                        c = colData.getColumnName(i);
                        v = rs.getString(c);
                        map.put(c, v);
                    }
                    dataList.add(map);
                }

                ct = dataList.size();
                for (HashMap<String, String> hashMap2 : dataList) {
                    Map qmap=new HashMap();
                    qmap.put("user_id",hashMap2.get("user_id"));
                    qmap.put("study_plan_id",hashMap2.get("study_plan_id"));
                    qmap.put("project_id",project_id);
                    boolean rst = deleteEsData(qmap,index);
                    if(rst){
                        total.getAndIncrement();
                    }
                }
                logger.info("-------------------------- Finally insert number total : " + count);
                // 将数据刷新到es, 注意这一步执行后并不会立即生效，取决于bulkProcessor设置的刷新时间
            } catch (Exception e1) {
                e1.printStackTrace();
            }


        try {
            st.close();
            rs.close();
            connect.close();

        } catch (Exception e2) {
            e2.printStackTrace();
            logger.error(e2.getMessage());
        }
        ResultBean resultBean = ResultBean.newInstance();
        resultBean.setData(data);
        resultBean.setCode(ct);
        return resultBean;
    }

    private boolean  deleteEsData(Map qmap,String index){
        boolean  success = false;
        String _id=null;
        List<Map<String, Object>> list = esDao.searchByConditions(index,qmap,1,100);
        if(list.size()>0){
            Map<String,Object>  map = null;
            for(int i= 0;i< list.size();i++){
                 map = list.get(i);
                _id = map.get("_id").toString();
                esDao.deleteDoc(index,_id);
            }

            success = true;
        }
            return success;

    }


}