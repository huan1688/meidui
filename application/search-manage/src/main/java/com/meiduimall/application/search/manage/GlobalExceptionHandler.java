package com.meiduimall.application.search.manage;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.meiduimall.application.search.manage.constant.SysConstant;
import com.meiduimall.core.ResBodyData;
import com.meiduimall.exception.BizException;
import com.meiduimall.exception.ServiceException;
import com.meiduimall.exception.SystemException;


/**
 * Copyright (C), 2002-2017, 美兑壹购物
 * FileName: GlobalExceptionHandler.java
 * Author:   Administrator
 * Date:     2017年4月11日 下午3:41:29
 * Description: 全局参数验证不通过的处理
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
	
	private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
    @ExceptionHandler(value=MethodArgumentNotValidException.class)  
    public Object methodJsonArgumentNotValidHandler(HttpServletRequest request,MethodArgumentNotValidException exception)  {  
        StringBuilder sb=new StringBuilder();
        exception.getBindingResult().getFieldErrors().forEach((error)->{
        	sb.append(error.getDefaultMessage()).append(";");
        });
        Map<String, Object> result=new HashMap<>();
        result.put(SysConstant.STATUS_CODE,HttpStatus.SC_BAD_REQUEST);
		result.put(SysConstant.RESULT_MSG, sb.toString());
        return result;  
    }  
    
    
    @ExceptionHandler(value=BindException.class)  
    public Object methodFromArgumentNotValidHandler(HttpServletRequest request,BindException exception) {  
    	StringBuilder sb=new StringBuilder();
        exception.getBindingResult().getFieldErrors().forEach((error)->{
        	sb.append(error.getDefaultMessage()).append(";");
        });
        Map<String, Object> result=new HashMap<>();
        result.put(SysConstant.STATUS_CODE,HttpStatus.SC_BAD_REQUEST);
		result.put(SysConstant.RESULT_MSG, sb.toString());
        return result;  
    }  

    @ExceptionHandler(value = BizException.class)
    public ResBodyData bizException(HttpServletRequest request, ServiceException exception) {
      logger.error(request.getContextPath()+request.getRequestURI()+" "+exception.getLocalizedMessage());
      return new ResBodyData(exception.getCode(),exception.getLocalizedMessage());
    }
    
    @ExceptionHandler(value = SystemException.class)
    public ResBodyData systemException(HttpServletRequest request, ServiceException exception) {
      logger.error(request.getContextPath()+request.getRequestURI()+" "+exception.getLocalizedMessage());
      return new ResBodyData(exception.getCode(),exception.getLocalizedMessage());
    }
    
}
