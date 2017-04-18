package com.meiduimall.service.financial;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.meiduimall.core.ResBodyData;
import com.meiduimall.core.util.JsonUtils;
import com.meiduimall.exception.ApiException;
import com.meiduimall.exception.DaoException;
import com.meiduimall.exception.ServiceException;
import com.meiduimall.service.financial.constant.ServiceFinancialApiCode;

/**
 * @Copyright (C), 2002-2017, 美兑壹购物
 * @FileName: GlobalExceptionHandler.java
 * @Author: yangchangfu
 * @Date: 2017年3月17日 下午4:11:30
 * @Description: 全局异常处理
 */
@ControllerAdvice(basePackages = "com.meiduimall.service.financial.controller")
@ResponseBody
public class GlobalExceptionHandler {

	private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	public Object MethodJsonArgumentNotValidHandler(HttpServletRequest request,
			MethodArgumentNotValidException exception) {
		return new ResBodyData(ServiceFinancialApiCode.REQUEST_PARAMS_ERROR,
				ServiceFinancialApiCode.getZhMsg(ServiceFinancialApiCode.REQUEST_PARAMS_ERROR),
				JsonUtils.getInstance().createObjectNode());
	}

	@ExceptionHandler(value = BindException.class)
	public Object MethodFromArgumentNotValidHandler(HttpServletRequest request, BindException exception) {
		return new ResBodyData(ServiceFinancialApiCode.REQUEST_PARAMS_ERROR,
				ServiceFinancialApiCode.getZhMsg(ServiceFinancialApiCode.REQUEST_PARAMS_ERROR),
				JsonUtils.getInstance().createObjectNode());
	}

	@ExceptionHandler(value = ApiException.class)
	public Object ApiExceptionHandler(HttpServletRequest request, ServiceException exception) {
		return new ResBodyData(exception.getCode(), ServiceFinancialApiCode.getZhMsg(exception.getCode()),
				JsonUtils.getInstance().createObjectNode());
	}

	@ExceptionHandler(value = ServiceException.class)
	public Object ServiceExceptionHandler(HttpServletRequest request, ServiceException exception) {
		return new ResBodyData(exception.getCode(), ServiceFinancialApiCode.getZhMsg(exception.getCode()),
				JsonUtils.getInstance().createObjectNode());
	}

	@ExceptionHandler(value = DaoException.class)
	public Object DaoExceptionHandler(HttpServletRequest request, ServiceException exception) {
		logger.error("全局捕获DaoException:   " + exception);
		return new ResBodyData(ServiceFinancialApiCode.UNKNOWN_ERROR,
				ServiceFinancialApiCode.getZhMsg(ServiceFinancialApiCode.UNKNOWN_ERROR),
				JsonUtils.getInstance().createObjectNode());
	}
}
