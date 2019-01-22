package com.ddf.scaffold.fw.log;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 *
 * <p>是否开启对当前应用控制层的方法的日志记录功能，目前没有细分哪些方法需要记录，哪些不需要记录，一旦开启，被拦截的
 * 控制层的代码都会被记录,如果以后需要，会看情况添加</p>
 * <p>需要在配置类上加上注解{@code @EnableLogAspect}使用如下:</>
 * <pre class="code">
 *     &#064;Configuration
 *     &#064;EnableLogAspect
 *     public class Config {
 *
 *     }
 * </pre>
 *
 * @see AccessLogAspect
 * @author DDf on 2018/11/7
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {LogAspectRegistrar.class})
public @interface EnableLogAspect {
}
