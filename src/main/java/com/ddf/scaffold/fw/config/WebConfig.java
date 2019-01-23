package com.ddf.scaffold.fw.config;

import com.ddf.scaffold.fw.interceptor.LoginInterceptor;
import com.ddf.scaffold.fw.interceptor.RequestContextInterceptor;
import com.ddf.scaffold.fw.jpa.JpaBaseDaoImpl;
import com.ddf.scaffold.fw.resolver.QueryParamArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.SpringDataWebConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @author DDf on 2018/12/8
 * 框架核心配置类
 *
 * 主要这里一定要实现{@link WebMvcConfigurer}，该接口已经提供了默认实现，而且{@link @EnableSpringDataWebSupport}
 * 提供的{@link SpringDataWebConfiguration}就是实现了该接口来添加相对应的参数解析器和消息转换器，如果这里处置不当，
 * 会覆盖该注解提供的功能，如{@link PageableHandlerMethodArgumentResolver}
 *
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(value = {"com.ddf"},
		repositoryBaseClass = JpaBaseDaoImpl.class)
@EnableJpaAuditing
@EnableSpringDataWebSupport
@EnableAspectJAutoProxy
@EnableAsync
@EnableScheduling
@ComponentScan("com.ddf.scaffold")
@EntityScan("com.ddf.scaffold.fw.entity")
@EnableCaching
public class WebConfig implements WebMvcConfigurer {
	@Autowired
	private RequestContextInterceptor requestContextInterceptor;
	@Autowired
	private LoginInterceptor loginInterceptor;
	@Autowired
	private QueryParamArgumentResolver queryParamArgumentResolver;


	/**
	 * 配置允许所有请求跨域
	 * @param registry
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**");
	}

	/**
	 * 配置静态资源映射路径
	 * @param registry
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	}


	/**
	 * 配置拦截器
	 * @param registry
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(requestContextInterceptor).addPathPatterns("/**");
		registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
	}

	/**
	 * 配置自定义参数解析器
	 * @param resolvers
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(queryParamArgumentResolver);
	}

	/**
	 * 一个全局的用于格式化的工具类
	 * 如果一个方法内多次使用的话，最好还是自己new一个使用
	 * @return
	 */
	@Bean
	@Primary
	@Scope("prototype")
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}


	/**
	 * 默认线程池
	 * @return
	 */
	@Bean
	@Primary
	public Executor taskExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("default-thread-pool-%d");
		threadPoolTaskExecutor.setCorePoolSize(8);
		threadPoolTaskExecutor.setMaxPoolSize(50);
		threadPoolTaskExecutor.setKeepAliveSeconds(0);
		threadPoolTaskExecutor.setQueueCapacity(1000);
		return threadPoolTaskExecutor;
	}


	/**
	 * 定时任务调度线程池
	 * @return
	 */
	@Bean
	@Primary
	public Executor scheduledExecutorService() {
		ThreadFactory namedThreadFactory = new CustomizableThreadFactory("scheduledExecutorService-%d");
		ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(8,
				namedThreadFactory);
		scheduledExecutorService.setMaximumPoolSize(50);
		return scheduledExecutorService;
	}
}
