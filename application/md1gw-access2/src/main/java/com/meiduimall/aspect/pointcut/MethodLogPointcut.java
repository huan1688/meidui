package com.meiduimall.aspect.pointcut;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * AOP配置，公共配置meiduimall-common工程中
 * 方法限定名：com.meiduimall.aspect.pointcut.MethodLogPointcut.pointcutLog()
 * @author yangchangfu
 */
@Aspect
public class MethodLogPointcut {
	
	@Pointcut("execution(* com.meiduimall.application.catalog.controller.*.*(..))")
	public void pointcutLog() {
		
	}
}
