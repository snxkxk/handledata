package com.batgm.handledata.interceptor;

/**
 * @author yqq
 * @createdate 2020/7/23
 */
import com.batgm.handledata.entity.StudyActionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Component
public class UserLoginInterceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(UserLoginInterceptor.class);

    long start = System.currentTimeMillis();
    //preHandle是在请求执行前执行的
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        start = System.currentTimeMillis();
        boolean hasData = false;

        StudyActionRecord record = (StudyActionRecord) request.getSession().getAttribute("record");
        if(record!=null){
            System.out.println("session Record:"+record.toString());
            //System.out.println(request.getParameter("loginUsername"));
            //System.out.println(request.getParameter("loginPassword"));
            hasData = true;
        }
        else {
            response.sendRedirect(request.getContextPath()+"/page/index");
        }

        return hasData;
        // 否则false为拒绝执行，起到拦截器控制作用
    }

    //postHandler是在请求结束之后,视图渲染之前执行的,但只有preHandle方法返回true的时候才会执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        System.out.println("Interception cost="+(System.currentTimeMillis()-start));
    }

    //afterCompletion是视图渲染完成之后才执行,同样需要preHandle返回true，
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        //该方法通常用于清理资源等工作
        System.out.println("afterCompletion.......");

    }
}