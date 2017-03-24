package com.meiduimall.service.settlement.api;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.StringUtil;
import com.meiduimall.core.ResBodyData;
import com.meiduimall.service.settlement.common.DrawCashConstants;
import com.meiduimall.service.settlement.common.SettlementUtil;
import com.meiduimall.service.settlement.common.ShareProfitConstants;
import com.meiduimall.service.settlement.common.ShareProfitUtil;
import com.meiduimall.service.settlement.model.EcmMzfAccount;
import com.meiduimall.service.settlement.model.EcmMzfDraw;
import com.meiduimall.service.settlement.model.EcmSystemSetting;
import com.meiduimall.service.settlement.service.AgentService;
import com.meiduimall.service.settlement.service.DrawService;
import com.meiduimall.service.settlement.util.DateUtil;

@RestController
@RequestMapping("/settlementservice/drawservice/v1")
public class DrawController {
	
	private static final Logger log = LoggerFactory.getLogger(DrawController.class);
	
	@Autowired
	private DrawService drawService;
	
	@Autowired
	private AgentService agentService;
	
	/**
	 * 获取区代、个代或商家可提现金额
	 * @param code
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/queryaccoutbalance")
	public ResBodyData queryAccoutBalance(EcmMzfAccount ecmMzfAccount) throws Exception{
		
		Map<String, Object> ecmmzfaccount = drawService.queryAccoutBalance(ecmMzfAccount.getCode());
		return SettlementUtil.buildReponseData(ecmmzfaccount, 0, "成功");
		
	}
	
	/**
	 * 新增提现申请记录接口
	 * @param ecmMzfDraw
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/drawcash")
	public ResBodyData drawCash(EcmMzfDraw ecmMzfDraw) throws Exception {
		try {
			
			//提现手续费从配置表获取
			List<EcmSystemSetting> settingList = agentService.quertSharefit();
			Map<String, String> systemSetting = ShareProfitUtil.queryShareProfit(settingList);
			BigDecimal cashWithdrawalFee = new BigDecimal(systemSetting.get(ShareProfitConstants.CASH_WITHDRAWAL_FEE));
			
			//查询当天是否有提现记录
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("code", ecmMzfDraw.getCode());
			params.put("drawType", ecmMzfDraw.getDrawType());
			params.put("nowTime", DateUtil.formatDate(new Date()));
			int drawCount = drawService.getCountByCode(params);
			
			//如果当天是第一次提现无需收取手续费，否则要收取手续费
			if(drawCount > 0){
				ecmMzfDraw.setCashWithdrawalFee(cashWithdrawalFee);
				ecmMzfDraw.setTotalMoney(ecmMzfDraw.getMoney().add(cashWithdrawalFee));
				ecmMzfDraw.setRemark("手续费"+cashWithdrawalFee+"元");
			}else{
				ecmMzfDraw.setCashWithdrawalFee(new BigDecimal("0.00"));
				ecmMzfDraw.setTotalMoney(ecmMzfDraw.getMoney());
				ecmMzfDraw.setRemark("提现");
			}
			
			//根据code查询账户余额
			BigDecimal balance = null;
			Map<String, Object> account = drawService.queryAccoutBalance(ecmMzfDraw.getCode());
			if(account.get("balance") != null && !"".equals(account.get("balance"))){
				balance = new BigDecimal(account.get("balance").toString());
			}
			
			//如果提现总金额=提现金额+手续费 > 账户总金额时，返回提现金额不能大于账号可提现金额，请重新输入
			if(balance.compareTo(ecmMzfDraw.getTotalMoney()) == -1){//balance>totalMoney时返回1,-1是小于,0是等于
				return SettlementUtil.failure("", "提现金额不能大于账号可提现金额，请重新输入");
			}

			boolean result = drawService.insertDrawInfo(ecmMzfDraw);
			if(result){
				return SettlementUtil.success("", "申请提现成功");
			}
			return SettlementUtil.failure("", "申请提现失败");
			
		} catch (Exception e) {
			log.error(e.toString());
			return SettlementUtil.failure("", "申请提现失败");
		}
	}
	
	
	/**
	 * 获取提现管理列表
	 * @param pageNumber 页数
	 * @param pageSize 每页显示条数
	 * @param type(list,export)
	 * @param params(drawCode,code,drawType,realname,userType,addTime,status,drawName)
	 * @return
	 */
	@PostMapping(value = "/querydrawcash")
	public ResBodyData queryDrawCash(
			@RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
			@RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
			@RequestParam(value = "type", defaultValue = "list") String type,
			@RequestParam HashMap<String, Object> params) {
		try {
			if("list".equals(type)){
				PageHelper.startPage(pageNumber, pageSize);
			}
			List<EcmMzfDraw> ecmMzfDrawList = drawService.queryDrawCash(params);
			int total = drawService.getDrawCount(params);
			
			Map<String,Object> result = new HashMap<String,Object>();
			result.put("list", ecmMzfDrawList);
			result.put("total", total);
			return SettlementUtil.success(result, "获取流水列表成功");
			
		} catch (Exception e) {
			log.error(e.toString());
			return SettlementUtil.failure("", "获取提现列表失败");
		}
		
	}
	
	
	/**
	 * 根据提现编号获取提现详情
	 * @param drawCode 提现编号
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/querydrawcashbyid")
	public ResBodyData queryDrawCashById(String drawCode) throws Exception{
		EcmMzfDraw ecmmzfdraw = drawService.queryDrawCashById(drawCode);
		return SettlementUtil.buildReponseData(ecmmzfdraw, 0, "成功");
		
	}
	
	/**
	 * 审核提现申请
	 * @param drawCode
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/verifydrawcashbyid")
	public ResBodyData verifyDrawCashById(EcmMzfDraw ecmmzfdraw) throws Exception{
		
		Map<String, Object> hashMap=new HashMap<String,Object>();
		if(StringUtil.isEmpty(ecmmzfdraw.getDrawCode())){
			return SettlementUtil.buildReponseData("", 0, "接口参数drawCode为空!");
		}

		String msg="审核提现申请成功";
		ecmmzfdraw.setStatus(DrawCashConstants.STATUS_VERIFIED_SUCDESS);
		ecmmzfdraw.setVerifyName(StringUtil.isEmpty(ecmmzfdraw.getVerifyName())?"admin":ecmmzfdraw.getVerifyName());
		ecmmzfdraw.setVerifyStatus(DrawCashConstants.STATUS_VERIFIED_SUCDESS);
		ecmmzfdraw.setVerifyTime(DateUtil.getCurrentTimeSec());
		
		try {
			hashMap = drawService.verifyDrawCashById(ecmmzfdraw);
		} catch (Exception e) {
			msg="审核提现申请操作失败!";
			log.error("verifyDrawCashById() for drawCode:{} got error:{}",ecmmzfdraw.getDrawCode(),e.getMessage());
		}
		
		int statusCode=ShareProfitConstants.RESPONSE_STATUS_CODE_SUCCESS;
		if(hashMap==null || hashMap.isEmpty()){
			statusCode=ShareProfitConstants.RESPONSE_STATUS_CODE_FAILURE;
		}
		
		return SettlementUtil.buildReponseData(hashMap, statusCode, msg);

	}
	
	/**
	 * 驳回提现申请接口
	 * @param ecmMzfDraw
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/rejectdrawcashbyid")
	public ResBodyData rejectDrawCashById(EcmMzfDraw ecmmzfdraw) throws Exception{
		
		Map<String, Object> hashMap=new HashMap<String,Object>();
		if(StringUtil.isEmpty(ecmmzfdraw.getDrawCode()) || StringUtil.isEmpty(ecmmzfdraw.getRemark())){
			return SettlementUtil.buildReponseData("", 1, "接口参数drawCode或remark为空!");
		}
		
		String msg="驳回提现申请成功";
		ecmmzfdraw.setStatus(DrawCashConstants.STATUS_VERIFIED_REJECTED);
		ecmmzfdraw.setVerifyName(StringUtil.isEmpty(ecmmzfdraw.getVerifyName())?"admin":ecmmzfdraw.getVerifyName());
		ecmmzfdraw.setVerifyStatus(DrawCashConstants.STATUS_VERIFIED_REJECTED);
		ecmmzfdraw.setVerifyTime(DateUtil.getCurrentTimeSec());
		try {
			hashMap = drawService.rejectDrawCashById(ecmmzfdraw);
		} catch (Exception e) {
			msg="驳回提现申请操作失败!";
			log.error("rejectDrawCashById() for drawCode:{} got error:{}",ecmmzfdraw.getDrawCode(),e.getMessage());
		}
		
		int statusCode=ShareProfitConstants.RESPONSE_STATUS_CODE_SUCCESS;
		if(hashMap==null || hashMap.isEmpty()){
			statusCode=ShareProfitConstants.RESPONSE_STATUS_CODE_FAILURE;
		}
		
		return SettlementUtil.buildReponseData(hashMap, statusCode, msg);

	}
	
	/**
	 * 提现确认转账成功或失败接口（财务更改提现单状态）
	 * @param ecmMzfDraw
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/confirmdrawcashbyidbytype")
	public ResBodyData confirmDrawCashByIdByType(EcmMzfDraw ecmmzfdraw) throws Exception{
		
		String msg="提现确认转账成功";
		
		if(ecmmzfdraw.getType()!=null && ecmmzfdraw.getType()==1){  //1:转账成功；0：转账失败
			ecmmzfdraw.setStatus(DrawCashConstants.STATUS_TRANSFER_SUCCESS);
			ecmmzfdraw.setFinanceStatus(DrawCashConstants.STATUS_TRANSFER_SUCCESS);
		}else{
			if(StringUtil.isEmpty(ecmmzfdraw.getRemark())){
				return SettlementUtil.buildReponseData("", 1, "确认转账失败时，必须提供原因");
			}
			msg="提现确认转账失败";
			ecmmzfdraw.setStatus(DrawCashConstants.STATUS_TRANSFER_FAIL);
			ecmmzfdraw.setFinanceStatus(DrawCashConstants.STATUS_TRANSFER_FAIL);

		}
		ecmmzfdraw.setFinanceTime(DateUtil.getCurrentTimeSec());
		ecmmzfdraw.setFinanceName(StringUtil.isEmpty(ecmmzfdraw.getFinanceName())?"admin":ecmmzfdraw.getFinanceName());
		
		Map<String, Object> hashMap=new HashMap<String,Object>();
		try {
			hashMap = drawService.confirmDrawCashByIdByType(ecmmzfdraw);
			msg+="操作成功!";
		} catch (Exception e) {
			msg+="操作失败!";
			log.error("drawService.confirmDrawCashByIdByTyp() drawCode:{},got error:{}",ecmmzfdraw.getDrawCode(),e.getMessage());
		}
		
		int statusCode=ShareProfitConstants.RESPONSE_STATUS_CODE_SUCCESS;
		if(hashMap==null || hashMap.isEmpty()){
			statusCode=ShareProfitConstants.RESPONSE_STATUS_CODE_FAILURE;
		}
		
		return SettlementUtil.buildReponseData(hashMap, statusCode, msg);

		
		
	}
	
	
	
}