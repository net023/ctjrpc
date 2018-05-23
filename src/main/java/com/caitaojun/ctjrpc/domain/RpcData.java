package com.caitaojun.ctjrpc.domain;

import java.io.Serializable;
/**
 * 传输数据的实体包装类
 * @author caitaojun
 *
 */
public class RpcData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String interfaceName;
	private String methodName;
	private Object[] args;
	public String getInterfaceName() {
		return interfaceName;
	}
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Object[] getArgs() {
		return args;
	}
	public void setArgs(Object[] args) {
		this.args = args;
	}
}
