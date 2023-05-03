package com.batgm.handledata.utils.es2mysql;


import com.alibaba.fastjson.JSONObject;
import com.batgm.handledata.elasticsearch.EsDao;
import com.batgm.handledata.utils.muti.ITask;
import com.batgm.handledata.utils.muti.ResultBean;

import java.util.*;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
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
public class ESToMySqlEx implements ITask<ResultBean, Object> {
    private static Logger logger = LoggerFactory.getLogger(ESToMySqlEx.class);
    private DataSource dataSource;

    private EsDao esDao;

    private StringRedisTemplate stringRedisTemplate;


    public ESToMySqlEx(StringRedisTemplate stringRedisTemplate, EsDao esDao, DataSource dataSource){
        this.stringRedisTemplate = stringRedisTemplate;
        this.dataSource = dataSource;
        this.esDao = esDao;
    }

    @Override
    public ResultBean execute(Object e, ConcurrentHashMap<String, Object> params) {
        AtomicInteger total = (AtomicInteger) params.get("count");
        String index = params.get("tableName").toString();
        int pageSize = (int)params.get("pageSize");
        List<Integer> data = (List) e;
        Connection connect = null;
        try {
            connect = dataSource.getConnection();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        PreparedStatement st = null;
        int pageNum = 1;
        int ct= 0;
        Map tableMap = getFieldMap(index,connect);
        Map fieldMap = (Map) tableMap.get("properties");
        String sql = tableMap.get("SQL").toString();
        for(int i = 0 ;i < data.size();i++){
        pageNum = data.get(i);
        Map qmap = (Map) params.get("qmap");
        List<Map<String, Object>> list = esDao.searchByConditions(index,qmap,pageNum,pageSize);
        try {
            st = connect.prepareStatement(sql);
            connect.setAutoCommit(false);
            for(Map map:list){
                try {
                    dataFormat(st,map,index, connect,fieldMap);
                } catch (Exception e1) {
                    logger.error("批量插入单条出错抛弃,数据:{},异常:{}",map.toString(),e);
                    continue;
                }
                st.addBatch();
            }
            //执行批量操作
            st.executeBatch();
            //提交事务
            connect.commit();
            ct += list.size();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        }
        try {
            st.close();
            connect.close();

        } catch (Exception e2) {
            e2.printStackTrace();
            logger.error(e2.getMessage());
        }
        ResultBean resultBean = ResultBean.newInstance();
        resultBean.setData(data);
        resultBean.setCode(ct);
        total.set(total.intValue()+ct);
        return resultBean;
    }

    private  void dataFormat(PreparedStatement  statement, Map<String,Object> data,String tableName,Connection connect,Map fieldMap) throws SQLException {
        Iterator<Entry<String, Integer>> it = fieldMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Integer> entry = it.next();
            Integer index = entry.getValue();
            String key = entry.getKey();
            statement.setObject(index, data.get(key));
        }
    }
    public static   Map<String, Object> getFieldMap(String tableName,Connection connect){
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Map<String, Integer> properties = new HashMap<String, Integer>();


        StringBuffer col = new StringBuffer();
        StringBuffer ps = new StringBuffer();
                List<String> columns = getColumnNames(tableName,connect);
        for(int i=0;i< columns.size();i++){
            properties.put(columns.get(i), i+1);
            col.append(columns.get(i)+",");
            ps.append("?,");
        }
        String sql = "INSERT INTO "+tableName+" ("+col.toString().substring(0,col.length()-1)+ ") VALUES ("+ps.toString().substring(0,ps.length()-1)+ ");";
        returnMap.put("properties",properties);
        returnMap.put("SQL",sql);
        return  returnMap;

    }

    /**
     * 获取表中所有字段名称
     * @param tableName 表名
     * @return
     */
    public static List<String> getColumnNames(String tableName,Connection connect) {
        List<String> columnNames = new ArrayList<>();
        //与数据库的连接
        PreparedStatement pStemt = null;
        String tableSql = "select * from " + tableName + " limit 0,1 ;";
        try {
            pStemt = connect.prepareStatement(tableSql);
            //结果集元数据
            ResultSetMetaData rsmd = pStemt.getMetaData();
            //表列数
            int size = rsmd.getColumnCount();
            for (int i = 0; i < size; i++) {
                columnNames.add(rsmd.getColumnName(i + 1));
            }
        } catch (SQLException e) {
            logger.error("getColumnNames failure", e);
        }finally {
            try {
                pStemt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return columnNames;
    }
}