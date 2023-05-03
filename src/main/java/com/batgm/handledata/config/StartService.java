package com.batgm.handledata.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 服务启动完成后执行
 * @author yqq
 * @createdate 2020/6/22
 */
@Component
public class StartService implements CommandLineRunner {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("####spring boot cache *~lock####");
        //System.out.println("*~lock keys:"+stringRedisTemplate.keys("*~lock"));
        System.out.println("####spring boot cache *~lock####");
    }
}


