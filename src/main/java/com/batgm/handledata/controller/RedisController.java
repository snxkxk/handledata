package com.batgm.handledata.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.batgm.handledata.elasticsearch.BulkProcessorUtils;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 操作记录表
 * */
@Controller
@RequestMapping("/redis")
public class RedisController {


	@Autowired
	private StringRedisTemplate stringRedisTemplate;


	private  final Logger logger = LoggerFactory.getLogger(RedisController.class);

	/**
	 * 通过key查看redis数据
	 * */
	@RequestMapping(value = "getByKey", method = RequestMethod.GET)
	@ResponseBody
	public String getRedisByKey(HttpServletRequest request,
                                HttpServletResponse response, @RequestParam(value="key",required=true)String key) {
		Object data=null;
		if(stringRedisTemplate.hasKey(key)){
			HashOperations<String, Object, Object> ops = stringRedisTemplate	.opsForHash();
			stringRedisTemplate.getExpire(key);
			data=ops.get(key,key)+"(Expire:"+stringRedisTemplate.getExpire(key)+")";
		}else{
			data=key+"不存在";
		}
		return JSON.toJSONString(data);

	}

	/**
	 *
	 * @param key 设置缓存主键
	 * @param type 设置倒计时 put  获取倒计时 get
	 * @param expire 倒计时时长（小时）
	 * @return expire 秒
	 */
	@RequestMapping(value = "getCountdown", method = RequestMethod.GET)
	@ResponseBody
	public String getCountdown(@RequestParam(value="key",required=true)String key,@RequestParam(value="type",required=false)String type,@RequestParam(value="expire",required=false)Long expire){
		key="countdown:"+key;
		long time =0;
		if(expire == null){
			expire = 1L;
		}
		String msg = null;
		JSONObject json = new JSONObject();
		HashOperations<String, Object, Object> ops = stringRedisTemplate.opsForHash();
		if("get".equals(type)){
			if(stringRedisTemplate.hasKey(key)){
				time = stringRedisTemplate.getExpire(key);
			}
			msg = "获取成功";
		}else if("put".equals(type)){
			ops.put(key, key, key);
			stringRedisTemplate.expire(key, expire, TimeUnit.HOURS);
			time = expire*3600;
			msg = "设置成功";
		}
		else{
			msg = "参数有误";
		}
		json.put("expire",time);
		json.put("msg",msg);
		return json.toString();
	}

	/**
	 * 通过key删除redis数据
	 * */
	@RequestMapping(value = "deleteByKey", method = RequestMethod.GET)
	@ResponseBody
	public String deleteByKey(HttpServletRequest request,
                              HttpServletResponse response, @RequestParam(value="key",required=true)String key) {
		Object data=null;
		if(stringRedisTemplate.hasKey(key)){
			HashOperations<String, Object, Object> ops = stringRedisTemplate	.opsForHash();
			data=ops.get(key,key);
			ops.delete(key,key);
			data = " 删除成功("+key+"):"+data;
		}else{
			data=key+"不存在";
		}
		return JSON.toJSONString(data);

	}

	@RequestMapping(value = "deleteByKeys", method = RequestMethod.GET)
	@ResponseBody
	public String deleteByKeys( @RequestParam(value="keys",required=true)String keys,@RequestParam(value="op",required=false)String op) {
		Set data=stringRedisTemplate.keys(keys);
		if(data.size()>0){
			if("del".equals(op)){
				stringRedisTemplate.delete(data);
				return "删除成功["+data.size()+"]条"+JSON.toJSONString(data);
			}else{
				return "查询成功["+data.size()+"]条"+JSON.toJSONString(data);
			}
		}else {
			return "未匹配到相关数据："+keys;
		}
	}



	/**
	 * 通过key删除redis数据
	 * */
	@RequestMapping(value = "save", method = RequestMethod.GET)
	@ResponseBody
	public String save(HttpServletRequest request,
                       HttpServletResponse response, @RequestParam(value="key",required=true)String key, @RequestParam(value="value",required=true)String value, @RequestParam(value="expire",required=false)Long expire) {
			HashOperations<String, Object, Object> ops = stringRedisTemplate	.opsForHash();
		    ops.put(key, key, value);
		    Long timeout=20L;
		    if(expire!=null){
				timeout = expire;
			}
		stringRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
		return JSON.toJSONString("保存成功");

	}

	/**
	 * 多线程压力测试Mongdb insert
	 *
	 * @param total
	 * @param pool
	 * @return
	 */
	@GetMapping("stressTestingRedis")
	@ResponseBody
	public String stressTestingRedis( Integer total, Integer pool,Long expire) {
		long startTime = System.currentTimeMillis(); //获取结束时间
		String str = null;
		if (total == null) {
			total = 1000;
		}
		if (pool == null) {
			pool = 5;
		}
		if (expire == null) {
			expire = 10000L;
		}
		BulkProcessorUtils.stressTestingRedis(stringRedisTemplate,expire,total, pool);
		long endTime = System.currentTimeMillis(); //获取结束时间
		long time = (endTime - startTime);
		if (time > 1000) {
			str = String.format("执行成功,写入 %d 条数据,启用 %d 个线程，总耗时 %d 秒[%d毫秒],并发#%d#", total, pool, time / 1000, time, total / (time / 1000));
		} else {
			str = "执行完成，时间低于一秒，无法计算具体数据";
		}
		return str;
	}

	public static void main(String[] args) {
		//ip 端口
		Jedis jedis = new Jedis("127.0.0.1", 6379);
		//密码
		//jedis.auth("mypassword");
		//测试连接
		String ping = jedis.ping();
		System.out.println(ping);
		//jedis 使用数据库1
		jedis.select(0);
/*		//字符串存取
		String set = jedis.set("k1", "k2");
		System.out.println(set);
		String k1 = jedis.get("k1");
		System.out.println(k1);
		//不存在的键
		System.out.println(jedis.get("k5"));
		//键是否存在
		System.out.println(jedis.exists("k5"));*/
		//键遍历
		Set<String> keys = jedis.keys("*a*");
		Pattern pattern = Pattern.compile("^[0-9]+[a][0-9]+$");
		Matcher matcher = null;
		String value = null;
		int i = 0;

		int j = 0;
		int x = 0;
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			matcher = pattern.matcher(key);
			if(matcher.find()){
				value = jedis.get(key);
				if(value.startsWith("\"2020-07-15")){
                    //jedis.set("new_"+key,new Date().toString());
					i++;
				}else if(value.startsWith("\"2020-07-23")){
					System.out.println(key);
					System.out.println(value);
                   j++;
				}else{
                   x++;
				}
			}

		}
		System.out.println("2020-07-23 共："+j);
		System.out.println("2020-07-15 共："+i);
		System.out.println("其他 共："+i);
/*		//批量存取
		jedis.mset("test-redis-str1","v1","test-redis-str2","v2","test-redis-str3","v3");
		System.out.println(jedis.mget("test-redis-str1","test-redis-str2","test-redis-str3"));
		//set
		jedis.sadd("orders","jd001");
		jedis.sadd("orders","jd002");
		jedis.sadd("orders","jd003");
		Set<String> set1 = jedis.smembers("orders");
		for (Iterator iterator = set1.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
		}
		jedis.srem("orders","jd002");
		Set<String> set2 = jedis.smembers("orders");
		for (Iterator iterator = set2.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
		}
		//hash
		jedis.hset("hash1","userName","lisi");
		System.out.println(jedis.hget("hash1","userName"));
		Map<String,String> map = new HashMap<String,String>();
		map.put("telphone","13810169999");
		map.put("address","atguigu");
		map.put("email","abc@163.com");
		jedis.hmset("hash2",map);
		List<String> result = jedis.hmget("hash2", "telphone","email");
		for (String element : result) {
			System.out.println(element);
		}
		//zset
		jedis.zadd("zset01",60d,"v1");
		jedis.zadd("zset01",70d,"v2");
		jedis.zadd("zset01",80d,"v3");
		jedis.zadd("zset01",90d,"v4");
		Set<String> s1 = jedis.zrange("zset01",0,-1);
		for (Iterator iterator = s1.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			System.out.println(string);
		}*/

	}

}
