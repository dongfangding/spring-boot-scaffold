package com.ddf.scaffold.fw.config;

import com.ddf.scaffold.fw.constant.GlobalConstants;
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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @author dongfang.ding on 2018/12/8
 * 框架核心配置类
 *
 * 主要这里一定要实现{@link WebMvcConfigurer}，该接口已经提供了默认实现，而且{@link @EnableSpringDataWebSupport}
 * 提供的{@link SpringDataWebConfiguration}就是实现了该接口来添加相对应的参数解析器和消息转换器，如果这里处置不当，
 * 会覆盖该注解提供的功能，如{@link PageableHandlerMethodArgumentResolver}
 *
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(value = {GlobalConstants.BASE_PACKAGE},
		repositoryBaseClass = JpaBaseDaoImpl.class)
@EnableJpaAuditing
@EnableSpringDataWebSupport
@EnableAspectJAutoProxy
@EnableAsync
@EnableScheduling
@ComponentScan(GlobalConstants.BASE_PACKAGE)
@EntityScan(basePackages = GlobalConstants.BASE_PACKAGE)
@EnableCaching
public class WebConfig implements WebMvcConfigurer {
	@Autowired
	private RequestContextInterceptor requestContextInterceptor;
	@Autowired
	private QueryParamArgumentResolver queryParamArgumentResolver;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(0, new MappingJackson2HttpMessageConverter());
	}

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
	 *
	 * @return
	 */
	@Bean
	@Primary
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("default-thread-pool-");
		threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
		threadPoolTaskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2 + 1);
		threadPoolTaskExecutor.setKeepAliveSeconds(0);
		threadPoolTaskExecutor.setQueueCapacity(100000);
		return threadPoolTaskExecutor;
	}


	/**
	 * 连接通道队列处理线程池
	 *
	 * @return
	 */
	@Bean
	public ThreadPoolTaskExecutor transferQueueExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("channel-transfer-queue-pool-%s");
		threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
		threadPoolTaskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2 + 1);
		threadPoolTaskExecutor.setKeepAliveSeconds(60);
		threadPoolTaskExecutor.setQueueCapacity(10000);
		return threadPoolTaskExecutor;
	}


	/**
	 * 连接通道重试线程池
	 *
	 * @return
	 */
	@Bean
	public ThreadPoolTaskExecutor transferRetryExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("channel-transfer-retry-pool-%s");
		threadPoolTaskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
		threadPoolTaskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2 + 1);
		threadPoolTaskExecutor.setKeepAliveSeconds(30);
		threadPoolTaskExecutor.setQueueCapacity(10000);
		return threadPoolTaskExecutor;
	}


	/**
	 * 定时任务调度线程池
	 *
	 * @return
	 */
	@Bean
	@Primary
	public ScheduledThreadPoolExecutor scheduledExecutorService() {
		ThreadFactory namedThreadFactory = new CustomizableThreadFactory("scheduledExecutorService-");
		ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
				namedThreadFactory);
		scheduledExecutorService.setMaximumPoolSize(Runtime.getRuntime().availableProcessors() * 2 + 1);
		return scheduledExecutorService;
	}
}
