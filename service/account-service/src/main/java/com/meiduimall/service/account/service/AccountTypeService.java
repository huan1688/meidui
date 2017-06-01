package com.meiduimall.service.account.service;

import java.util.List;

import com.meiduimall.service.account.model.MSAccountType;

/**
 * 账户类型管理相关业务逻辑
 * @author jun.wu@meiduimall.com
 *
 */
public interface AccountTypeService {
	
	/**
	 * 查询财务调整相关的账户类型信息
	 * @return
	 */
	public List<MSAccountType> getCwtzWalletTypeList();
	
	/**
	 * 根据账户类型编号查询当前序列号
	 * @param typeNo 账户类型编号
	 * @return 序列号
	 */
	public Long getSequenceByAccountTypeNo(String accountTypeNo);
	
	/**
	 * 根据账户类型编号更新当前序列号
	 * @param typeNo 账户类型编号
	 * @return 更新后的序列号，上一个序列号+1
	 */
	public Long updateSequenceByAccountTypeNo(String accountTypeNo);

}
