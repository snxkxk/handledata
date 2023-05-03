package com.batgm.handledata.annotation.aspect;

import com.batgm.handledata.annotation.HandlingTime;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author yqq
 * @createdate 2020/9/18
 */

@Aspect
@Component
public class HandlingTimeAspect {
    private  final Logger logger = LoggerFactory.getLogger(HandlingTimeAspect.class);


    @Pointcut("@annotation(com.batgm.handledata.annotation.HandlingTime)")
    public void handlingTimePointcut() {}

    @Around("handlingTimePointcut()&& @annotation(handlingTime)")
    public Object handlingTimeAround(ProceedingJoinPoint joinPoint, HandlingTime handlingTime){
        try {
            long startTime = System.currentTimeMillis();
            Object proceed = joinPoint.proceed();
            logger.info("########HandlingTime["+handlingTime.value()+"]########");
            logger.info("#方法执行时间:" + (System.currentTimeMillis() - startTime)+"MS#");
            return proceed;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }
}
