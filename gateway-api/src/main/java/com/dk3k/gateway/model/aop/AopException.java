/*
 * Copyright @ 2016QIANLONG.
 * All right reserved.
 */

package com.dk3k.gateway.model.aop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.dk3k.framework.constants.Constants;
import com.dk3k.framework.core.dto.ResponseEntity;
import com.dk3k.framework.exception.CommonDubboException;

@Component
@Aspect
public class AopException implements Ordered {

	private final static Logger LOGGER = LoggerFactory.getLogger(AopException.class);

	@Pointcut("execution(* com.dk3k.gateway..controller.*.*(..))||execution(* com.dk3k.gateway.service..*.*(..))")
	public void aopException() {
	}

	@After("aopException()")
	public void after(JoinPoint joinPoint) {
		// LOGGER.debug("===========after");
	}

	@Before("aopException()")
	public void beforeAdvice(JoinPoint joinPoint) {
		// LOGGER.debug("===========before");
	}

	@AfterReturning(pointcut = "aopException()", returning = "returnVal")
	public Object afterReturning(JoinPoint joinPoint, Object returnVal) {
		// LOGGER.debug("===========afterReturning");
		return returnVal;
	}

	@AfterThrowing(pointcut = "aopException()", throwing = "error")
	public void afterThrowing(JoinPoint jp, Throwable error) {
		// LOGGER.debug("===========afterThrowing");
	}

	@Around("aopException()")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		List<Object> list = new ArrayList<Object>();
		for (Object object : pjp.getArgs()) {
			if (object instanceof Serializable) {
				list.add(object);
			}
		}
		String name = pjp.getTarget().getClass().getSimpleName() + "." + pjp.getSignature().getName();
//		String type = "Service";
		if (name.contains("Controller")) {
//			type = "URL";
			name = "Controller." + name;
		} else if (name.contains("Service")) {
//			type = "Service";
			name = "Service." + name;
		} else if (name.contains("BusinessImpl")) {
//			type = "Service";
			name = "Business." + name;
		}
		Object returnVal = null;
		Class<?> returnType = ((MethodSignature) pjp.getSignature()).getReturnType();
		if (ResponseEntity.class.equals(returnType) && name.contains("Service")) {
			LOGGER.debug("===========AopException  around start");
			// 返回值类型是ResponseEntity,并且类名中含有Service的方法,做异常捕获
			ResponseEntity<?> entity = new ResponseEntity<Object>();
			try {
				LOGGER.debug("enter " + name + " method,args {}", JSONObject.toJSONString(list));
				entity = (ResponseEntity<?>) pjp.proceed();
				LOGGER.debug(name + " success,args {},response {}", JSONObject.toJSONString(list), JSONObject.toJSONString(entity));
			} catch (CommonDubboException e) {
				entity.setError(e.getErrorCode());
				entity.setMsg(e.getErrorMsg());
				entity.setStatus(Constants.System.OK);
				LOGGER.error(name + " error,args {},response {}", JSONObject.toJSONString(list), JSONObject.toJSONString(entity), e);
			} catch (Exception e) {
				entity.setError(Constants.System.SERVER_ERROR);
				entity.setMsg(Constants.System.SERVER_ERROR_MSG);
				entity.setStatus(Constants.System.FAIL);
				LOGGER.error(name + " error,args {},response {}", JSONObject.toJSONString(list), JSONObject.toJSONString(entity), e);
			} finally {
				
			}
			returnVal = entity;
			LOGGER.debug("exit " + name + " method");
			LOGGER.debug("==========AopException  around end");
		} else {
			// 其他方法,包括controller/返回值不含entity的Service/BusinessImpl不做异常捕获,只记录调用次数和调用时间
			try {
				returnVal = pjp.proceed();
			} finally {
				
			}
		}
		return returnVal;
	}

	@Override
	public int getOrder() {
		return 1;
	}

}