/*
 * Copyright @ 2015QIANLONG.
 * All right reserved.
 */

package com.dk3k.framework.exception;

import org.apache.commons.lang3.StringUtils;

public class CommonDubboException extends BaseException {

	private static final long serialVersionUID = 1L;
	/**
	 * 自定义错误代码
	 */
	private String errorCode;

	/**
	 * 自定义错误信息
	 */
	private String errorMsg;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		if (StringUtils.isBlank(errorMsg)) {
			return super.getMessage();
		} else {
			return this.errorMsg;
		}
	}

	@Override
	public String getMessage() {
		if (StringUtils.isBlank(errorMsg)) {
			return super.getMessage();
		} else {
			return this.errorMsg;
		}
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public CommonDubboException() {
		super();
	}

	public CommonDubboException(String message) {
		super(message);
	}

	public CommonDubboException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public CommonDubboException(Throwable throwable) {
		super(throwable);
	}

	public CommonDubboException(String errorCode, String errorMsg) {
		super();
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
}
