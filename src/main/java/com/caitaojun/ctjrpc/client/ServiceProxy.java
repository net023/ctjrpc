package com.caitaojun.ctjrpc.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.caitaojun.ctjrpc.domain.RpcData;
import com.caitaojun.ctjrpc.domain.SerializableType;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
/**
 * 客户端代理增强类
 * @author caitaojun
 *
 */
public class ServiceProxy implements InvocationHandler{
	
	private Class<?> interfaceClass;
	private String serviceHost;
	private SerializableType serializableType;
	
	public Object bind(Class<?> clazz, String serviceHost, SerializableType serializableType){
		this.interfaceClass = clazz;
		this.serviceHost = serviceHost;
		this.serializableType = serializableType;
		return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {interfaceClass}, this);
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		RpcData rpcData = new RpcData();
		rpcData.setInterfaceName(interfaceClass.getName());
		rpcData.setMethodName(method.getName());
		rpcData.setArgs(args);
		if(serializableType.equals(SerializableType.JDK_Serial)){
			System.out.println("jdk_serial...");
			File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".ctj");
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile));
			oos.writeObject(rpcData);
			HttpResponse send = HttpRequest.post(serviceHost).form("reqData", tempFile).send();
			byte[] bodyBytes = send.bodyBytes();
			oos.close();//关闭io流，否则后面文件删除不了
			tempFile.delete();
			ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bodyBytes);
			ObjectInputStream objectInputStream = new ObjectInputStream(arrayInputStream);
			return objectInputStream.readObject();
		}else if(serializableType.equals(SerializableType.JSON_Serial)){
			System.out.println("json_serial...");
			String rpcDataJson = JSONObject.toJSONString(rpcData);
			String responseBody = HttpRequest.post(serviceHost).form("reqData",rpcDataJson).send().body();
			//获取method的返回类型
			Class<?> returnType = method.getReturnType();
			//如果是集合，获取集合中的泛型  java.util.List<com.net023.domain.User>
			if(List.class.isAssignableFrom(returnType)){
				String genericClassName = method.getGenericReturnType().toString().substring(returnType.getName().length()+1);
				genericClassName = genericClassName.substring(0, genericClassName.length()-1);
				Class<?> clazz = Class.forName(genericClassName);
				List<?> result = JSONArray.parseArray(responseBody, clazz);
				return result;
			}else{
				Object object = JSONObject.parseObject(responseBody, returnType);
				return object;
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		ArrayList<String> ss = new ArrayList<String>();
		System.out.println(List.class.isAssignableFrom(ss.getClass()));
		
		SerializableType serializableType = SerializableType.JDK_Serial;
		System.out.println(serializableType.equals(SerializableType.JDK_Serial));
		System.out.println(serializableType == SerializableType.JDK_Serial);
		System.out.println(serializableType.equals("JDK_Serial"));
		
	}
	
}
