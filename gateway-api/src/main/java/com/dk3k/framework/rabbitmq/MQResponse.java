package com.dk3k.framework.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author lait.zhang@gmail.com
 * @Date 2016年8月19日-下午5:38:22
 * @Description:<br>
 * 					统一返回值,可描述失败细节 </br>
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MQResponse {
	boolean isSuccess;
	String errMsg;
	
	/**
	T data;
	public MQResult(boolean isSuccess, String errMsg) {
		super();
		this.isSuccess = isSuccess;
		this.errMsg = errMsg;
	}
	public MQResult() {
		super();
	}*/

	
	
}
