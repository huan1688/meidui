package com.meiduimall.application.usercenter.constant;

import com.meiduimall.core.BaseApiCode;

/**
 * API返回值
 * @author chencong
 *
 */
public class ApiStatusConst extends BaseApiCode {
	
	public final static Integer REQUEST_GATEWAY_EX=9001;
	public final static Integer VAL_TOKEN_ANNOTATION_EX=9002;
	public final static Integer TOKEN_NOT_EXIST=9003;
	public final static Integer VAL_REDIS_TOKEN_EX=9004;
	public final static Integer RESOLVE_REQUEST_EX=9005;
	public final static Integer TIMESTAMP_EMPTY=9006;
	public final static Integer CLIENTID_EMPTY=9007;
	public final static Integer SIGN_EMPTY=9008;
	public final static Integer REQUEST_TIMEOUT=9009;
	public final static Integer TIMESTAMP_FORMAT_ERROR=9010;
	public final static Integer MEMID_OF_TOKEN_EMPTY=9011;
	public final static Integer SIGN_FORMAT_ERROR=9012;
	public final static Integer SIGN_ERROR=9013;
	public final static Integer GET_SIGN_EX=9014;
	
	public final static Integer EXIT_EXCEPTION=9015;
	public final static Integer UPDATE_PAYPWD_EXCEPTION=9016;
	public final static Integer GET_VALIDATE_CODE_EXCEPTION=9017;
	
	static {
		zhMsgMap.put(REQUEST_GATEWAY_EX, "网关HTTP请求程序异常");
		zhMsgMap.put(VAL_TOKEN_ANNOTATION_EX, "判断API接口是否有token注解程序异常");
		zhMsgMap.put(TOKEN_NOT_EXIST, "token不存在");
		zhMsgMap.put(VAL_REDIS_TOKEN_EX, "校验token程序异常");
		zhMsgMap.put(RESOLVE_REQUEST_EX, "解析request程序异常");
		zhMsgMap.put(TIMESTAMP_EMPTY, "时间戳不能为空");
		zhMsgMap.put(CLIENTID_EMPTY, "clientID不能为空");
		zhMsgMap.put(SIGN_EMPTY, "签名不能为空");
		zhMsgMap.put(REQUEST_TIMEOUT, "请求超时");
		zhMsgMap.put(TIMESTAMP_FORMAT_ERROR, "时间戳格式错误");
		zhMsgMap.put(MEMID_OF_TOKEN_EMPTY, "token对应的memId为空");
		zhMsgMap.put(SIGN_FORMAT_ERROR, "签名格式错误");
		zhMsgMap.put(SIGN_ERROR, "签名错误");
		zhMsgMap.put(GET_SIGN_EX, "生成签名程序异常");
		
		zhMsgMap.put(EXIT_EXCEPTION, "退出登录失败，请联系客服");
		zhMsgMap.put(UPDATE_PAYPWD_EXCEPTION, "修改支付密码失败，请联系客服");
		zhMsgMap.put(GET_VALIDATE_CODE_EXCEPTION, "获取短信验证码失败，请联系客服");
	}
}