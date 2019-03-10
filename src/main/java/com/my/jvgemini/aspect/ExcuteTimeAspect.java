package com.my.jvgemini.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author zhe.sun
 * @Description: TODO
 * @date 2019/2/19 21:51
 */
@Aspect
@Slf4j
@Component
public class ExcuteTimeAspect {

    @Around("@annotation(excuteTime)")
    public Object excuteTime(ProceedingJoinPoint joinPoint, ExcuteTime excuteTime) {
        Object result = null;
        long startTime = System.currentTimeMillis();
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            log.warn("Around Advice Exception : " + throwable.getMessage());
        }
        long endTime = System.currentTimeMillis();
        log.info(excuteTime.param() + joinPoint + " excute time : " + (endTime - startTime));
        return result;
    }
}