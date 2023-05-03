package com.batgm.handledata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

/**
 * @author yqq
 * @createdate 2020/8/10
 */
@EnableWebSecurity //启用Spring Security.
@EnableGlobalMethodSecurity(prePostEnabled=true)
@Configuration
public class WebSecurityConfig extends
        WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().sameOrigin().disable();

        http.csrf().ignoringAntMatchers("/druid/*");

        //POST请求403解决
        http.cors().and().csrf().disable();

        //SpringSecurity
        http
                .authorizeRequests()
                .antMatchers("/","/index").permitAll()
                .antMatchers("/login").permitAll() //允许所有人可以访问登录页面.
                .antMatchers("/user/login","/page/test","/page/index","/redis/*").permitAll()
                .antMatchers("/js/**","/plugins/**","/images/**","/css/**","/favicon.ico").permitAll()//这个允许/res下.js和.html文件可以直接访问。
                //.anyRequest().authenticated()// 所有的请求需要在登录之后才能够访问。
                //.anyRequest().access("@authService.canAccess(request,authentication)")
                .and().sessionManagement().maximumSessions(1)
        ;
    }


    //一些其他的配置
    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    @Bean //注入PsswordEncoder
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }
}