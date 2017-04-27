package com.meiduimall.service.account.service;

import com.meiduimall.exception.SystemException;
import com.meiduimall.service.account.model.MSMembersPaypwd;
import com.meiduimall.service.account.model.ResBodyData;

/**
 * 支付密码相关操作
 * @author chencong
 *
 */
public interface PaypwdService {
	
	/**
	 * 验证支付密码
	 * @param msMembersPaypwd 支付密码信息
	 * @return 统一数据返回格式
	 * @throws SystemException  
	 */
	ResBodyData validePaypwd(MSMembersPaypwd msMembersPaypwd) throws SystemException;
	
	/**
	 * 设置支付密码
	 * @param msMembersPaypwd 支付密码信息
	 * @return 统一数据返回格式
	 * @throws SystemException 
	 */
	ResBodyData setPaypwd(MSMembersPaypwd msMembersPaypwd) throws SystemException;
	
	/**
	 * 根据memId查询是否设置过支付密码
	 * @param memId 会员ID
	 * @return 1：存在  0：不存在
	 * @throws Exception
	 */
	ResBodyData isExistPaypwd(String memId);
}
