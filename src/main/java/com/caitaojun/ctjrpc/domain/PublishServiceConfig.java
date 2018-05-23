package com.caitaojun.ctjrpc.domain;

import java.util.HashMap;
import java.util.Map;
/**
 * 发布服务配置的配置类
 * @author caitaojun
 *
 */
public class PublishServiceConfig {
	private Map<String, String> publishServices = new HashMap<String, String>();
	
	private SerializableType serializableType;
	

	public SerializableType getSerializableType() {
		return serializableType;
	}

	public void setSerializableType(SerializableType serializableType) {
		this.serializableType = serializableType;
	}

	public Map<String, String> getPublishServices() {
		return publishServices;
	}

	public void setPublishServices(Map<String, String> publishServices) {
		this.publishServices = publishServices;
	}
	
}
