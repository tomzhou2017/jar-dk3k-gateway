package com.dk3k.framework.core.dto;


import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by zhangyingliang on 2016/12/10.
 *
 * install of artifactId : mobanker-fx-contract
 * @see{com.dk3k.framework.contract.ResponseEntity}
 * 下个版本删除，请注意！这样名称的结构是模块化，有规律，方便spring集成scan指定模块的包，而不是像之前扫 com.dk3k.framework.**
 * 而现在是按需加载，需要什么模块扫什么模块，之间不相互依赖  比如扫：com.dk3k.framework.contract ，com.dk3k.framework.dao ，
 * 这样做其实实质是简化开发，你自己体会吧！core 也需要改，但是只能一点一点割肉了....因为原先老系统依赖太多，结构也有些不合理
 */
@Data
@ToString(callSuper = true)
public class ResponseEntity<T> implements Serializable {
	private static final long serialVersionUID = -720807478055084231L;
	
	private String status;
	private String error;
	private String msg;
	private T data;

	private String pageCount;

	private String code;
	public ResponseEntity(){
		
	}
	
	public ResponseEntity(String status){
		this.status = status;
	}
	
	public ResponseEntity(String status, String error){
		this.status = status;
		this.error = error;
	}
	
	public ResponseEntity(String status, T data){
		this.status = status;
		this.data = data;
	}
	
	public ResponseEntity(String status, T data,String pageCount){
		this.status = status;
		this.data = data;
		this.pageCount = pageCount;
	}
	
	public ResponseEntity(String status, String error, String msg, T data){
		this.status = status;
		this.error = error;
		this.msg = msg;
		this.data = data;
	}
	
	public String getStatus() {
		return status;
	}

	public ResponseEntity<T> setStatus(String status) {
		this.status = status;
		return this;
	}

	public String getError() {
		return error;
	}

	public ResponseEntity<T> setError(String error) {
		this.error = error;
		return this;
	}

	public String getMsg() {
		return msg;
	}

	public ResponseEntity<T> setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public T getData() {
		return data;
	}

	public ResponseEntity<T> setData(T data) {
		this.data = data;
		return this;
	}

	public String getPageCount() {
		return pageCount;
	}

	public void setPageCount(String pageCount) {
		this.pageCount = pageCount;
	}

}
