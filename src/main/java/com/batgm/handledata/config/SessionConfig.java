package com.batgm.handledata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author yqq
 * @createdate 2020/7/23
 *
 * maxInactiveIntervalInSeconds: 设置 Session 失效时间
 * 使用 Redis Session 之后，原 Spring Boot 中的 server.session.timeout 属性不再生效。
 *
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class SessionConfig {
}