package com.meiduimall.service.settlement.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.meiduimall.service.settlement.common.CodeRuleUtil;
import com.meiduimall.service.settlement.common.ShareProfitConstants;
import com.meiduimall.service.settlement.common.ShareProfitUtil;
import com.meiduimall.service.settlement.model.EcmAgent;
import com.meiduimall.service.settlement.model.EcmMzfAccount;
import com.meiduimall.service.settlement.model.EcmMzfAgentWater;
import com.meiduimall.service.settlement.model.EcmMzfStoreRecord;
import com.meiduimall.service.settlement.model.EcmMzfWater;
import com.meiduimall.service.settlement.model.EcmStore;
import com.meiduimall.service.settlement.model.EcmSystemSetting;
import com.meiduimall.service.settlement.model.ShareProfitAgentLog;
import com.meiduimall.service.settlement.service.AgentService;
import com.meiduimall.service.settlement.service.BeanSelfAware;
import com.meiduimall.service.settlement.service.DepositService;
import com.meiduimall.service.settlement.service.MemberService;
import com.meiduimall.service.settlement.service.O2oCallbackService;
import com.meiduimall.service.settlement.task.AsyncTaskService;


@Service
public class DepositServiceImpl implements DepositService, BeanSelfAware {
	
	private static final Logger logger = LoggerFactory.getLogger(DepositServiceImpl.class);
	
	@Autowired
	private MemberService scoreService;

	@Autowired
	private AgentService agentService;
	
	@Autowired
	private DepositService proxySelf;
	
	@Autowired
	private AsyncTaskService asyncTaskService;
	
	@Autowired
	private O2oCallbackService o2oCallbackService;
	
	@Override
	public void setSelf(Object proxyBean) {
		this.proxySelf = (DepositService) proxyBean;
	}

	
	@Override
	public List<Map<String, Object>> shareDeposit(EcmAgent ecmAgent) throws Exception {

		logger.info("分账开始时间:"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

		//查询基本分润配置
		List<EcmSystemSetting> settingList = agentService.quertSharefit();
		Map<String, String> systemSetting = ShareProfitUtil.queryShareProfit(settingList);
		
		//返回结果
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
				
		try {
			
			//调用分润主方法（事务在主方法中,控制单条个代业务逻辑事务）
			this.proxySelf.shareDepositMain(ecmAgent, systemSetting);
			
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("agentNo", ecmAgent.getAgentNo());
			map.put("billStatus", "1");
			resultList.add(map);
			
		} catch (Exception e) {
			logger.error("个代保证金分润：{}", e.toString());
		}
		
		return resultList;
	}

	/**
	 * 分润主方法
	 * @param ecmAgent 个代对象 用于分保证金
	 * @param systemSetting 分润比例
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void shareDepositMain(EcmAgent ecmAgent, Map<String, String> systemSetting) throws Exception {
		
		int score = Integer.parseInt(systemSetting.get(ShareProfitConstants.NEWBIE_PERSON_POINT));//新加盟个代获得积分 6500
		double areaToPersonRate = Double.parseDouble(systemSetting.get(ShareProfitConstants.AREA_TO_PERSON_RATE));//区代直推个代代理费分成  50%
		double personToPersonRecRate = Double.parseDouble(systemSetting.get(ShareProfitConstants.PERSON_TO_PERSON_REC_RATE));//个代推个代代理费  推荐人获取代理费分成比例 20%
		double personToPersonAreaRate = Double.parseDouble(systemSetting.get(ShareProfitConstants.PERSON_TO_PERSON_AREA_RATE));//个代推个代    创建人 即区代  获取代理费分成比例 30%
		
		String remark = null;//推荐时备注内容
		double areaToPersonDeposit = ecmAgent.getCashDeposit() * areaToPersonRate;//区代推荐个代，区代获取50%的保证金
		double personalToPersonRecDeposit = ecmAgent.getCashDeposit() * personToPersonRecRate;//个代推个代  推荐人获取保证金的20%(若推荐人是个代则20%账单不发出，若推荐人区代20%账单发出)
		double personToPersonAreaDeposit = ecmAgent.getCashDeposit() * personToPersonAreaRate;//个代推个代  创建人获取保证金的30%
		
		ecmAgent.setOpTime(new Timestamp(System.currentTimeMillis()));//操作日期(代理流水、总流水保持一致)
		
		//区代推荐个代（推荐人编号长度为6则为区代）
		if(ecmAgent.getRecommenderCode().length() == 6){
			
			logger.info("区代推荐个代，区代编码：" + ecmAgent.getRecommenderCode());
			
			remark = ShareProfitConstants.REMARK_1_TYPE;//备注内容为区代推荐个代
			
			//判断区代公司名称是否是"美兑壹购物"，如果不是则插入区代数据（流水金额）
			if(!ecmAgent.getAddCompanyName().equals(ShareProfitConstants.COMPANY_NAME)){
				
				logger.info("===============区代推荐个代，插入区代获取保证金的流水start===============");
				
				//如果未缴清保证金余款 则生成两条代理流水30%和20%
				if(ecmAgent.getAddDepositLeftAmount() != 0){
					logger.info("区代直推个代，区代未缴清保证金余款，生成两条流水20%和30%");
					addWater(ecmAgent.getId(), CodeRuleUtil.getAreaAgentFlowCode(ecmAgent.getRecommenderCode()), ecmAgent.getRecommenderCode(),
							personalToPersonRecDeposit, 0, ecmAgent.getRecommenderPhone(), ShareProfitConstants.ROLE_TYPE_AREA_AGENT, (int)(personToPersonRecRate*100), ecmAgent.getRecNo(), ecmAgent.getOpTime());
					logger.info("区代作为推荐人获得20%保证金：{}", personalToPersonRecDeposit);
					
					addWater(ecmAgent.getId(), CodeRuleUtil.getAreaAgentFlowCode(ecmAgent.getRecommenderCode()), ecmAgent.getRecommenderCode(),
							personToPersonAreaDeposit, 0, ecmAgent.getRecommenderPhone(), ShareProfitConstants.ROLE_TYPE_AREA_AGENT, (int)(personToPersonAreaRate*100), ecmAgent.getRecNo(), ecmAgent.getOpTime());
					logger.info("区代作为创建人获得30%保证金：{}", personToPersonAreaDeposit);
				}else{//生成一条代理流水50%
					addWater(ecmAgent.getId(), CodeRuleUtil.getAreaAgentFlowCode(ecmAgent.getRecommenderCode()), ecmAgent.getRecommenderCode(),
							areaToPersonDeposit, 0, ecmAgent.getRecommenderPhone(), ShareProfitConstants.ROLE_TYPE_AREA_AGENT, (int)(areaToPersonRate*100), ecmAgent.getRecNo(), ecmAgent.getOpTime());
					logger.info("区代获得50%保证金：{}", areaToPersonDeposit);
				}
				
				logger.info("===============区代推荐个代，插入区代获取保证金的流水end===============");
			}
			
			
			//插入被推荐个代数据（积分）
			
			logger.info("===============区代推荐个代，插入个代获得积分start===============");
			
			addWater(ecmAgent.getId(), CodeRuleUtil.getPersonalAgentFlowCode(ecmAgent.getAgentNo()), ecmAgent.getAgentNo(), 0, score,
					ecmAgent.getBindPhone(), ShareProfitConstants.ROLE_TYPE_PERSONAL_AGENT, 0, ecmAgent.getRecNo(), ecmAgent.getOpTime());
			
			logger.info("===============区代推荐个代，插入个代获得积分end===============");

			
		}else{//个代推荐个代
			
			logger.info("个代推荐个代，个代编码：{}", ecmAgent.getRecommenderCode());
			
			remark = ShareProfitConstants.REMARK_2_TYPE;//备注内容为个代推荐个代
			
			//插入区代数据（流水金额）
			
			//判断区代公司名称是否是"美兑壹购物"，如果不是则插入区代数据（流水金额）
			if(!ecmAgent.getAddCompanyName().equals(ShareProfitConstants.COMPANY_NAME)){
				
				if(!StringUtils.isEmpty(ecmAgent.getAddAgentNo()) && !StringUtils.isEmpty(ecmAgent.getAddBindPhone())){
					
					logger.info("===============个代推荐个代，插入创建人区代获取保证金的流水start===============");
					logger.info("个代推荐个代，创建人区代获取30%代理费");
					
					addWater(ecmAgent.getId(), CodeRuleUtil.getAreaAgentFlowCode(ecmAgent.getAddAgentNo()), ecmAgent.getAddAgentNo(),
							personToPersonAreaDeposit, 0, ecmAgent.getAddBindPhone(), ShareProfitConstants.ROLE_TYPE_AREA_AGENT, (int)(personToPersonAreaRate*100), ecmAgent.getRecNo(), ecmAgent.getOpTime());
					logger.info("创建人区代获得30%保证金：{}", personToPersonAreaDeposit);
					
					logger.info("===============个代推荐个代，插入创建人区代获取保证金的流水end===============");
				}
			}
			
			//插入推荐个代数据（流水金额）
			
			logger.info("===============个代推荐个代，插入推荐人个代的保证金流水start===============");
			
			addWater(ecmAgent.getId(), CodeRuleUtil.getPersonalAgentFlowCode(ecmAgent.getRecommenderCode()), ecmAgent.getRecommenderCode(),
					personalToPersonRecDeposit, 0, ecmAgent.getRecommenderPhone(), ShareProfitConstants.ROLE_TYPE_PERSONAL_AGENT, (int)(personToPersonRecRate*100), ecmAgent.getRecNo(), ecmAgent.getOpTime());
			
			logger.info("推荐人个代获得20%保证金：{}", personalToPersonRecDeposit);
			logger.info("===============个代推荐个代，插入推荐人个代的保证金流水end===============");
			
			//插入被推荐个代数据（积分）
			
			logger.info("===============个代推荐个代，插入个代获得积分start===============");
			
			addWater(ecmAgent.getId(), CodeRuleUtil.getPersonalAgentFlowCode(ecmAgent.getAgentNo()), ecmAgent.getAgentNo(),
					0, score, ecmAgent.getBindPhone(), ShareProfitConstants.ROLE_TYPE_PERSONAL_AGENT, 0, ecmAgent.getRecNo(), ecmAgent.getOpTime());
			
			logger.info("===============个代推荐个代，插入个代获得积分end===============");

		}
		
		//更新账户余额（个代或区代账户，个代、区代账户由o2o平台创建）
		boolean flag = updateAccountBalance(ecmAgent,remark,ShareProfitConstants.WATER_TYPE_AGENT_PROFIT);
		if(flag){
			
			//当区代公司名称不为'美兑壹购物'
			if(!ecmAgent.getAddCompanyName().equals(ShareProfitConstants.COMPANY_NAME)){
				
				//区代保证金余款不等于0大于或小于30%（区代所获30%代理费抵扣保证金）
				offsetDeposit(personToPersonAreaDeposit, ecmAgent);
				
			}else{
				logger.info("区代公司名称是'美兑壹购物'不执行代理费抵扣保证金业务");
			}
			
			//给新个代送积分
			asyncTaskService.updateScore(ecmAgent, score, ShareProfitConstants.SHARE_PROFIT_SOURCE_O2O, null);
			
		}else{
			logger.error("更新账户余额失败");
			throw new Exception("更新账户余额失败");
		}
	}

	/**
	 * 区代推荐个代所得推荐费用于抵扣履约保证金
	 * @param personToPersonAreaDeposit 30%推荐费
	 * @param areaAgent 区代信息
	 * @throws Exception
	 */
	private void offsetDeposit(double personToPersonAreaDeposit, EcmAgent areaAgent) throws Exception {
		try {
			if(areaAgent.getAddDepositLeftAmount() != 0){
				//如果区代保证金余款>30%
				double amount;
				//当余款大于30%时，直接扣除30%的保证金
				if(areaAgent.getAddDepositLeftAmount() > personToPersonAreaDeposit){
					amount = personToPersonAreaDeposit;
				}else{//当余款小于30%时，直接扣除剩下的所有余款
					amount = areaAgent.getAddDepositLeftAmount();
				}
				
				//更新代理表(ecm_agent)中区代的余款 depositLeftAmount-amount
				//插入抵扣保证金到缴费记录表中
				//回调php接口 更新代理表中区代的余款  插入抵扣保证金到缴费记录表中
				
				String payinId = o2oCallbackService.addProxyFee(areaAgent, amount);
				
				
				//ecm_mzf_water流水表扣除30%区代获取的代理费  类型为保证金
				EcmMzfWater water = new EcmMzfWater();
				water.setWaterId(CodeRuleUtil.getAreaAgentFlowCode(areaAgent.getAddAgentNo()));//生成区代流水编号
				water.setCode(areaAgent.getAddAgentNo());
				water.setMoney(new BigDecimal((-amount)));
				water.setRemark(ShareProfitConstants.REMARK_3_TYPE);//备注内容 推荐费抵扣;

				water.setWaterType(ShareProfitConstants.WATER_TYPE_DEPOSIT);//保证金
				water.setExtId(payinId);//ecm_agent_payin表中的id
				water.setOpTime(areaAgent.getOpTime());
				int waterFlag = agentService.insertWater(water);
				
				//更新账户余额 
				EcmMzfAccount account = new EcmMzfAccount();
				account.setCode(areaAgent.getAddAgentNo());
				account.setBalance(new BigDecimal((-amount)));
				int accountFlag = agentService.updateAccount(account);
				
				if(waterFlag > 0 && accountFlag > 0){
					logger.info("区代30%代理费抵扣保证金成功");
				}else{
					logger.error("区代30%代理费抵扣保证金失败");
					throw new Exception("区代30%代理费抵扣保证金失败");
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
			throw e;
		}
	}

	
	/**
	 * 更新账户余额
	 * @param code 代理编码
	 * @param type 流水类型 1-提现、2-账单、3-代理费
	 * @return
	 * @throws Exception
	 */
	private boolean updateAccountBalance(EcmAgent ecmAgent, String remark, String type) throws Exception {
		boolean flag = false;
		try {
			
			//当区代推荐个代时，创建人和推荐人都为当前区代  若区代公司名称为"美兑壹购物"时不产生任何代理流水，所以无需更新账户 (为满足区代30%代理费抵扣保证金 新增的判断)
			if(ecmAgent.getRecommenderCode().length() == 6 && ecmAgent.getAddCompanyName().equals(ShareProfitConstants.COMPANY_NAME)){

				logger.info("创建人区代公司名称是'美兑壹购物'不生成ecm_mzf_water流水，不更新账户余额");
				return true;
			}
			
			//根据code和id查询代理流水表（ecm_mzf_agent_water）中的记录
			List<EcmMzfAgentWater> agentWaters = agentService.findAgentWaterByAgentCode(ecmAgent.getId());
			
			if (!CollectionUtils.isEmpty(agentWaters)) {
				
				int updateCount = 0;
				int insertCount = 0;
				
				for (int i = 0; i < agentWaters.size(); i++) {
					EcmMzfAgentWater agentWater = agentWaters.get(i);
					
					if(agentWater.getMoney() > 0){
						
						//根据code查询account是否存在
						EcmMzfAccount ecmMzfAccount = agentService.findAccountByCode(agentWater.getCode());
						
						if(ecmMzfAccount != null){
							//更新账户余额（ecm_mzf_account）
							EcmMzfAccount account = new EcmMzfAccount();
							account.setCode(agentWater.getCode());
							account.setBalance(new BigDecimal(agentWater.getMoney()));
							updateCount = agentService.updateAccount(account);
							updateCount++;
							logger.info("更新账户余额参数：{},{}", agentWater.getCode(), agentWater.getMoney());
							
							//插入代理流水（ecm_mzf_water）
							EcmMzfWater water = new EcmMzfWater();
							String flowCode = null;
							if(agentWater.getType() == ShareProfitConstants.ROLE_TYPE_AREA_AGENT){//角色类型 1-区代、2-个代
								flowCode = CodeRuleUtil.getAreaAgentFlowCode(agentWater.getCode());//生成区代流水编号
							}else{
								flowCode = CodeRuleUtil.getPersonalAgentFlowCode(agentWater.getCode());//生成个代流水编号
							}
							water.setWaterId(flowCode);
							water.setCode(agentWater.getCode());
							water.setMoney(new BigDecimal(agentWater.getMoney()));
							water.setRemark(remark);
							water.setWaterType(type);
							water.setExtId(agentWater.getAgentWaterId());
							water.setOpTime(ecmAgent.getOpTime());
							insertCount = agentService.insertWater(water);
							insertCount++;
							logger.info("插入代理流水参数：" + water.toString());
						}else{
							logger.error("代理编号为：{}的账户不存在,无法更新账户余额", agentWater.getCode());
							throw new Exception("代理编号为："+agentWater.getCode()+"的账户不存在,无法更新账户余额");
						}
						
					}
				}
	
				if(updateCount > 0 && insertCount > 0){
					logger.info("更新账户余额成功");
					flag = true;
				}

			}
		} catch (Exception e) {
			logger.error(e.toString());
			throw e;
		}
		return flag;
	}
	
	
	/**
	 * 插入异常日志
	 * @param agentLog
	 * @param retryType
	 * @throws Exception
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void shareProfitAgentLog(ShareProfitAgentLog agentLog, String retryType) throws Exception {
		int flag = agentService.insertShareProfitAgentLog(agentLog);
		if(flag <= 0){
			logger.error("更新积分失败时，插入异常日志记录失败");
		}
		if(ShareProfitConstants.SHARE_PROFIT_RETRY_TYPE_FINAL_ROUND.equals(retryType)){
			int flags = agentService.updateRetryFlag(agentLog.getAgentNo());
			if(flags <= 0){
				logger.error("修改重试标识为 '不需要重试'状态失败");
			}
		}
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public List<Map<String,Object>> updateStoreScore(EcmStore ecmStore) {
		
		logger.info("更新商家积分开始时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
		
		try {
			
			//查询基本分润配置
			List<EcmSystemSetting> settingList = agentService.quertSharefit();
			Map<String, String> systemSetting = ShareProfitUtil.queryShareProfit(settingList);
			//获取商家初始积分
			String score = systemSetting.get(ShareProfitConstants.STORE_INIT_POINT);
			
			if(!StringUtils.isEmpty(ecmStore.getUsername()) && !StringUtils.isEmpty(ecmStore.getStoreNo())){
				//商家积分流水号
				String scoreFlow = "EGW" + ecmStore.getStoreNo() + System.currentTimeMillis();
				//调用积分接口 更新积分 
				Boolean result = scoreService.addConsumePoints(ecmStore.getUsername(), score, ShareProfitConstants.DATA_SOURCE_O2O,scoreFlow);
				
				int scoreStatus = 0; //积分是否同步到会员系统  0-否，1-是
				if(result){
					scoreStatus = 1;
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("storeNo", ecmStore.getStoreNo());
					map.put("username", ecmStore.getUsername());
					map.put("pointStatus", "1");
					resultList.add(map);
					logger.info("商家为:{}更新积分成功", ecmStore.getUsername());
				}else{
					logger.error("商家为:{}更新积分失败", ecmStore.getUsername());
				}
				
				//插入商家送积分记录
				EcmMzfStoreRecord ecmMzfStoreRecord = new EcmMzfStoreRecord();
				Timestamp date = new Timestamp(System.currentTimeMillis());
				ecmMzfStoreRecord.setStoreNo(ecmStore.getStoreNo());
				ecmMzfStoreRecord.setPhone(ecmStore.getUsername());
				ecmMzfStoreRecord.setScore(Integer.parseInt(score));
				ecmMzfStoreRecord.setScoreStatus(scoreStatus);
				ecmMzfStoreRecord.setCreatedDate(date);
				ecmMzfStoreRecord.setUpdatedDate(date);
				agentService.insertStoreRecord(ecmMzfStoreRecord);
				
			}
		} catch (Exception e) {
			logger.error("更新商家积分：{}", e.toString());
		}
		return resultList;
	}
	
	
	
	/**
	 * 代理获取保证金或返还给个代积分
	 * @param flowCode 流水编号
	 * @param agentCode 代理编号
	 * @param agentDeposit 代理获得保证金
	 * @param score 返还给个代积分
	 * @param phone 手机号码
	 * @param type 角色类型 1-区代、2-个代
	 * @param agentRate 代理费比例
	 * @throws Exception 
	 */
	private void addWater(int agentId, String flowCode, String agentCode, double agentDeposit, int score, String phone,
			int type, int agentRate, String recNo, Timestamp opTime) throws Exception {
		EcmMzfAgentWater agentWater = new EcmMzfAgentWater();
		agentWater.setAgentId(agentId);
		agentWater.setAgentWaterId(flowCode);//流水编号
		agentWater.setCode(agentCode);//代理编码
		agentWater.setMoney(agentDeposit);//获得保证金
		agentWater.setScore(score);//返还积分
		agentWater.setPhone(phone);//区代手机号码
		agentWater.setType(type);//角色类型 1-区代、2-个代
		agentWater.setAgentRate(agentRate);//代理费比例
		agentWater.setRecNo(recNo);//推荐单号
		agentWater.setAgentWaterTime(opTime);//流水时间
		agentService.insertAgentWater(agentWater);
		logger.info("插入分润参数：{}", agentWater.toString());
	}

	@Override
	public int createAccount(EcmMzfAccount ecmMzfAccount) throws Exception {
		int flag = 0;
		//判断当前个代账户是否存在，如果不存在，则创建账户
		EcmMzfAccount mzfAccount = agentService.findAccountByCode(ecmMzfAccount.getCode());
		if(mzfAccount == null){
			//插入被推荐个代的账户信息
			logger.info("===============创建个代账户start===============");
			EcmMzfAccount account = new EcmMzfAccount();
			account.setCode(ecmMzfAccount.getCode());
			account.setAccountRoleType("2");
			flag = agentService.insertAccount(account);
		}else{
			logger.info("新个代{}账户已存在，不可重复创建账户", ecmMzfAccount.getCode());
		}
		return flag;
	}

	
}