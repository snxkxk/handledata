package com.batgm.handledata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableCaching
@EnableSwagger2
@MapperScan("com.batgm.handledata.dao")
@ServletComponentScan("com.batgm.handledata.filter")
@EnableAspectJAutoProxy
public class HandleDataApplication {
    public static void main(String[] args) {
        //System.setProperty("es.set.netty.runtime.available.processors","false");
        SpringApplication.run(HandleDataApplication.class,args);
    }
}
