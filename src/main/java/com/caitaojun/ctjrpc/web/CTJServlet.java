package com.caitaojun.ctjrpc.
web;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.caitaojun.ctjrpc.domain.PublishServiceConfig;
import com.caitaojun.ctjrpc.domain.RpcData;
import com.caitaojun.ctjrpc.domain.SerializableType;

/**
 * ctj-rpc的服务发布servlet
 * @author caitaojun
 *
 */
public class CTJServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	/**
	 * servlet中获取spring容器中的服务配置bean
		接收参数：
			interfaceName
			methodName
			args
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
		//获取发布服务配置bean
		PublishServiceConfig publishServiceConfig = context.getBean(PublishServiceConfig.class);
		Map<String, String> publishServices = publishServiceConfig.getPublishServices();
		SerializableType serializableType = publishServiceConfig.getSerializableType();
		if(serializableType.equals(SerializableType.JDK_Serial)){
			boolean multipartContent = ServletFileUpload.isMultipartContent(req);
			//System.out.println(multipartContent);
			DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
			ServletFileUpload fileUpload = new ServletFileUpload(diskFileItemFactory);
			try {
				InputStream inputStream = fileUpload.parseParameterMap(req).get("reqData").get(0).getInputStream();
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
				RpcData rpcData = (RpcData) objectInputStream.readObject();
				String serviceImplClassStr = publishServices.get(rpcData.getInterfaceName());
				Class<?> clazz = Class.forName(serviceImplClassStr);
				//先去spring容器里面获取是否存在了，如果存在了就获取来用，否则就新创建，并存入spring容器中
				String first = (clazz.getSimpleName().charAt(0)+"").toLowerCase();
				String beanName = first+clazz.getSimpleName().substring(1);
				boolean contains = context.containsBeanDefinition(beanName);
				if(!contains){
					ConfigurableApplicationContext configContext = (ConfigurableApplicationContext) context;
					ConfigurableListableBeanFactory configurableListableBeanFactory = configContext.getBeanFactory();
					DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableListableBeanFactory;
					AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
					bpp.setBeanFactory(defaultListableBeanFactory);
					defaultListableBeanFactory.addBeanPostProcessor(bpp);
					RootBeanDefinition beanDefinition = new RootBeanDefinition(clazz);
					beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
					defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinition);
				}
				Object serviceBean = context.getBean(clazz);
				Method[] declaredMethods = clazz.getDeclaredMethods();
				Object result = null;
				for (Method method : declaredMethods) {
					method.setAccessible(true);
					if(method.getName().equals(rpcData.getMethodName())){
						if(rpcData.getArgs()!=null){
							result = method.invoke(serviceBean,rpcData.getArgs());
						}else{
							result = method.invoke(serviceBean, new Object[0]);
						}
						break;
					}
				}
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(resp.getOutputStream());
				objectOutputStream.writeObject(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(serializableType.equals(SerializableType.JSON_Serial)){
			String reqDataJson = req.getParameter("reqData");
			//反序列化
			RpcData rpcData = JSONObject.parseObject(reqDataJson, RpcData.class);
			String serviceImplClassStr = publishServices.get(rpcData.getInterfaceName());
			//加载服务接口实现类，并调用方法（反射）
			try {
				Class<?> clazz = Class.forName(serviceImplClassStr);
				//先去spring容器里面获取是否存在了，如果存在了就获取来用，否则就新创建，并存入spring容器中
				String first = (clazz.getSimpleName().charAt(0)+"").toLowerCase();
				String beanName = first+clazz.getSimpleName().substring(1);
				boolean contains = context.containsBeanDefinition(beanName);
				if(!contains){
					ConfigurableApplicationContext configContext = (ConfigurableApplicationContext) context;
					ConfigurableListableBeanFactory configurableListableBeanFactory = configContext.getBeanFactory();
					DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableListableBeanFactory;
					AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
					bpp.setBeanFactory(defaultListableBeanFactory);
					defaultListableBeanFactory.addBeanPostProcessor(bpp);
					RootBeanDefinition beanDefinition = new RootBeanDefinition(clazz);
					beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
					defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinition);
				}
				Object serviceBean = context.getBean(clazz);
				Method[] declaredMethods = clazz.getDeclaredMethods();
				Object result = null;
				for (Method method : declaredMethods) {
					method.setAccessible(true);
					if(method.getName().equals(rpcData.getMethodName())){
						Object[] args = rpcData.getArgs();
						/*for (Object object : args) {
						System.out.println(object.getClass());
					}*/
						//如果方法参数是一个自定义对象，那么传过来的是一个JSONObject
						if(rpcData.getArgs()!=null){
							List<Object> obj = new ArrayList<Object>();
							Class<?>[] parameterTypes = method.getParameterTypes();
							for (int i = 0; i < args.length; i++) {
								if(args[i] instanceof JSONObject){
									Object object = JSONObject.toJavaObject((JSON)args[i], parameterTypes[i]);
									obj.add(object);
								}else{
									obj.add(args[i]);
								}
							}
							//					result = method.invoke(serviceBean,rpcData.getArgs());
							result = method.invoke(serviceBean,obj.toArray());
						}else{
							result = method.invoke(serviceBean, new Object[0]);
						}
						break;
					}
				}
				String jsonString = JSONObject.toJSONString(result);
				resp.getOutputStream().write(jsonString.getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	public static void main(String[] args) {
		List<String> ll = new ArrayList<String>();
		ll.add("sss");
		ll.add("aaa");
		System.out.println(JSONObject.toJSONString(ll));
	}
	
}
