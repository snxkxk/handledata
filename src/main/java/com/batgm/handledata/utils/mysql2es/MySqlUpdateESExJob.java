package com.batgm.handledata.utils.mysql2es;


import com.batgm.handledata.elasticsearch.EsDao;
import com.batgm.handledata.utils.muti.ITask;
import com.batgm.handledata.utils.muti.ResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
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
public class MySqlUpdateESExJob implements ITask<ResultBean, Object> {
    private static Logger logger = LoggerFactory.getLogger(MySqlUpdateESExJob.class);
    private DataSource dataSource;

    private EsDao esDao;


    private StringRedisTemplate stringRedisTemplate;

    public MySqlUpdateESExJob(StringRedisTemplate stringRedisTemplate, EsDao esDao, DataSource dataSource){
        this.stringRedisTemplate = stringRedisTemplate;
        this.esDao = esDao;
        this.dataSource = dataSource;
    }

    @Override
    public ResultBean execute(Object e, ConcurrentHashMap<String, Object> params) {
        /**
         * 具体业务逻辑：将list中的元素加上辅助参数中的数据返回
         */

        AtomicInteger total = (AtomicInteger) params.get("count");
        String tableName = params.get("tableName").toString();
        String index = params.get("index").toString();

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
        for (int x = 0; x < data.size(); x++) {
            int id = data.get(x);
            String indexName = tableName;
            String sql = "select * from "+tableName+" where job_id = ? ";
            try {
                st = connect.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                st.setInt(1, id);
                rs = st.executeQuery();
                ResultSetMetaData colData = rs.getMetaData();

                ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
                HashMap<String, String> map = null;
                String c = null;
                String v = null;
                String job_id=null;
                while (rs.next()) {
                    map = new HashMap<String, String>(100);
                    for (int i = 1; i <= colData.getColumnCount(); i++) {
                        c = colData.getColumnName(i);
                        v = rs.getString(c);
                        map.put(c, v);
                    }
                    dataList.add(map);
                }
                if(dataList.size()>0){
                    boolean rst = updateEsData(id+"",index,dataList);
                    if(rst){
                        ct++;
                        total.getAndIncrement();
                    }
                }
                // 将数据刷新到es, 注意这一步执行后并不会立即生效，取决于bulkProcessor设置的刷新时间
            } catch (Exception e1) {
                e1.printStackTrace();
            }
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

    private boolean  updateEsData(String id,String index,ArrayList<HashMap<String, String>> jobCommentMap){
        boolean  success = false;
        Map qmap = new HashMap();
        qmap.put("id",id);//根据job id 查询
        String _id=null;
        List<Map<String, Object>> list = esDao.searchByConditions(index,qmap,1,5);
        int size = list.size();
        if(size==1){
            Map<String,Object>  map = null;
            map = list.get(0);
            _id = map.get("_id").toString();
            //System.out.println("_ID:"+_id);
            qmap.clear();
            try {
                qmap.put("jobComments",jobCommentMap);
                qmap.put("jobCommentsCount",jobCommentMap.size());
                esDao.updateDoc(index,_id,qmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            success = true;
        }else if(size >1)
        {
            logger.warn("qmap:"+qmap);
            logger.warn("upateJobs size>1:"+size);
            for (int i= 0; i< size ;i++){
                logger.warn(list.get(i).toString());
            }
        }else {
            logger.warn("qmap:"+qmap);
            logger.warn("upateJobs size:"+size);
        }
            return success;

    }


}