package com.batgm.handledata.handlerexception;

/**
 * @author yqq
 * @createdate 2020/8/4
 */


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

@Component
public class MvcExceptionResolver  implements HandlerExceptionResolver{
    private   Logger logger = Logger.getLogger(MvcExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception ex) {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String errorMsg="飞了。。。。";
        ModelAndView mv = new ModelAndView("/error/500.html");
        mv.addObject("errorMsg", errorMsg);
        return mv ;


    }

}


