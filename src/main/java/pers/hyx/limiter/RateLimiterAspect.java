package pers.hyx.limiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import pers.hyx.limiter.annotation.RateLimiter;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 * @author heyouxin
 * @since 2021/7/29/0029 11:47
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RateLimiterAspect {
    private final RateLimiterUtils rateLimiterUtils;

    @Pointcut("@annotation(pers.hyx.limiter.annotation.RateLimiter)")
    public void rateLimit() {
    }

    @Around("rateLimit()")
    public Object pointcut(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimiter rateLimiter = AnnotationUtils.findAnnotation(method, RateLimiter.class);
        if (rateLimiter != null) {
            String key = rateLimiter.key();
            if (StringUtils.isEmpty(key)) {
                key = method.getDeclaringClass().getName() + "." + method.getName();
            }

            long max = rateLimiter.max();
            long timeout = rateLimiter.timeout();
            TimeUnit timeUnit = rateLimiter.timeUnit();
            boolean limited = rateLimiterUtils.shouldLimited(key, max, timeout, timeUnit);
            if (limited) {
                throw new LimiterException(key);
            }
        }
        return point.proceed();
    }
}
