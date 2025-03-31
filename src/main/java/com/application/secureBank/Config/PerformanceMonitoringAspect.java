package com.application.secureBank.Config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Aspect for monitoring and logging execution time of service methods
 * Only active in non-production environments
 */
@Aspect
@Configuration
@Slf4j
@Profile("!prod")
public class PerformanceMonitoringAspect {

    /**
     * Logs execution time for all service methods
     * Highlights methods that take longer than 500ms as potentially problematic
     */
    @Around("execution(* com.application.secureBank.Services.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - start;

            // Highlight slow methods
            if (executionTime > 500) {
                log.warn("SLOW EXECUTION: {} completed in {} ms", methodName, executionTime);
            } else {
                log.debug("{} completed in {} ms", methodName, executionTime);
            }
        }
    }
}