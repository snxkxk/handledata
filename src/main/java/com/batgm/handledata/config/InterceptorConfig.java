package com.batgm.handledata.config;

/**
 * @author yqq
 * @createdate 2020/7/23
 */

import com.batgm.handledata.interceptor.UserLoginInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.charset.Charset;
import java.util.List;

@Configuration
public class InterceptorConfig extends WebMvcConfigurationSupport {
    private static Logger logger = LoggerFactory.getLogger(InterceptorConfig.class);


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserLoginInterceptor())
                //添加需要验证登录用户操作权限的请求
                .addPathPatterns("/**")
                //这里add为“/**”,下面的exclude才起作用，且不管controller层是否有匹配客户端请求，拦截器都起作用拦截
//                .addPathPatterns("/hello")
                //如果add为具体的匹配如“/hello”，下面的exclude不起作用,且controller层不匹配客户端请求时拦截器不起作用
                //排除不需要验证登录用户操作权限的请求
                .excludePathPatterns("/user/login","/page/test","/page/index","/redis/*")
                .excludePathPatterns("/js/**","/plugins/**","/images/**","/css/**","/favicon.ico");

        //这里可以用registry.addInterceptor添加多个拦截器实例，后面加上匹配模式
        super.addInterceptors(registry);//最后将register往这里塞进去就可以了
    }
    /**
     * 添加主页方法
     *
     * @param registry 主页注册器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        logger.info("开启拦截器");
        //设置主页
        registry.addViewController("/").setViewName("index");
        //设置优先级
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        //将主页注册器添加到视图控制器中
        super.addViewControllers(registry);
    }
    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        return new StringHttpMessageConverter(Charset.forName("UTF-8"));
    }
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(responseBodyConverter());
        // 这里必须加上加载默认转换器，不然bug玩死人，并且该bug目前在网络上似乎没有解决方案
        // 百度，谷歌，各大论坛等。你可以试试去掉。
        addDefaultHttpMessageConverters(converters);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/")
                .addResourceLocations("classpath:/static/");
    }

}