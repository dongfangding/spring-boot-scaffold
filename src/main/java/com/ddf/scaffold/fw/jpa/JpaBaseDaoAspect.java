package com.ddf.scaffold.fw.jpa;

import com.ddf.scaffold.fw.session.RequestContext;
import lombok.Setter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>底层的{@link JpaBaseDao}由于不能交给{@code Spring}管理，所以获取不到{@link RequestContext}和{@link SessionContext},
 * 只能由这里来处理每次需要使用到底层查询功能时，获取当前的对象信息然后通过反射将值传递下去</p>
 * <p>
 *     存在疑问？是否存在线程安全问题，暂时已使用{@code ThreadLocal}来封装对象；是否耗费性能，每次调用底层方法就必须反射
 *     对变量赋值；如果一个方法中使用了两个底层方法，那么就要重复赋值与释放两次。而且还是针对的同一个对象；
 * </p>
 * @author dongfang.ding on 2019/1/17
 */
@Component
//@Aspect
public class JpaBaseDaoAspect {
    @Autowired(required = false)
    @Setter
    private RequestContext requestContext;
    @Autowired(required = false)

    public void boundThreadScope(ThreadLocal<RequestContext> requestLocal) {
        this.requestContext = requestLocal.get();
    }


    @Pointcut(value = "execution(public * com.hitisoft.hidoc.fw.jpa.JpaBaseDao.*(..))")
    public void pointcut() {}

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        setLocalVariable(joinPoint);
    }

    /**
     * 设置RequestContext和SessionContext的值
     * @param joinPoint
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void setLocalVariable(JoinPoint joinPoint) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> target = joinPoint.getTarget().getClass();
        Method setRequestContext = target.getDeclaredMethod("setRequestContext", ThreadLocal.class);
        ThreadLocal<RequestContext> localRequest = new ThreadLocal<>();
        localRequest.set(requestContext);
        setRequestContext.invoke(joinPoint.getTarget(), localRequest);
    }


    @After("pointcut()")
    public void after(JoinPoint joinPoint) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        releaseLocalVariable(joinPoint);
    }

    /**
     * 处理完成后，释放ThreadLocal变量
     * @param joinPoint
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void releaseLocalVariable(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> target = joinPoint.getTarget().getClass();
        Method getRequestContext = target.getDeclaredMethod("getRequestContext");
        ThreadLocal<RequestContext> localRequest = (ThreadLocal<RequestContext>) getRequestContext.invoke(joinPoint.getTarget());
        localRequest.remove();
    }
}
