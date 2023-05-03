package com.batgm.handledata.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yqq
 * @createdate 2020/8/4
 */
@Component
public class AddResponseHeaderFilter extends OncePerRequestFilter {
    private static Logger logger = LoggerFactory.getLogger(AddResponseHeaderFilter.class);

    private static final Map<String,String> safeUrl=new HashMap<String,String>();

    static {
        //safeUrl.put("/page/index","/page/index");
        safeUrl.put("/page/test5","/page/test5");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        //httpServletResponse.addHeader("X-Frame-Options", "SAMEORIGIN");
        httpServletResponse.addHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0");
        httpServletResponse.addHeader("Cache-Control", "no-cache='set-cookie'");
        httpServletResponse.addHeader("Pragma", "no-cache");
        //谷歌80.x版本后测试
        httpServletResponse.setHeader("Set-Cookie", "HttpOnly;Secure;SameSite=None");

        if (httpServletRequest.getMethod().equals("OPTIONS"))
        {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            httpServletResponse.setHeader("Access-Control-Allow-Methods",
                    httpServletRequest.getHeader("Access-Control-Request-Method"));
            httpServletResponse.setHeader("Access-Control-Allow-Headers",
                    httpServletRequest.getHeader("Access-Control-Request-Headers"));

        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}
