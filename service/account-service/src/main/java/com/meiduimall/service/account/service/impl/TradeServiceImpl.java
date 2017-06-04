package com.meiduimall.service.account.service.impl;

import static org.assertj.core.api.Assertions.in;
import static org.mockito.Matchers.endsWith;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.meiduimall.core.ResBodyData;
import com.meiduimall.exception.DaoException;
import com.meiduimall.exception.MdSysException;
import com.meiduimall.exception.ServiceException;
import com.meiduimall.service.account.constant.ConstApiStatus;
import com.meiduimall.service.account.constant.ConstPointsChangeType;
import com.meiduimall.service.account.constant.ConstPointsFinalType;
import com.meiduimall.service.account.constant.ConstSysParamsDefination;
import com.meiduimall.service.account.constant.ConstTradeType;
import com.meiduimall.service.account.dao.BaseDao;
import com.meiduimall.service.account.model.MSAccount;
import com.meiduimall.service.account.model.MSAccountFreezeDetail;
import com.meiduimall.service.account.model.MSAccountReport;
import com.meiduimall.service.account.model.MSBankAccount;
import com.meiduimall.service.account.model.MSBankWithdrawDeposit;
import com.meiduimall.service.account.model.MSConsumePointsFreezeInfo;
import com.meiduimall.service.account.model.MSMemberConsumeHistory;
import com.meiduimall.service.account.model.MSMemberIntegral;
import com.meiduimall.service.account.model.request.MSMemberConsumeHistoryReq;
import com.meiduimall.service.account.model.request.MemberConsumeMessageReq;
import com.meiduimall.service.account.model.request.RequestSaveOrder;
import com.meiduimall.service.account.model.request.RequestCancelOrder;
import com.meiduimall.service.account.service.AccountAdjustService;
import com.meiduimall.service.account.service.AccountDetailService;
import com.meiduimall.service.account.service.AccountFreezeDetailService;
import com.meiduimall.service.account.service.AccountReportService;
import com.meiduimall.service.account.service.AccountService;
import com.meiduimall.service.account.service.BankAccountService;
import com.meiduimall.service.account.service.BankWithdrawDepositService;
import com.meiduimall.service.account.service.MSConsumePointsDetailService;
import com.meiduimall.service.account.service.MemberConsumeHistoryService;
import com.meiduimall.service.account.service.MoneyService;
import com.meiduimall.service.account.service.TradeService;
import com.meiduimall.service.account.service.ValidateService;
import com.meiduimall.service.account.service.PointsService;
import com.meiduimall.service.account.util.DateUtil;
import com.meiduimall.service.account.util.DoubleCalculate;
import com.meiduimall.service.account.util.GenerateNumber;
import com.meiduimall.service.account.util.SerialStringUtil;
import com.meiduimall.service.account.util.StringUtil;
import com.netflix.infix.lang.infix.antlr.EventFilterParser.predicate_return;

/**
 * 订单交易相关逻辑接口{@link=TradeService}实现类
 * @author chencong
 *
 */
@Service
public class TradeServiceImpl implements TradeService {

	private final static Logger logger = LoggerFactory.getLogger(TradeServiceImpl.class);

	@Autowired
	private BaseDao baseDao;

	@Autowired
	private PointsService pointsService;

	@Autowired
	private MoneyService moneyService;

	@Autowired
	private AccountService accountServices;

	@Autowired
	private BankWithdrawDepositService bankWithdrawDepositService;

	@Autowired
	private BankAccountService bankAccountService;

	@Autowired
	private AccountAdjustService accountAdjustService;

	@Autowired
	private MSConsumePointsDetailService pointsDetailService;

	@Autowired
	private MemberConsumeHistoryService memberConsumeHistoryService;

	@Autowired
	private AccountReportService accountReportService;
	
	@Autowired
	private AccountFreezeDetailService accountFreezeDetailService;
	
	@Autowired
	private AccountDetailService accountDetailService;

	private ValidateService validateService;
	
	@Override
	public ResBodyData saveOrder(RequestSaveOrder model){
		//校验交易金额
		validateService.checkConsumeAmountRelation(model.getConsumeAmount(),model.getConsumeMoney(),model.getConsumePoints());
		//将数据来源转换为字典值
		model.setOrderSource(SerialStringUtil.getDictOrderSource(model.getOrderSource()));
		
		ResBodyData resBodyData=new ResBodyData(ConstApiStatus.SUCCESS,"保存订单成功");
		
		/**冻结积分*/
		/*resBodyData=pointsService.freezePointsAndAddRecord(memId,consumePoints,orderId,orderSource);*/
		logger.info("冻结积分结果：{}",resBodyData.toString());
		if(resBodyData.getStatus()!=0){
			return resBodyData;
		}
 
		
		/**冻结余额*/
		/*resBodyData=moneyService.freezeMoneyAndAddRecord(memId,consumeMoney,orderId,orderSource);*/
		logger.info("冻结余额结果：{}",resBodyData.toString());
		if(resBodyData.getStatus()!=0){
			return resBodyData;
		}

		return resBodyData;
	}

	@Override
	public ResBodyData cancelOrder(RequestCancelOrder model)  {
		ResBodyData resBodyData=new ResBodyData(null,null);
		String memId=model.getMemId();
		/**解冻并扣减积分*/
		Map<String,Object> dataMap=new HashMap<>();
		/*if(pointsService.getFreezeUnfreezeRecordByOrderId(orderId)){
			pointsService.unFreezePointsAndAddRecord(memId,consumePoints,orderId,orderSource,dataMap);
			pointsService.deductPointsAndAddRecord(memId,consumePoints,orderId,orderSource,dataMap);
			}
		*//**解冻并扣减余额*//*
		if(moneyService.getFreezeUnfreezeRecordByOrderId(orderId)){
			moneyService.unFreezeMoneyAndAddRecord(memId,consumeMoney,orderId,orderSource,dataMap);
			moneyService.deductMoneyAndAddRecord(memId,consumeMoney,orderId,orderSource,dataMap);
		}*/
		resBodyData.setData(dataMap);
		/** 写入会员消费记录 */
		MSMemberConsumeHistory history = new MSMemberConsumeHistory();
		/*history.setMchId(UUID.randomUUID().toString());
		history.setMemId(memId);
		history.setOrderId(orderId);
		history.setMchProductName(productName);
		history.setMchOrginType(orderSource);
		history.setMchOrginMemId("");
		history.setMchPayType(payType);
		history.setMchStatus("1");
		history.setMchConsumePointsCount(consumePoints);
		history.setMchShoppingCouponCount(consumeMoney);
		history.setMchSettingStatus(1);*/
		history.setMchIssueStatus(1);
		try {
			this.saveMemberTradeHistory(history);
		} catch (Exception e) {

		}
		return resBodyData;
	}

	@Override
	public JSONObject accountTradeCancel(JSONObject param) throws Exception {
		final JSONObject resultJson = new JSONObject();
		/*
		 * resultJson.put(SysParamsConst.STATUS_CODE, SysConstant.ZERO);
		 * resultJson.put(SysParamsConst.RESULT_MSG, SysConstant.SUCCESS);
		 */

		final String userId = param.getString("user_id"); // 用户标识
		final String orderId = param.getString("order_id");// 订单号
		final String orderSource = param.getString("order_source");// 数据来源
		final String tradeType = param.getString("trade_type"); // 业务类型
		final String tradeDateStr = param.getString("trade_date"); // 订单发生时间
		final String payType = param.getString("pay_type"); // 支付方式
		final String productName = param.getString("product_name"); // 消费项目
		final String tradeTotalMoney = param.getString("trade_total_money"); // 消费总额
		final String tradeMoney = param.getString("trade_money"); // 消费使用现金金额
		final String tradeAmount = param.getString("trade_amount"); // 消费使用余额
		final String tradePoint = param.getString("trade_point"); // 消费使用积分
		final String remark = param.getString("remark"); // 备注
		boolean bsFlag = true;

		// 数据转换
		Date tradeDate = new Date(Long.parseLong(tradeDateStr));
		// 检查会员
		String memId = accountServices.getMemIdByUserId(userId);

		// 解冻积分,并扣减积分
		if (bsFlag) {
			/*
			 * if(accountServices.checkFreezePointByOrderId(orderId)){ bsFlag =
			 * accountServices.cutMDConsumePointsFreezeAndDetail(memId,
			 * tradePoint, orderId, orderSource, tradeType, memId, remark); }
			 */
		}
		// 解冻余额，并扣减余额
		if (bsFlag) {
			if (accountServices.checkFreezeMoneyByOrderId(orderId)) {
				bsFlag = accountAdjustService.cutConsumeFreezeMoneyAndDetail(memId, orderId, tradeType, tradeDate,
						tradeAmount, remark);
			}
		}

		// 出现错误返回运行时异常回滚事务
		if (!bsFlag) {
			throw new RuntimeException("accountTradeCancel-业务处理时出现错误-1002，回滚事务。");
		}

		return resultJson;
	}

	@Override
	public JSONObject accountTradeRefundApply(JSONObject param) throws Exception {
		final JSONObject resultJson = new JSONObject();
		/*
		 * resultJson.put(SysParamsConst.STATUS_CODE, SysConstant.ZERO);
		 * resultJson.put(SysParamsConst.RESULT_MSG, SysConstant.SUCCESS);
		 */
		// 暂不实现，库表不支持
		return resultJson;
	}

	@Override
	public JSONObject accountTradeRefundAffirm(JSONObject param) throws Exception {
		final JSONObject resultJson = new JSONObject();
		/*
		 * resultJson.put(SysParamsConst.STATUS_CODE, SysConstant.ZERO);
		 * resultJson.put(SysParamsConst.RESULT_MSG, SysConstant.SUCCESS);
		 */

		final String userId = param.getString("user_id"); // 用户标识
		final String orderId = param.getString("order_id");// 订单号
		final String orderSource = param.getString("order_source");// 数据来源
		final String tradeType = param.getString("trade_type"); // 业务类型
		final String payType = param.getString("pay_type"); // 支付方式
		final String productName = param.getString("product_name"); // 项目
		final String tradeTotalMoney = param.getString("trade_total_money"); // 总额
		final String tradeMoney = param.getString("trade_money"); // 使用现金金额
		final String tradeDateStr = param.getString("trade_date"); // 订单发生时间
		final String tradeAmount = param.getString("trade_amount"); // 退款余额
		final String tradePoint = param.getString("trade_point"); // 退款积分
		final String remark = param.getString("remark"); // 备注
		boolean bsFlag = true;

		// 数据转换
		Date tradeDate = new Date(Long.parseLong(tradeDateStr));
		// 获取会员id
		String memId = accountServices.getMemIdByUserId(userId);
		// 检查订单退款有效性
		List<Map<String, String>> historyList = baseDao.selectList(orderId,
				"MSAccountMapper.getConsumeHistoryByOrderId");
		Double pointTotal = new Double("0"); // 积分总额
		Double pointRefundTotal = new Double("0");// 退款积分总额
		Double moneyTotal = new Double("0"); // 余额总额
		Double moneyRefundTotal = new Double("0");// 退款余额总额
		for (Map<String, String> tmpMap : historyList) {
			String status = tmpMap.get("status");
			// 获取已完成订单的积分与余额
			if ("1".equals(status)) {
				moneyTotal = Double.valueOf(tmpMap.get("money"));
				pointTotal = Double.valueOf(tmpMap.get("point"));
			}
			// 获取已退款的积分与余额
			if ("2".equals(status)) {
				moneyRefundTotal = DoubleCalculate.add(moneyRefundTotal, Double.valueOf(tmpMap.get("money")));
				pointRefundTotal = DoubleCalculate.add(pointRefundTotal, Double.valueOf(tmpMap.get("point")));
			}
		}
		if (DoubleCalculate.sub(DoubleCalculate.sub(moneyTotal, moneyRefundTotal), Double.valueOf(tradeAmount)) < 0) {

			return resultJson;
		}
		if (DoubleCalculate.sub(DoubleCalculate.sub(pointTotal, pointRefundTotal), Double.valueOf(tradePoint)) < 0) {

			return resultJson;
		}

		// 退款增加积分
		if (bsFlag) {
			if (accountServices.checkFreezePointByOrderId(orderId)) {
				if (!StringUtil.isEmptyByString(tradePoint) && StringUtil.checkNumber(tradePoint, "+")) {
					bsFlag = pointsDetailService.addMDConsumePointsAndDetail(memId, tradePoint, orderId, orderSource,
							tradeType, memId, remark);
				}
			}
		}
		// 退款增加余额
		if (bsFlag) {
			if (accountServices.checkFreezeMoneyByOrderId(orderId)) {
				if (!StringUtil.isEmptyByString(tradeAmount) && StringUtil.checkFloat(tradeAmount, "+")) {
					bsFlag = accountAdjustService.addConsumeMoneyAndDetail(memId, orderId, tradeType, tradeDate,
							tradeAmount, remark);
				}
			}
		}
		// 消费记录
		if (bsFlag) {
			MSMemberConsumeHistory history = new MSMemberConsumeHistory();
			history.setMchId(UUID.randomUUID().toString());
			history.setMemId(memId);
			history.setOrderId(orderId);
			history.setMchProductName(productName);
			history.setMchOrginType(orderSource);
			history.setMchOrginMemId("");
			history.setMchCreatedDate(tradeDate);
			history.setMchPayType(payType);
			history.setMchStatus("2");
			/*
			 * history.setMchConsumePointsCount(tradePoint);
			 * history.setMchShoppingCouponCount(tradeAmount);
			 */
			history.setMchSettingStatus(1);
			history.setMchIssueStatus(1);
			saveMemberTradeHistory(history);
		}

		// 出现错误返回运行时异常回滚事务
		if (!bsFlag) {
			throw new RuntimeException("accountTradeRefundAffirm-业务处理时出现错误-1004，回滚事务。");
		}

		return resultJson;
	}

	@Override
	public JSONObject bankWithdrawDepositApply(JSONObject param) throws Exception {
		final JSONObject resultJson = new JSONObject();
		/*
		 * resultJson.put(SysParamsConst.STATUS_CODE, SysConstant.ZERO);
		 * resultJson.put(SysParamsConst.RESULT_MSG, SysConstant.SUCCESS);
		 */

		final String userId = param.getString("user_id"); // 用户标识
		final String accountIdcard = param.getString("account_idcard");// 身份证
		final String accountNo = param.getString("account_no");// 银行卡号
		final String accountName = param.getString("account_name");// 银行卡户名
		final String accountBank = param.getString("account_bank");// 银行名称
		String accountProvince = "";// 银行所属省份
		if (param.containsKey("account_province")) {
			accountProvince = param.getString("account_province");// 银行所属省份
		}
		String accountCity = "";// 银行所属城市
		if (param.containsKey("account_city")) {
			accountCity = param.getString("account_city");// 银行所属城市
		}
		String accountArea = "";// 银行所属地区
		if (param.containsKey("account_area")) {
			accountArea = param.getString("account_area");// 银行所属地区
		}
		final String accountSubBank = param.getString("account_sub_bank");// 支行名称
		final String applyCarryCash = param.getString("apply_carry_cash");// 申请提现金额
		final String counterFee = param.getString("counter_fee");// 申请提现手续费
		final String applyDate = param.getString("apply_date");// 申请提现时间

		// 检查会员
		String memId = accountServices.getMemIdByUserId(userId);
		if (StringUtil.isEmptyByString(memId)) {
			/*
			 * resultJson.put(SysParaNameConst.STATUS_CODE,
			 * ApiStatusConst.USERNAME_ERROR);
			 * resultJson.put(SysParaNameConst.RESULT_MSG,
			 * ApiStatusConst.USERNAME_ERROR_C);
			 */
			return resultJson;
		}
		// 检查银行信息是否存在
		MSBankAccount bankAccount = bankAccountService.getBankAccount(memId, accountNo);
		if (bankAccount == null) {
			return resultJson;
		}
		// 检查申请余额，并计算
		final Double old_useMoney = accountServices.getUseConsumeMoney(memId);
		final Double old_applyCarryCash = Double.valueOf(applyCarryCash);
		// 申请提现余额超过最大可提现金额
		if (old_applyCarryCash > 50000) {
			return resultJson;
		}
		// 计算申请提现余额是否超最大余额
		if (old_applyCarryCash > old_useMoney) {
			return resultJson;
		}
		// 计算当前余额是否低于最低提现金额
		if (old_useMoney <= ConstSysParamsDefination.MIN_APPLY_CARRY_CASH) {
			return resultJson;
		}

		String businessNo = GenerateNumber.generateBusinessNo(ConstTradeType.TRADE_TYPE_YETX.getCode());
		MSBankWithdrawDeposit dto = new MSBankWithdrawDeposit();
		dto.setBusinessNo(businessNo);
		dto.setMemId(memId);
		dto.setBankAccountId(bankAccount.getId());
		dto.setAccountIdcard(accountIdcard);
		// dto.setAccountNo(accountNo);
		dto.setAccountBank(accountBank);
		dto.setAccountName(accountName);
		dto.setAccountProvince(accountProvince);
		dto.setAccountCity(accountCity);
		dto.setAccountArea(accountArea);
		dto.setAccountSubBank(accountSubBank);
		/*
		 * dto.setApplyCarryCash(applyCarryCash); dto.setCounterFee(counterFee);
		 * dto.setApplyDate(new Date(Long.parseLong(applyDate)));
		 * dto.setStatus("0");
		 * 
		 * //计算扣除金额与手续费 Map<String, String> returnMap =
		 * this.calcBankWithdrawDeposit(memId, dto.getApplyCarryCash(),
		 * dto.getCounterFee());
		 */
		// 计算后实际金额
		/*
		 * String calcActualCarryCash = returnMap.get("calc_actualCarryCash");
		 * //计算后手续费 String calcCounterFee = returnMap.get("calc_counterFee");
		 * //数据检查 实际提现金额不能大于申请提现金额 if(Double.parseDouble(calcActualCarryCash) >
		 * Double.parseDouble(dto.getApplyCarryCash())){ throw new
		 * RuntimeException("bankWithdrawDepositApply-业务处理时出现错误-1001，回滚事务。"); }
		 * //数据检查 实际提现金额必须大于0 if(Double.parseDouble(calcActualCarryCash) <= 0){
		 * throw new
		 * RuntimeException("bankWithdrawDepositApply-业务处理时出现错误-1002，回滚事务。"); }
		 * //添加到dto中 dto.setActualCarryCash(calcActualCarryCash);
		 * dto.setCounterFee(calcCounterFee);
		 */
		// 提现申请时间
		Date tradeDate = new Date(Long.parseLong(applyDate));

		// 增加一条提现记录，返回业务流水号
		String id = bankWithdrawDepositService.addBankWithdrawDeposit(dto);
		if (!StringUtil.isEmptyByString(id)) {
			MSAccount account = accountServices.getAccountMoney(memId);
			if (account != null) {
				/* 临时注销代码 */
				// 余额提现冻结余额
				/*
				 * accountServices.addConsumeFreezeMoneyAndDetail(memId,
				 * businessNo, ConstTradeType.TRADE_TYPE_YETX.getCode(),
				 * tradeDate, calcActualCarryCash, "余额提现"); //余额提现冻结手续费
				 * accountServices.addConsumeFreezeMoneyAndDetail(memId,
				 * businessNo, ConstTradeType.TRADE_TYPE_TXSX.getCode(),
				 * tradeDate, calcActualCarryCash, "提现手续费");
				 */
			}
		} else {
			throw new RuntimeException("bankWithdrawDepositApply-业务处理时出现错误-1003，回滚事务。");
		}

		return resultJson;
	}

	@Override
	public JSONObject getBankWithdrawDeposits(JSONObject param) throws Exception {
		final JSONObject resultJson = new JSONObject();
		/*
		 * resultJson.put(SysParamsConst.STATUS_CODE, SysConstant.ZERO);
		 * resultJson.put(SysParamsConst.RESULT_MSG, SysConstant.SUCCESS);
		 */

		final String userId = param.getString("user_id"); // 用户标识
		final String page = param.getString("current_page");// 当前页数
		final String pagesize = param.getString("page_size");// 每页数量

		// 检查会员
		String memId = accountServices.getMemIdByUserId(userId);
		if (StringUtil.isEmptyByString(memId)) {
			/*
			 * resultJson.put(SysParaNameConst.STATUS_CODE,
			 * ApiStatusConst.USERNAME_ERROR);
			 * resultJson.put(SysParaNameConst.RESULT_MSG,
			 * ApiStatusConst.USERNAME_ERROR_C);
			 */
			return resultJson;
		}
		Map<String, String> resultMap = new HashMap<>();
		Map<String, String> parasMap = new HashMap<>();
		parasMap.put("current_page", page);
		parasMap.put("page_size", pagesize);
		List<MSBankWithdrawDeposit> bankWithdrawDepositList = bankWithdrawDepositService
				.getBankWithdrawDepositList(memId, true, parasMap, resultMap);
		if (bankWithdrawDepositList != null) {
			final JSONObject dataJson = new JSONObject();
			dataJson.put("total_page", resultMap.get("pageTotal"));
			/*
			 * dataJson.put("data_list",
			 * JSONArray.toJSON(bankWithdrawDepositList).toString());
			 */

			resultJson.put("result", dataJson);
		} else {
			final JSONObject dataJson = new JSONObject();
			dataJson.put("total_page", "0");
			dataJson.put("data_list", "[]");
			resultJson.put("result", dataJson);
		}
		return resultJson;
	}

	/**
	 * 方法名: calcBankWithdrawDeposit<br>
	 * 描述: 计算可提现金额与手续费 <br>
	 * 创建时间: 2016-12-19
	 * 
	 * @param dto
	 * @return
	 */
	private Map<String, String> calcBankWithdrawDeposit(String memId, String applyCarryCash, String counterFee) {

		Map<String, String> returnMap = new HashMap<String, String>();

		final Double old_useMoney = accountServices.getUseConsumeMoney(memId);
		final Double old_applyCarryCash = Double.valueOf(applyCarryCash);
		// final Double old_counterFee = Double.valueOf(counterFee);
		final Double calc_feeScale = new Double("0.01"); // 手续费比例
		final Double calc_minFee = ConstSysParamsDefination.MIN_APPLY_CARRY_FEE; // 最小手续费

		Double calc_counterFee = new Double("0");
		Double calc_actualCarryCash = new Double("0");

		// 计算手续费与实际提现金额
		if (old_applyCarryCash == old_useMoney) {
			// 全部提取，手续费从申请申请提取金额中扣除，实际提现金额=申请提取金额-手续费
			calc_counterFee = DoubleCalculate.mul(old_applyCarryCash, calc_feeScale);
			if (calc_counterFee < calc_minFee) {
				calc_counterFee = calc_minFee;
			}
			calc_actualCarryCash = DoubleCalculate.sub(old_applyCarryCash, calc_counterFee);
		} else {
			// 部分提取，手续费直接从余额中扣除，实际提现金额=申请提取金额；余额不足扣除手续费时，实际提现金额=申请提取金额-扣除余额后不足的手续费
			calc_counterFee = DoubleCalculate.mul(old_applyCarryCash, calc_feeScale);
			if (calc_counterFee < calc_minFee) {
				calc_counterFee = calc_minFee;
			}
			// 计算扣除申请提现金额后的余额
			Double subUseMoney = DoubleCalculate.sub(old_useMoney, old_applyCarryCash);
			// 余额够扣除手续费时，实际提现金额=申请提取金额
			if (subUseMoney >= calc_counterFee) {
				calc_actualCarryCash = old_applyCarryCash;
			} else {
				// 余额不足扣除手续费时，实际提现金额=申请提取金额-扣除余额后不足的手续费
				Double subCounterFee = DoubleCalculate.sub(calc_counterFee, subUseMoney); // 扣除余额后剩余未扣减手续费
				calc_actualCarryCash = DoubleCalculate.sub(old_applyCarryCash, subCounterFee);
			}
		}
		returnMap.put("calc_actualCarryCash", String.valueOf(calc_actualCarryCash));
		returnMap.put("calc_counterFee", String.valueOf(calc_counterFee));
		return returnMap;
	}

	private boolean saveMemberTradeHistory(MSMemberConsumeHistory history) throws Exception {
		int flag = baseDao.insert(history, "MSAccountMapper.insertMemberConsumeHistory");
		if (flag <= 0) {
			logger.error("写入会员消费记录表失败，会员编号：{}，订单编号：{}", history.getMemId(), history.getOrderId());
			return false;
		}
		return true;
	}

	@Override
	@Transactional
	public ResBodyData updateMemberOrder(MemberConsumeMessageReq mmt) {

		JSONObject json;
		try {
			double old_consume_coupon = 0;
			double old_integral = 0;
			double old_shopping_coupon = 0;
			double old_consume_money = 0; // 原订单的消费金额
			double old_consume_points = 0; // 美兑积分

			MSMemberConsumeHistory mConHis = new MSMemberConsumeHistory();

			mConHis.setMchId(UUID.randomUUID().toString());
			// 会员ID
			mConHis.setMemId(mmt.getMemId());
			// 订单ID
			mConHis.setOrderId(mmt.getOrderId());

			// 消费商品名称
			mConHis.setMchProductName(mmt.getProductName());

			// 消费来源
			mConHis.setMchOrginType(mmt.getOrderSource());
			// O2O会员ID
			mConHis.setMchOrginMemId(mmt.getOrginalUserId());
			// 消费时间
			mConHis.setMchCreatedDate(new Date(System.currentTimeMillis()));
			// 支付方式
			mConHis.setMchPayType(mmt.getPayType());
			// 订单状态
			mConHis.setMchStatus(mmt.getMchStatus());

			mConHis.setMchSettingStatus(1);
			mConHis.setMchIssueStatus(1);

			// 查询数据库是否已存在该订单，如果不存在则直接保存，如果存在则修改
			MSMemberConsumeHistory history = memberConsumeHistoryService
					.queryByOrderIdInfo(new MSMemberConsumeHistoryReq());
			List<MSMemberConsumeHistory> listhistory = memberConsumeHistoryService
					.listByOrderIdInfo(new MSMemberConsumeHistoryReq());

			/** 订单状态1表示已经完，2表示已退单 */
			if ("1".equals(mmt.getMchStatus())) {
				if (null != history) {
					logger.info("重复提交的订单");
					return new ResBodyData(2021, "重复提交的订单");
				}
			} else {
				if (null == history) {
					logger.info("当前退单的订单号与已提交的订单号不匹配");
					return new ResBodyData(2063, "当前退单的订单号与已提交的订单号不匹配");
				}
			}
			if (DateUtil.daysBetween(history.getMchCreatedDate(), new Date()) > 30) {
				logger.info("当前退单时间超过下单时的时间，无法退单");
				return new ResBodyData(2066, "当前退单时间超过下单时的时间，无法退单");
			}

			for (MSMemberConsumeHistory mc : listhistory) {
				// 美兑积分
				if (Double.valueOf(mc.getMchConsumePointsCount()) < 0) {
					String mchConsumePointsCount = String.valueOf(mc.getMchConsumePointsCount());
					old_consume_points = DoubleCalculate.add(old_consume_points,
							Double.valueOf(mchConsumePointsCount.substring(1, mchConsumePointsCount.length())));
				} else {
					old_consume_points = DoubleCalculate.add(Double.valueOf(old_consume_points),
							Double.valueOf(mc.getMchConsumePointsCount()));
				}
			}
			old_consume_coupon = DoubleCalculate.add(Double.valueOf(old_consume_coupon),
					Double.valueOf(mmt.getConsumeCouponCount()));
			old_integral = DoubleCalculate.add(Double.valueOf(old_integral), Double.valueOf(mmt.getBackIntegral()));
			old_consume_money = DoubleCalculate.add(Double.valueOf(old_consume_money),
					Double.valueOf(mmt.getConsumerMoney())); // add by Liujun at
																// 2016-07-25
																// 19:55

			// 历史使用积分 = 历史退款积分 + 当前退款积分
			if (mmt.getConsumePointsCount() != null) {
				old_consume_points = DoubleCalculate.add(old_consume_points,
						Double.valueOf(mmt.getConsumePointsCount()));
			}

			logger.info("当前退单订单号=" + mmt.getOrderId() + "支付余额总计(包含本次)" + old_shopping_coupon);
			if (old_shopping_coupon > Double.valueOf(history.getMchShoppingCouponCount())) {
				logger.info("当前退单的账户余额超出订单使用的余额");
				return new ResBodyData(2069, "当前退单的账户余额超出订单使用的余额");
			}

			// 增加美兑积分需求 2016-11-01
			logger.info("当前退单订单号=" + mmt.getOrderId() + "美积分总计(包含本次)" + old_consume_points);
			if (old_consume_points > Double.valueOf(history.getMchConsumePointsCount())) {
				logger.info("当前退单的积分超出订单使用的积分");
				return new ResBodyData(2069, "当前退单的积分超出订单使用的积分");
			}

			json = new JSONObject();
			json.put("mem_id", mmt.getMemId());

			// 获取退费前积分余额
			Double preConsumePoints = pointsService.getAvailablePointsByMemId(mmt.getMemId());
			// 获取退费前余额
			Double preConsumeMoney = accountReportService.getAvailableBalance(mmt.getMemId());
			// 增加美兑积分需求 2016-11-01 退费前积分余额
			json.put("pre_consume_points", StringUtil.interceptionCharacter(2, preConsumePoints));
			json.put("pre_shopping_coupon", StringUtil.interceptionCharacter(2, preConsumeMoney));

			// 判断支付方式
			if ("1".equals(mmt.getPayType()) || "2".equals(mmt.getPayType())) {

				// 消费卷全部返回0
				json.put("pre_consume_coupon", "0.00");
				json.put("after_consume_coupon", "0.00");

				/******************************** 执行返回扣取购物券*****************开始 ***********************/
				// 如果有余额,调用方会传递过来
				logger.info("如果为 null 将不会进行退余额操作: " + mmt.getShoppingCouponCount());
				if ("2".equals(mmt.getPayType()) && null != mmt.getShoppingCouponCount()) {
					logger.info("余额的返还金额为: " + mmt.getShoppingCouponCount());
					// 退单增加余额
					accountAdjustService.addConsumeMoneyAndDetail(mmt.getMemId(), mmt.getOrderId(),
							ConstTradeType.TRADE_TYPE_TKSH.getCode(), new Date(), mmt.getShoppingCouponCount(),
							ConstTradeType.TRADE_TYPE_TKSH.getCode());

					// 退单后余额
					double afterMoney = DoubleCalculate.add(preConsumeMoney,
							Double.valueOf(mmt.getShoppingCouponCount()));
					// 同时更新订单表的
					mConHis.setMchShoppingCouponCount(Double.valueOf(mmt.getShoppingCouponCount()));
					// 返回退单后余额
					json.put("after_shopping_coupon", StringUtil.interceptionCharacter(2, afterMoney));

					logger.info("退费订单号：" + mmt.getOrderId() + "，当次退费余额是：" + mmt.getShoppingCouponCount());

				}
				/******************************** 执行返回扣取购物券*****************结束 ***********************/
				// 增加美兑积分需求 2016-11-01
				String consumePoints = mmt.getConsumePointsCount();
				if ("2".equals(mmt.getPayType()) && null != consumePoints) {
					logger.info("退费订单号：" + mmt.getOrderId() + "，进入退费美积分计算方法.");
					// 退单返回美兑积分
					try {
						accountAdjustService.addMDConsumePoints(mmt.getMemId(), consumePoints, false);
					} catch (MdSysException e) {
						logger.error("退单返回美兑积分错误:{}", e);
						throw new ServiceException(ConstApiStatus.MD_POINTS_ERROR,
								ConstApiStatus.getZhMsg(ConstApiStatus.MD_POINTS_ERROR));
					}

					// 退单后积分余额
					double afterPoints = DoubleCalculate.add(preConsumePoints, Double.valueOf(consumePoints));
					// 同时更新订单表的
					mConHis.setMchConsumePointsCount(Double.valueOf(consumePoints));
					// 返回退单后积分
					json.put("after_consume_points", StringUtil.interceptionCharacter(2, afterPoints));

					logger.info("退费订单号：" + mmt.getOrderId() + "，当月退费美积分金额是：" + consumePoints);
				}
			}

			memberConsumeHistoryService.save(mConHis);

			logger.info("当前退余额: " + mmt.getShoppingCouponCount() + "当前退积分：" + mmt.getConsumePointsCount());
			// 更新退单消费表示
			this.cancelOrder(new MSMemberIntegral(mmt.getMemId(), history.getMchCreatedDate()));
		} catch (DaoException e) {
			logger.error(ConstApiStatus.getZhMsg(ConstApiStatus.SERVER_DEAL_WITH_EXCEPTION) + ":{}", e);
			throw new ServiceException(ConstApiStatus.SERVER_DEAL_WITH_EXCEPTION,
					ConstApiStatus.getZhMsg(ConstApiStatus.SERVER_DEAL_WITH_EXCEPTION));
		}

		return new ResBodyData(ConstApiStatus.SUCCESS, ConstApiStatus.SUCCESS_M, json);
	}

	/** 更新退单消费表示 **/
	private void cancelOrder(MSMemberIntegral mSMemberIntegral) {
		baseDao.update(mSMemberIntegral, "MsMemberIntegralMapper.cancelOrder");
	}

	@Override
	@Transactional
	public ResBodyData saveMemberOrder(MemberConsumeMessageReq mmt) {
		JSONObject result = new JSONObject();
//		MemberBasicAccount account = memberBasicAccountDao.queryMemberBasicAccount(mmt.getMem_id());
		MSMemberConsumeHistory memConHis = new MSMemberConsumeHistory();

		memConHis.setMchId(UUID.randomUUID().toString());
		// 会员ID
		memConHis.setMemId(mmt.getMemId());
		// 订单ID
		memConHis.setOrderId(mmt.getOrderId());

		// 消费商品名称
		memConHis.setMchProductName(mmt.getProductName());

		// 消费来源
		memConHis.setMchOrginType(mmt.getOrderSource());
		// O2O会员ID
		memConHis.setMchOrginMemId(mmt.getOrginalUserId());
		// 消费时间
		memConHis.setMchCreatedDate(new Date(System.currentTimeMillis()));
		// 支付方式
		memConHis.setMchPayType(mmt.getPayType());
		// 订单状态
		memConHis.setMchStatus(mmt.getMchStatus());
		memConHis.setMchShoppingCouponCount(Double.valueOf(mmt.getShoppingCouponCount()));
		// 增加美兑积分逻辑 2016-10-31
		memConHis.setMchShoppingCouponCount(Double.valueOf(mmt.getConsumePointsCount()));
		memConHis.setMchSettingStatus(1);
		memConHis.setMchIssueStatus(1);
		// 查询数据库是否已存在该订单，如果不存在则直接保存，如果存在则修改
		List<MSMemberConsumeHistory> listByOrderIdInfo = memberConsumeHistoryService
				.listByOrderIdInfo(new MSMemberConsumeHistoryReq());
		if (StringUtils.isEmpty(listByOrderIdInfo)) {
			return new ResBodyData(ConstApiStatus.REPEAT_ORDER, ConstApiStatus.getZhMsg(ConstApiStatus.REPEAT_ORDER));
		} else {

			// 根据订单查询冻结积分
			List<MSConsumePointsFreezeInfo> pointsList = pointsService.queryRecordByOrderId(mmt.getOrderId());
			// 获取积分余额
			Double consumePoints = pointsService.getAvailablePointsByMemId(mmt.getMemId());
			// 获取可使用余额
			Double useConsumeMoney = accountReportService.getAvailableBalance(mmt.getMemId());

			// 根据订单查询冻结余额
			List<MSAccountFreezeDetail> moneyFreezeList = accountFreezeDetailService.queryRecordByOrderId(mmt.getOrderId());

			logger.info("当前可使用积分：" + consumePoints + "，可使用余额：" + useConsumeMoney);

			// 只做冻结就返回，不做订单保存
			if ("1".equals(mmt.getFreeType())) {

				// 增加美兑积分逻辑 2016-10-31 美兑积分冻结
				if (isZero(mmt.getConsumePointsCount())) {
					logger.info("订单编号：{} ,进入积分冻结方法.", mmt.getOrderId());
					// 判断积分余额
					if (Double.valueOf(consumePoints) < Double.valueOf(mmt.getConsumePointsCount())) {
						logger.info("积分余额不足");
						return new ResBodyData(ConstApiStatus.NOT_ENOUGH_POINTS, ConstApiStatus.getZhMsg(ConstApiStatus.NOT_ENOUGH_POINTS));
					}
					// 检查重复冻结
					if (pointsList.size() > 0) {
						logger.info("重复提交的冻结订单" + mmt.getOrderId() + ";冻结" + mmt.getConsumePointsCount());
						return new ResBodyData(ConstApiStatus.REPEAT_FREEZ_ORDER, ConstApiStatus.getZhMsg(ConstApiStatus.REPEAT_FREEZ_ORDER));
					}
					// 写入积分冻结表
					accountFreezeDetailService.saveFreezePoints(mmt.getMemId(), mmt.getOrderId(), 
							mmt.getConsumePointsCount(), ConstPointsChangeType.POINTS_FREEZE_TYPE_DJ.getCode(),
							ConstPointsChangeType.POINTS_FREEZE_TYPE_DJ.getName());
				}
				
				// 增加账户余额支付逻辑 2017-03-02
				if (isZero(mmt.getShoppingCouponCount())) {
					logger.info("订单编号：" + mmt.getOrderId() + ",进入余额冻结方法.");
					// 判断余额
					if (Double.valueOf(useConsumeMoney) < Double.valueOf(mmt.getShoppingCouponCount())) {
						logger.info("账户余额不足，当前可使用账户余额为：" + mmt.getShoppingCouponCount());
						return new ResBodyData(ConstApiStatus.NOT_ENOUGH_POINTS, ConstApiStatus.getZhMsg(ConstApiStatus.NOT_ENOUGH_POINTS));
					}
					// 检查重复冻结
					if (moneyFreezeList.size() > 0) {
						logger.info("重复提交的冻结订单" + mmt.getOrderId() + ";冻结" + mmt.getShoppingCouponCount());
						return new ResBodyData(ConstApiStatus.REPEAT_FREEZ_ORDER, ConstApiStatus.getZhMsg(ConstApiStatus.REPEAT_FREEZ_ORDER));
					}
					// 写入余额冻结
					moneyService.freezeMoneyAndAddRecord(mmt.getMemId(), Double.valueOf(mmt.getShoppingCouponCount()), mmt.getOrderId(), mmt.getOrderSource());
				}
				logger.info(mmt.getOrderId() + ";冻结积分：" + mmt.getConsumePointsCount() + ";冻结余额："
						+ mmt.getShoppingCouponCount());

			} // 只做解冻动作，不作保存，但解冻前检查这个订单号是否有过冻结历史，有的话就冻结，没有直接打回
			else if ("2".equals(mmt.getFreeType())) {

				// 增加美兑积分逻辑 2016-10-31 美兑积分解冻
				if (isZero(mmt.getConsumePointsCount())) {
					logger.info("订单编号：" + mmt.getOrderId() + ",进入积分解冻方法.");
					if (pointsList.size() > 0) {
						// 检查重复解冻
						if (pointsList.size() > 1) {
							logger.info("重复提交的冻结订单" + mmt.getOrderId() + ";冻结" + mmt.getConsumePointsCount());
							return new ResBodyData(ConstApiStatus.REPEAT_FREEZ_ORDER, ConstApiStatus.getZhMsg(ConstApiStatus.REPEAT_FREEZ_ORDER));
						}
						// 解冻金额和冻结金额是否一样
						if (DoubleCalculate.add(Double.valueOf(mmt.getConsumePointsCount()),
								Double.valueOf(pointsList.get(0).getMcpfConsumePoints())) != 0.0) {
							logger.info("订单解冻积分不等于冻结积分");
							return new ResBodyData(ConstApiStatus.DJ_NOT_EQUALS_DJ, ConstApiStatus.getZhMsg(ConstApiStatus.DJ_NOT_EQUALS_DJ));
						}
						// 写入积分冻结表
						accountFreezeDetailService.saveUnFreezePoints(mmt.getMemId(), mmt.getOrderId(), mmt.getConsumePointsCount(),
								ConstPointsChangeType.POINTS_FREEZE_TYPE_JD.getCode(), ConstPointsChangeType.POINTS_FREEZE_TYPE_JD.getName());
						
						logger.info(mmt.getOrderId() + ";解冻" + mmt.getConsumePointsCount());
					} else {
						logger.info("没有冻结的积分记录");
						return new ResBodyData(ConstApiStatus.NO_DJ_POINTS, ConstApiStatus.getZhMsg(ConstApiStatus.NO_DJ_POINTS));
					}
				}
				// 增加账户余额支付逻辑 2017-03-02
				if (isZero(mmt.getShoppingCouponCount())) {
					logger.info("订单编号：" + mmt.getOrderId() + ",进入余额解冻方法.");
					if (moneyFreezeList.size() > 0) {
						// 检查重复解冻
						if (moneyFreezeList.size() > 1) {
							logger.info("重复提交的冻结订单" + mmt.getOrderId() + ";冻结" + mmt.getShoppingCouponCount());
							return new ResBodyData(ConstApiStatus.REPEAT_FREEZ_ORDER, ConstApiStatus.getZhMsg(ConstApiStatus.REPEAT_FREEZ_ORDER));
						}
						// 解冻金额和冻结金额是否一样
						if (DoubleCalculate.sub(Double.valueOf(mmt.getShoppingCouponCount()),
								Double.valueOf(moneyFreezeList.get(0).getTradeAmount())) != 0.0) {
							logger.info("订单解冻余额不等于冻结余额!");
							return new ResBodyData(ConstApiStatus.MONEY_DJ_NOT_EQUALS_DJ, ConstApiStatus.getZhMsg(ConstApiStatus.MONEY_DJ_NOT_EQUALS_DJ));
						}
						// 写入积分解冻表
						accountAdjustService.cutConsumeFreezeMoneyAndDetail(mmt.getMemId(), mmt.getOrderId(), ConstTradeType.TRADE_TYPE_TKQX.getCode(),
								new Date(), mmt.getShoppingCouponCount(), ConstTradeType.TRADE_TYPE_TKQX.getName());

						logger.info(mmt.getOrderId() + ";解冻" + mmt.getShoppingCouponCount());
					} else {
						logger.info("没有冻结的余额记录");
						return new ResBodyData(ConstApiStatus.NO_DJ_MONEY, ConstApiStatus.getZhMsg(ConstApiStatus.NO_DJ_MONEY));
					}
				}

			} // 类型什么都不传，但pay_type是1或2，检查这个订单号是否有冻结记录，如果有就要作解冻动作，再保存订单
			else {
				// 判断支付方式
				if ("1".equals(mmt.getPayType()) || "2".equals(mmt.getPayType())) {

					// 美兑积分，解冻
					if (isZero(mmt.getConsumePointsCount())) {
						logger.info("订单编号：" + mmt.getOrderId() + "其他支付方式,进入积分解冻方法.");
						// 保存订单前判断是否有冻结数据 有就先解冻
						if (pointsList.size() == 1) {
							// 解冻金额和冻结金额是否一样
							if (DoubleCalculate.add(Double.valueOf(mmt.getConsumePointsCount()),
									Double.valueOf(pointsList.get(0).getMcpfConsumePoints())) != 0.0) {
								logger.info("订单解冻积分不等于冻结积分!");
								return new ResBodyData(ConstApiStatus.DJ_NOT_EQUALS_DJ, ConstApiStatus.getZhMsg(ConstApiStatus.DJ_NOT_EQUALS_DJ));
							}
							// 写入积分冻结表，解冻
							accountFreezeDetailService.saveUnFreezePoints(mmt.getMemId(), mmt.getOrderId(), mmt.getConsumePointsCount(),
									ConstPointsChangeType.POINTS_FREEZE_TYPE_JD.getCode(), ConstPointsChangeType.POINTS_FREEZE_TYPE_JD.getName());
						}
					}
					// 美兑积分不为空时进入
					if (!StringUtil.isEmptyByString(mmt.getConsumePointsCount())) {
						logger.info("订单编号：" + mmt.getOrderId() + "其他支付方式,进入积分扣除方法.");
						// 判断积分余额
						if (isZero(mmt.getConsumePointsCount())
								&& consumePoints < Double.valueOf(mmt.getConsumePointsCount())) {
							logger.info("积分余额不足无法支付-1，当前积分：" + consumePoints + "，交易积分：" + mmt.getConsumePointsCount());
							return new ResBodyData(ConstApiStatus.NOT_ENOUGH_POINTS, ConstApiStatus.getZhMsg(ConstApiStatus.NOT_ENOUGH_POINTS));
						} else {
							// 执行扣除积分余额
							if (isZero(mmt.getConsumePointsCount())) {
								// 扣除账户信息表中积分余额
								boolean returnBool = false;
								try {
									returnBool = accountAdjustService.cutMDConsumePointsAndDetail(
											mmt.getMemId(), mmt.getConsumePointsCount(),mmt.getOrderId(), mmt.getOrderSource(),
											ConstPointsFinalType.POINTS_OPERATOR_TYPE_XF, mmt.getMemId(), 
											SerialStringUtil.getPointsRemark(ConstPointsFinalType.POINTS_OPERATOR_TYPE_XF,mmt.getMemId()));
								} catch (MdSysException e) {
									throw new ServiceException(ConstApiStatus.SYSTEM_ERROR);
								}
								
								if (!returnBool) {
									logger.info("积分余额不足无法支付-2，当前积分：" + consumePoints + "，交易积分："
											+ mmt.getConsumePointsCount());
									return new ResBodyData(ConstApiStatus.NOT_ENOUGH_POINTS, ConstApiStatus.getZhMsg(ConstApiStatus.NOT_ENOUGH_POINTS));
								}
							}
						}
					} else {
						logger.info("积分余额不足无法支付");
						return new ResBodyData(ConstApiStatus.NOT_ENOUGH_POINTS, ConstApiStatus.getZhMsg(ConstApiStatus.NOT_ENOUGH_POINTS));
					}

					// 增加账户余额支付逻辑 2017-03-02
					if (isZero(mmt.getShoppingCouponCount())) {
						logger.info("订单编号：" + mmt.getOrderId() + "其他支付方式,进入余额解冻方法.");
						// 保存订单前判断是否有冻结数据 有就先解冻
						if (moneyFreezeList.size() == 1) {
							// 解冻金额和冻结金额是否一样
							if (DoubleCalculate.sub(Double.valueOf(mmt.getShoppingCouponCount()),
									Double.valueOf(moneyFreezeList.get(0).getTradeAmount())) != 0.0) {
								logger.info("其他支付方式，订单解冻余额不等于余额!");
								return new ResBodyData(ConstApiStatus.MONEY_DJ_NOT_EQUALS_DJ, ConstApiStatus.getZhMsg(ConstApiStatus.MONEY_DJ_NOT_EQUALS_DJ));
							}
							MSAccountReport accountMoney = accountReportService.getTotalAndFreezeBalanceByMemId(mmt.getMemId());
							// 扣减余额与冻结余额
							accountAdjustService.cutFreezeMoneyAndCutMoney(mmt.getMemId(), mmt.getShoppingCouponCount(), mmt.getShoppingCouponCount());
							
							// 写入余额解冻明细
							Double freezeBalance = DoubleCalculate.sub(Double.valueOf(accountMoney.getFreezeBalance()),
									Double.valueOf(mmt.getShoppingCouponCount()));
							accountFreezeDetailService.saveAccountUnFreezeDetail(mmt.getMemId(), mmt.getOrderId(),
									accountMoney.getId(), mmt.getPayType(),
									ConstTradeType.TRADE_TYPE_YEXF.getCode(), mmt.getShoppingCouponCount(),
									new Date(), String.valueOf(freezeBalance), "余额消费");
							// 写入扣除余额明细
							logger.info("订单编号：" + mmt.getOrderId() + "其他支付方式,进入余额扣除方法.");
							Double balance = DoubleCalculate.sub(Double.valueOf(accountMoney.getBalance()),
									Double.valueOf(mmt.getShoppingCouponCount()));
							accountDetailService.saveCutAccountDetail(mmt.getMemId(), mmt.getOrderId(),
									accountMoney.getId(), mmt.getPayType(),
									ConstTradeType.TRADE_TYPE_YEXF.getCode(), mmt.getShoppingCouponCount(),
									new Date(), String.valueOf(balance), "余额消费");
						} else {
							logger.info("余额不足无法支付");
							return new ResBodyData(ConstApiStatus.NOT_ENOUGH_POINTS, ConstApiStatus.getZhMsg(ConstApiStatus.NOT_ENOUGH_POINTS));
						}
					}

				}
				memberConsumeHistoryService.save(memConHis);

				Double beforeCouponsBalance = Double.parseDouble("0");
				Double endCouponsBalance = Double.parseDouble("0");
				// 美兑积分
				Double beforeConsumePoints = DoubleCalculate.add(consumePoints, Double
						.parseDouble(mmt.getConsumePointsCount() == null ? "0" : mmt.getConsumePointsCount())); // 扣除积分前余额=当前余额+扣除积分
				// 账户余额
				Double beforeShoppingBalance = DoubleCalculate.add(useConsumeMoney, Double
						.parseDouble(mmt.getShoppingCouponCount() == null ? "0" : mmt.getShoppingCouponCount())); // 扣除前余额=当前余额+扣除余额

				result.put("beforeCouponsBalance", beforeCouponsBalance);
				result.put("endCouponsBalance", endCouponsBalance);
				result.put("beforeShoppingBalance", beforeShoppingBalance);
				result.put("endShoppingBalance", useConsumeMoney);
				result.put("beforeConsumePointsBalance", beforeConsumePoints);
				result.put("endConsumePointsBalance", consumePoints);
			}
			logger.info("操作成功");
			return new ResBodyData(ConstApiStatus.SUCCESS, ConstApiStatus.SUCCESS_M, result);
		}

	}
	
	
	
	/**
	 * 方法名: isZero<br>
	 * 描述: 判断是否为零，如果为空返回false，如果不为数字返回 false，如果等于0返回 false，大于0返回true <br>
	 * 创建时间: 2017-3-24
	 * @param obj
	 * @return
	 */
	private boolean isZero(Object obj){
		if(obj == null){
			return false;
		}
		try{
			if(Double.valueOf(obj.toString()) > 0){
				return true;
			}
		}catch(Exception e){
			return false;
		}
		return false;
	}

}
