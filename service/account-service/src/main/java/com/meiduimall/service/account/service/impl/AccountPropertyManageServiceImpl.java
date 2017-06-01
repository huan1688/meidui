package com.meiduimall.service.account.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meiduimall.service.account.dao.BaseDao;
import com.meiduimall.service.account.model.MSAccountType;

/**
 * 账户类型管理相关业务逻辑{@link=AccountPropertyManageService}实现类
 * @author chencong
 *
 */
@Service
public class AccountPropertyManageServiceImpl implements AccountPropertyManageService {
	
	@Autowired
	private BaseDao baseDao;

	@Override
	public List<MSAccountType> getCwtzWalletTypeList(){
		return baseDao.selectList(null,"MSAccountTypeMapper.getCwtzWalletTypeList");
	}

}
