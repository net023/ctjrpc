package com.caitaojun.ctjrpc.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.caitaojun.ctjrpc.domain.SerializableType;
/**
 * 注册接口代理对象到spring容器中
 * @author caitaojun
 *
 */
public class RegistServiceBean implements ApplicationContextAware,BeanDefinitionRegistryPostProcessor{
	private String serviceHost;
	private SerializableType serializableType;
	private List<Class> serviceInterfaceClass = new ArrayList<Class>();
	private ApplicationContext applicationContext;
	
	public SerializableType getSerializableType() {
		return serializableType;
	}
	public void setSerializableType(SerializableType serializableType) {
		this.serializableType = serializableType;
	}
	public String getServiceHost() {
		return serviceHost;
	}
	public void setServiceHost(String serviceHost) {
		this.serviceHost = serviceHost;
	}
	public void setServiceInterfaceClass(List<Class> serviceInterfaceClass) {
		this.serviceInterfaceClass = serviceInterfaceClass;
	}
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		
	}
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		for (Class clazz : serviceInterfaceClass) {
			BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
			GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
			definition.getPropertyValues().add("interfaceClass", definition.getBeanClassName())
				.add("serviceHost", getServiceHost()).add("serializableType", getSerializableType());
			definition.setBeanClass(ProxyFactoryBean.class);
			definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
			// 注册bean名,一般为类名首字母小写
			String beanName = (clazz.getSimpleName().charAt(0)+"").toLowerCase()+clazz.getSimpleName().substring(1);
			registry.registerBeanDefinition(beanName, definition);
		}
	}
}
