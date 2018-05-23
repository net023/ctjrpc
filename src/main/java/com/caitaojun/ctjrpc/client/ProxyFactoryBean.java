package com.caitaojun.ctjrpc.client;

import org.springframework.beans.factory.FactoryBean;

import com.caitaojun.ctjrpc.domain.SerializableType;
/**
 * 代理工厂类
 * @author caitaojun
 *
 */
public class ProxyFactoryBean<T> implements FactoryBean<T>{
	private Class<T> interfaceClass;
	private String serviceHost;
	private SerializableType serializableType;
	
	public void setServiceHost(String serviceHost) {
		this.serviceHost = serviceHost;
	}
	
	public void setInterfaceClass(Class<T> interfaceClass) {
		this.interfaceClass = interfaceClass;
	}
	

	public void setSerializableType(SerializableType serializableType) {
		this.serializableType = serializableType;
	}

	public T getObject() throws Exception {
		return (T) new ServiceProxy().bind(interfaceClass,serviceHost,serializableType);
	}

	public Class<?> getObjectType() {
		return interfaceClass;
	}

	public boolean isSingleton() {
		return true;
	}
	
}
