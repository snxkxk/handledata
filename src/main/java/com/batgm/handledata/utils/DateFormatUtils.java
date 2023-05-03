package com.batgm.handledata.utils;

import org.joda.time.Interval;
import org.joda.time.Period;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 日期处理类
 * @author zhanglin
 *
 */
public class DateFormatUtils {

	public static int getSecond(){
        String secondStr = String.valueOf(System.currentTimeMillis());
        String second = secondStr.substring(0,secondStr.length()-3);
        return Integer.valueOf(second);
    }
     
    public static String getData(long time){
        time = time * 1000 ;
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        String format = sdf.format(new Date(time));
        return format;
    }
     
    public static String getDateShout(long time){
         time = time * 1000 ;
            SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");  
            String format = sdf.format(new Date(time));
            return format;
    }
     
    public static long getDateByLong(){
         
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");  
        long time=0;
        try {
             
            Calendar c = Calendar.getInstance();  
            c.add(Calendar.DATE, - 7);  
            Date monday = c.getTime();
            String preMonday = sdf.format(monday);
             
            time=sdf.parse(preMonday).getTime()/1000;
              
              
        } catch (ParseException e) {
             
            e.printStackTrace();
        }
        return time;
         
    }
	
    public static String getData(Date date){
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        String format = sdf.format(date);
        return format;
    }
    
    /**
     * 获取两个时间相差的天数
     * */
    public static Integer getDays(Date smdate,Date bdate) throws ParseException{
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	smdate = sdf.parse(sdf.format(smdate));
    	bdate = sdf.parse(sdf.format(bdate));
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(smdate);
    	long time1 = cal.getTimeInMillis();
    	cal.setTime(bdate);
    	long time2 = cal.getTimeInMillis();
    	long between_days = (time2-time1)/(1000*3600*24);
    	return Integer.parseInt(String.valueOf(between_days));
    }
    
    public static int compare_date(Date date1,Date date2){
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    	try {
			Date dt1 = df.parse(df.format(date1));
			Date dt2 = df.parse(df.format(date2));
			if(dt1.getTime() > dt2.getTime()){
				return 1;
			}else if(dt1.getTime()<dt2.getTime()){
				return -1;
			}else{
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return 0;
    }
    public static int compare_date_all(Date date1,Date date2){
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	try {
			Date dt1 = df.parse(df.format(date1));
			Date dt2 = df.parse(df.format(date2));
			if(dt1.getTime() > dt2.getTime()){
				return 1;
			}else if(dt1.getTime()<dt2.getTime()){
				return -1;
			}else{
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return 0;
    }

    /**
     * 比较两个时间差date1<=date2
     * @param date1
     * @param date2
     * @return map  年月日时分秒
     */
    public static Map<String,Integer> compareDate(Date date1, Date date2) {
        Interval interval = new Interval(date1.getTime(),date2.getTime());
        Period period = interval.toPeriod();
        Map<String,Integer> returnMap=new HashMap<String,Integer>();
        returnMap.put("year",period.getYears());
        returnMap.put("month",period.getMonths());
        returnMap.put("day",period.getDays());
        returnMap.put("hour",period.getHours());
        returnMap.put("minute",period.getMinutes());
        returnMap.put("second",period.getSeconds());
        return returnMap;
    }
}
