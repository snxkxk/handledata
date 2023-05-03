package com.batgm.handledata.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
 
import com.mchange.v2.c3p0.ComboPooledDataSource;
 
public class C3P0Inner {
	
	private static ComboPooledDataSource ds;
	private static final String url="jdbc:mysql://192.168.8.16:3306/oel_project_action_1031";
	private static final String driver="com.mysql.cj.jdbc.Driver";
	private static final String username="cj";
	private static final String password="cj@1qaz";
	
	//静态初始化块进行初始化
	static{
		try {
			ds = new ComboPooledDataSource();//创建连接池实例
			
			ds.setDriverClass(driver);//设置连接池连接数据库所需的驱动
			
			ds.setJdbcUrl(url);//设置连接数据库的URL
			
			ds.setUser(username);//设置连接数据库的用户名
			
			ds.setPassword(password);//设置连接数据库的密码
			
			ds.setMaxPoolSize(40);//设置连接池的最大连接数
			
			ds.setMinPoolSize(2);//设置连接池的最小连接数
			
			ds.setInitialPoolSize(10);//设置连接池的初始连接数
			
			ds.setMaxStatements(100);//设置连接池的缓存Statement的最大数	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//获取与指定数据库的连接
	public static ComboPooledDataSource getInstance(){
		return ds;
	} 
 
	//从连接池返回一个连接
	public static Connection getConnection(){
		Connection conn = null;
		try {
			conn = ds.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	//释放资源
	public static void realeaseResource(ResultSet rs,PreparedStatement ps,Connection conn){
		if(null != rs){
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(null != ps){
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
 
		try {
			if(conn!=null){
				conn.close();	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

