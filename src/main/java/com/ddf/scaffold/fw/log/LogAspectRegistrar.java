package com.ddf.scaffold.fw.log;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author DDf on 2018/11/7
 */
public class LogAspectRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        registryLogAspect(metadata, registry);
    }

    /**
     * 注册项目是否开启了@EnableLogAspect功能
     * @param metadata
     * @param registry
     */
    private void registryLogAspect(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        boolean exist = metadata.hasAnnotation(EnableLogAspect.class.getName());
        boolean enableLogAspect = false;
        if (exist) {
            enableLogAspect = true;
            // 拦截器默认不开启，只有开启了相关功能才注入到IOC，使之生效
            if (!registry.containsBeanDefinition(AccessLogAspect.BEAN_NAME)) {
                BeanDefinitionBuilder requestContextDefinition = BeanDefinitionBuilder.
                        genericBeanDefinition(AccessLogAspect.class);
                registry.registerBeanDefinition(AccessLogAspect.BEAN_NAME,
                        requestContextDefinition.getBeanDefinition());
            }
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(LogAspectConfiguration.class);
        builder.addPropertyValue("enableLogAspect", enableLogAspect);
        registry.registerBeanDefinition(LogAspectConfiguration.BEAN_NAME, builder.getBeanDefinition());
    }
}
