package com.example.demo.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ApiLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingAspect.class);

    @Around(
        "execution(* com.example.demo.api..*.*(..)) || " +
        "execution(* com.example.demo.service..*.*(..)) || " +
        "execution(* com.example.demo.HealthController.*(..))"
    )
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        var signature = pjp.getSignature().toShortString();
        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("API OK: {} ({} ms)", signature, elapsed);
            return result;
        } catch (Exception ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("API NG: {} ({} ms) {}", signature, elapsed, ex.getMessage());
            throw ex;
        }
    }
}
