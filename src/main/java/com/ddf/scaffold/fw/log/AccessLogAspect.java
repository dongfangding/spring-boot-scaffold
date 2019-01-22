package com.ddf.scaffold.fw.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * 拦截指定的请求为某些注解功能提供支持，目前支持功能如下
 * <ul>
 *     <li>{@link EnableLogAspect}</li>
 * </ul>
 * 
 * @author DDf on 2018/10/9
 */
@Aspect
public class AccessLogAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String BEAN_NAME = "accessLogAspect";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LogAspectConfiguration logAspectConfiguration;

    @Pointcut(value = "execution(public * com..controller..*(..))")
    public void pointCut() {}

    /**
     * before处理日志和封装用户信息
     * @param joinPoint
     */
    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        logBefore(joinPoint);
    }

    /**
     * 请求成功执行并返回值后后打印日志
     * @param joinPoint
     * @param result
     */
    @AfterReturning(pointcut = "pointCut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        logAfter(joinPoint, result, "方法执行结束，成功返回值");
    }

    /**
     * 请求出现异常打印日志
     * @param joinPoint
     * @param exception
     */
    @AfterThrowing(pointcut = "pointCut()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Exception exception) {
        logAfter(joinPoint, exception, "方法执行出现异常");
    }



    /**
     * 记录方法入参
     * @param joinPoint {@link JoinPoint}
     */
    private void logBefore(JoinPoint joinPoint) {
        if (!logAspectConfiguration.isEnableLogAspect()) {
            return;
        }
        Map<String, Object> paramsMap = new HashMap<>();
        String className = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        String str = "";
        if (parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; i++) {
                String value = joinPoint.getArgs()[i] != null ? joinPoint.getArgs()[i].toString() : "null";
                paramsMap.put(parameterNames[i], value);
            }
            try {
                str = objectMapper.writeValueAsString(paramsMap);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        logger.info("[{}.{}方法执行，参数列表===>({})]", className, methodName, str);
    }

    private void logAfter(JoinPoint joinPoint, Object result, String message) {
        if (!logAspectConfiguration.isEnableLogAspect()) {
            return;
        }
        String className = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        logger.info("[{}.{}]{}..........: ({})", className, methodName, message, result != null ? result.toString() : "");
    }

}
