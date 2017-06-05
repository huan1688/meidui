package com.meiduimall.service.account.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meiduimall.exception.MdBizException;
import com.meiduimall.service.account.constant.ConstApiStatus;
import com.meiduimall.service.account.dao.BaseDao;
import com.meiduimall.service.account.model.MSAccountFreezeDetail;
import com.meiduimall.service.account.service.AccountFreezeDetailService;
import com.meiduimall.service.account.service.AccountReportService;
import com.meiduimall.service.account.service.AccountService;
import com.meiduimall.service.account.util.DateUtil;

/**
 * 账户冻结解冻业务逻辑接口实现类{@link=AccountFreezeDetailService}
 * @author chencong
 *
 */
@Service
public class AccountFreezeDetailServiceImpl implements AccountFreezeDetailService{
	
	private final static Logger logger=LoggerFactory.getLogger(AccountDetailServiceImpl.class);

	@Autowired
	private BaseDao baseDao;
	
	@Autowired
	private AccountService accountServices;
	
	@Autowired
	private AccountReportService accountReportService;
	
	@Override
	public void saveAccountFreezeDetail(String memId, String orderId,
			String accountId, String accountType, String tradeType,
			String tradeAmount, Date tradeDate, String freezeBalance,
			String remark) throws MdBizException{
		Map<String,Object> paramsMap = new HashMap<String,Object>();
		paramsMap.put("id", UUID.randomUUID().toString());
		paramsMap.put("accountNo", orderId);
		paramsMap.put("tradeType", tradeType);
		paramsMap.put("tradeAmount", Double.valueOf(tradeAmount));
		paramsMap.put("freezeBalance", freezeBalance);
		paramsMap.put("remark", remark);
		paramsMap.put("inOrOut", "1");
		paramsMap.put("tradeDate", DateUtil.format(tradeDate,DateUtil.YYYY_MM_DD_HH_MM_SS));
		paramsMap.put("businessNo", orderId);
		
		paramsMap.put("createDate", DateUtil.format(tradeDate,DateUtil.YYYY_MM_DD_HH_MM_SS));
		paramsMap.put("createUser", "sysadmin");
		paramsMap.put("updateDate", DateUtil.format(tradeDate,DateUtil.YYYY_MM_DD_HH_MM_SS));
		paramsMap.put("updateUser", "sysadmin");
		try {
			baseDao.insert(paramsMap, "MSAccountFreezeDetailMapper.insertAccountFreezeDetail");
		} catch (Exception e) {
			logger.error("写入账户冻结明细出现错误-1001，会员编号:{}，订单编号:{}，错误信息:{}", memId, orderId, e.getMessage());
			throw new MdBizException(ConstApiStatus.INSERT_MEMBER_FREEZE_DETAIL_ERROR);
		}
	}

	@Override
	public void saveAccountUnFreezeDetail(String memId, String orderId,
			String accountId, String accountType, String tradeType,
			String tradeAmount, Date tradeDate, String freezeBalance,
			String remark) {
		Map<String,String> paramsMap = new HashMap<String,String>();
		paramsMap.put("id", UUID.randomUUID().toString());
		paramsMap.put("memId", memId);
		paramsMap.put("accountNo", accountId);
		paramsMap.put("tradeType", tradeType);
		paramsMap.put("tradeAmount", tradeAmount);
		paramsMap.put("tradeDate", DateUtil.format(tradeDate,DateUtil.YYYY_MM_DD_HH_MM_SS));
		paramsMap.put("inOrOut", "-1");
		paramsMap.put("freezeBalance", freezeBalance);
		paramsMap.put("businessNo", orderId);
		paramsMap.put("createDate", DateUtil.format(new Date(),DateUtil.YYYY_MM_DD_HH_MM_SS));
		paramsMap.put("createUser", "sysadmin");
		paramsMap.put("updateDate", DateUtil.format(new Date(),DateUtil.YYYY_MM_DD_HH_MM_SS));
		paramsMap.put("updateUser", "sysadmin");
		paramsMap.put("remark", remark);
		
		try {
			baseDao.insert(paramsMap, "MSAccountFreezeDetailMapper.insertAccountFreezeDetail");
		} catch (Exception e) {
			logger.error("写入账户冻结明细出现错误-1002，会员编号：%s，订单编号：%s，错误信息：%s", 
					memId, orderId, e.getMessage());
		}
	}
	

	@Override
	public List<MSAccountFreezeDetail> getRecordsByOrderId(String orderId) {
		return baseDao.selectList(orderId, "MSAccountFreezeDetailMapper.getRecordsByOrderId");
	}


}
