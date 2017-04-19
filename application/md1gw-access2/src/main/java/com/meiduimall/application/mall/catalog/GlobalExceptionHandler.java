package com.meiduimall.application.mall.catalog;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.meiduimall.application.mall.catalog.constant.ApplMallApiCode;
import com.meiduimall.core.ResBodyData;
import com.meiduimall.core.util.JsonUtils;
import com.meiduimall.exception.ApiException;
import com.meiduimall.exception.ServiceException;

/**
 * @Copyright (C), 2002-2017, 美兑壹购物
 * @FileName: GlobalExceptionHandler.java
 * @Author: yangchangfu
 * @Date: 2017年3月17日 下午4:11:30
 * @Description: 全局异常处理
 */
@ControllerAdvice(basePackages = "com.meiduimall.application.mall.catalog.controller")
@ResponseBody
public class GlobalExceptionHandler {

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public Object methodJsonArgumentNotValidHandler(HttpServletRequest request,
			MethodArgumentNotValidException exception) {
		return new ResBodyData(ApplMallApiCode.REQUEST_PARAMS_ERROR,
				ApplMallApiCode.getZhMsg(ApplMallApiCode.REQUEST_PARAMS_ERROR),
				JsonUtils.getInstance().createObjectNode());
	}

	@ExceptionHandler(value = BindException.class)
	public Object methodFromArgumentNotValidHandler(HttpServletRequest request, BindException exception) {
		return new ResBodyData(ApplMallApiCode.REQUEST_PARAMS_ERROR,
				ApplMallApiCode.getZhMsg(ApplMallApiCode.REQUEST_PARAMS_ERROR),
				JsonUtils.getInstance().createObjectNode());
	}

	@ExceptionHandler(value = ApiException.class)
	public Object apiExceptionHandler(HttpServletRequest request, ApiException exception) {
		return new ResBodyData(exception.getCode(), exception.getMessage(), JsonUtils.getInstance().createObjectNode());
	}

	@ExceptionHandler(value = ServiceException.class)
	public Object serviceExceptionHandler(HttpServletRequest request, ServiceException exception) {
		return new ResBodyData(exception.getCode(), exception.getMessage(), JsonUtils.getInstance().createObjectNode());
	}
}
