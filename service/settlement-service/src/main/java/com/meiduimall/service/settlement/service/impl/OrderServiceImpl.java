package com.meiduimall.service.settlement.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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

import com.google.common.collect.ImmutableMap;
import com.meiduimall.service.settlement.common.ShareProfitConstants;
import com.meiduimall.service.settlement.common.ShareProfitUtil;
import com.meiduimall.service.settlement.context.ShareProfitContext;
import com.meiduimall.service.settlement.dao.BaseMapper;
import com.meiduimall.service.settlement.model.EcmMzfOrderStatus;
import com.meiduimall.service.settlement.model.EcmMzfShareProfit;
import com.meiduimall.service.settlement.model.EcmOrder;
import com.meiduimall.service.settlement.model.EcmSystemSetting;
import com.meiduimall.service.settlement.service.BeanSelfAware;
import com.meiduimall.service.settlement.service.OrderService;
import com.meiduimall.service.settlement.util.ConnectionUrlUtil;
import com.meiduimall.service.settlement.util.DateUtil;
import com.meiduimall.service.settlement.vo.EcmMzfBillWaterVO;
import com.meiduimall.service.settlement.vo.ShareProfitVO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.StringUtil;

@Service
public class OrderServiceImpl implements OrderService,BeanSelfAware {
	
	private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
	
	@Autowired
	private BaseMapper baseMapper;
	
	/**
	 * spring声明式事务 同一类内方法调用事务失效
	 * //http://blog.csdn.net/jiesa/article/details/53438342
	 */
	@Autowired
	private OrderService proxySelf;  

	@Override
	public void setSelf(Object proxyBean) {
		this.proxySelf=(OrderService) proxyBean;
 
	}

/*	@Override
	public boolean shareProfit(EcmOrder ecmOrder) throws Exception {
		
		boolean isSuccess=true;
		final List<String> errors=new ArrayList<String>();
		EcmMzfShareProfit shareProfit=null;
		
		try{
			
			//计算订单分润数据
			shareProfit=buildShareProfit(ecmOrder,errors);
			if(errors!=null && errors.size()>0){
				log.info("orderSn:{},分润数据有错误:{}",ecmOrder.getOrderSn(),errors.toString());
				return false;
			}

		}catch(Exception e){
			log.error("buildShareProfit() got error:{}", e.getMessage(),e);
			return false;
		}
		
		
		try {
			this.proxySelf.saveShareProfit(shareProfit);
		} catch (Exception e) {
			isSuccess=false;
			log.error("doShareProfit() for orderSn:{} got error:{}",shareProfit.getOrderSn(),e.getMessage());
		}
		return isSuccess;
	}*/
	
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void saveShareProfit(EcmMzfShareProfit shareProfit) throws Exception {
		if(shareProfit!=null){
				
			Integer flag = baseMapper.insert(shareProfit, "ShareProfitMapper.shareProfit");

			if (flag <= 0) {
				log.error("OrderServiceImpl-->doShareProfit-->ShareProfitMapper.shareProfit-->分润数据插入失败!");
				throw new Exception("OrderServiceImpl-->insertShareProfit-->ShareProfitMapper.shareProfit-->分润数据插入失败!");
			} else {
				log.info("OrderServiceImpl-->doShareProfit-->ShareProfitMapper.shareProfit-->分润数据插入成功!");
				
				EcmMzfOrderStatus orderStatus=ShareProfitUtil.buildOrderStatusObj(shareProfit);
				Integer orderStatusCreated = baseMapper.insert(orderStatus, "EcmMzfOrderStatusMapper.createOrderStatus");
				
				if (orderStatusCreated <= 0) {
					log.error("OrderServiceImpl-->doShareProfit-->EcmMzfOrderStatusMapper.createOrderStatus-->创建订单状态数据失败!");
					throw new Exception("OrderServiceImpl-->doShareProfit-->EcmMzfOrderStatusMapper.createOrderStatus-->创建订单状态数据失败!");
				} else {
					log.info("OrderServiceImpl-->doShareProfit-->EcmMzfOrderStatusMapper.createOrderStatus-->创建订单状态数据成功!");
				}
			}
		}
	}
	
	

	public EcmMzfShareProfit buildShareProfit(EcmOrder ecmOrder,Collection<String> errors) throws Exception{

		log.info("buildShareProfit start:Current Date:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		// 查询基本分润配置
		List<EcmSystemSetting> list = baseMapper.selectList(null, "ShareProfitMapper.quertSharefit");
		Map<String, String> systemSetting = ShareProfitUtil.queryShareProfit(list);
		
		ShareProfitContext ctx=new ShareProfitContext();
		
		if(StringUtils.isEmpty(ecmOrder.getSellerName())){
			log.error("商家编号为空!略过该条数据！");
			errors.add("商家编号不能为空!");
		}
		// 商家让利折扣
		BigDecimal serviceRate =new BigDecimal(ecmOrder.getServiceFee());
		if (null == serviceRate || serviceRate.compareTo(new BigDecimal(0)) <= 0) {
			log.error("商家收益比例为空!略过该条数据");
			errors.add("商家收益比例不能为空!");
		}
		log.info("个代编号:" + ecmOrder.getAgentNoPersonal());
		log.info("订单编号:" + ecmOrder.getOrderSn());
		log.info("商家编号:" + ecmOrder.getSellerName());
		log.info("平台服务费:" + serviceRate);
		BigDecimal discount = new BigDecimal(100).subtract(serviceRate);
		if (null == ecmOrder.getOrderAmount() || ecmOrder.getOrderAmount().compareTo(new BigDecimal(0)) <= 0) {
			log.error("支付金额为空或为0!略过该条数据");
			errors.add("支付金额不能为空或为0!");
		}
		if (null == ecmOrder.getAgentNoRegion() || "".equals(ecmOrder.getAgentNoRegion())) {
			log.info("该订单区代为空!");
			errors.add("该订单区代不能为空!");
		}
		log.info("订单支付金额:" + ecmOrder.getOrderAmount());
		
		// 获取推荐人信息
		JSONObject resultJson = ConnectionUrlUtil.httpRequest(ShareProfitUtil.getBelongInfoUrl(ecmOrder.getBuyerName()), ShareProfitUtil.REQUEST_METHOD_POST, null);
		if (null == resultJson || "".equals(resultJson.toString())) {
			log.info("会员系统连接失败!略过该条数据");
			errors.add("从会员系统获取推荐人信息失败!");
		}
		// 判断返回是否成功,如果不成功则不理会
		Map<String, String> belongMap = null;
		if ("0".equals(resultJson.get("status_code"))) {
			belongMap = getlvlAndPhone(resultJson.getString("RESULTS"));
			log.info("推荐人信息:" + resultJson.getString("RESULTS"));
		} else {
			log.error("errcode:" + resultJson.get("status_code") + ";errmsg:" + resultJson.get("result_msg"));
		}
		
		// 平台分账 = 订单总金额 - 商家收益
		//BigDecimal platformRevenue = ecmOrder.getTotalFee().subtract(merchantRevenue);
		// 平台分账(即服务费) = 参与让利金额 * 店铺服务费率
		//因为PHP部门已经根据全额返利和部分返利这两种情况，经参与让利金额计算到ecm_order.rebate_amount字段，所以结算这边无需再判断是全额返利还是部分返利。
		BigDecimal platformRevenue =ecmOrder.getRebateAmount().multiply(serviceRate).divide(new BigDecimal(100));
		log.info("平台分账 = 参与让利金额("+ecmOrder.getRebateAmount()+") * 店铺服务费率("+serviceRate.divide(new BigDecimal(100))+"):"+platformRevenue);
		
		// 商家收益 = 订单总金额 * 商家收益比例 / 100%
		//BigDecimal merchantRevenue = ecmOrder.getTotalFee().multiply(discount).divide(new BigDecimal(100));
		//商家收益 = 订单支付金额 -平台分账(即服务费) 
		BigDecimal merchantRevenue = ecmOrder.getOrderAmount().subtract(platformRevenue);
		log.info("商家收益 = 订单支付金额 -平台分账(即服务费):"+merchantRevenue);
		
		//一级推荐人获得金额:参与让利金额 * 1%
		BigDecimal firstReferrerCash=ecmOrder.getRebateAmount().multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.FIRST_REFERRER_CASH_RATE)).divide(new BigDecimal(100)));
		
		// 消费者返积分 = 平台分账 * 5
		BigDecimal memberRevenue = platformRevenue.multiply(new BigDecimal(5));
		// 商家所获积分 = 消费者返回积分 * 20%
		BigDecimal sellerRevenue = memberRevenue.multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.SELLER_POINT_RATE)).divide(new BigDecimal(100)));
		// 推荐人获得积分 = 消费者返还积分 * 1%
		BigDecimal belongRevenue = memberRevenue.multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.BELONG_SCALE))).divide(new BigDecimal(100));
		// 本区区代分账 = 消费者返积分 * 本区区代分账比例
		BigDecimal areaAgentRevenue = null;
		// 跨区区代分账 = 消费者返积分 * 跨区区代分账比例
		BigDecimal crossAreaAgentRevenue = null;
		// 个代分账 = 消费者返积分 * 个代分账比例
		BigDecimal personAgentRevenue = null;
		// 是否是前200区代
		Boolean isTwoHundreAgentFlag = null;
		
		String personalAgentNo=ecmOrder.getAgentNoPersonal();
		String personalAgentType= ShareProfitUtil.getPersonalAgentType(personalAgentNo);
		
		ctx.setFirstReferrerCash(firstReferrerCash);
		ctx.setMerchantRevenue(merchantRevenue);
		ctx.setPlatformRevenue(platformRevenue);
		ctx.setMemberRevenue(memberRevenue);
		ctx.setSellerRevenue(sellerRevenue);
		ctx.setBelongRevenue(belongRevenue);
		ctx.setDiscount(discount);
		ctx.setBelongMap(belongMap);
		ctx.setSystemSetting(systemSetting);
		ctx.setPersonalAgentType(personalAgentType);
		
		if(log.isInfoEnabled()){
			log.info("个代类型:"+personalAgentType);
			log.info("商家收益:$" + merchantRevenue.setScale(2, BigDecimal.ROUND_HALF_UP));
			log.info("平台收益:$" + platformRevenue.setScale(2, BigDecimal.ROUND_HALF_UP));
			log.info("用户返还积分:" + memberRevenue.setScale(0, BigDecimal.ROUND_DOWN));
		}
		
		if(ShareProfitUtil.PERSONAL_AGENT_TYPE_DIRECT_SALE.equalsIgnoreCase(personalAgentType)){
			personAgentRevenue = BigDecimal.ZERO;
			areaAgentRevenue = BigDecimal.ZERO;
			crossAreaAgentRevenue=BigDecimal.ZERO;
			
		}else if(ShareProfitUtil.PERSONAL_AGENT_TYPE_BIG_REGION.equalsIgnoreCase(personalAgentType)){
			personAgentRevenue = memberRevenue.multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.PERSONAL_SCALE_FOR_BIG_REGION)).divide(new BigDecimal(100)));
			areaAgentRevenue = memberRevenue.multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.AREA_SCALE)).divide(new BigDecimal(100)));
			//大区个代：此个代无区域性，此个代推广的商家均为本区商家，无论商家的地址在哪儿
			crossAreaAgentRevenue=BigDecimal.ZERO;
			
			if(log.isInfoEnabled()){
				log.info("--大区个代信息--");
				log.info("大区个代分账比例:" + systemSetting.get(ShareProfitUtil.PERSONAL_SCALE_FOR_BIG_REGION) +"大区个代收益:$" + personAgentRevenue);
				log.info("大区个代商家区代分账比例:" + systemSetting.get(ShareProfitUtil.AREA_SCALE) +"大区个代商家区代收益:$" + areaAgentRevenue);
			}
			
		}else{
			// 商家跨区
			if (null != ecmOrder.getAgentNoRegionS() && !"".equals(ecmOrder.getAgentNoRegionS())) {
				areaAgentRevenue = memberRevenue.multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.AREA_SCALE)).divide(new BigDecimal(100)));
				personAgentRevenue = memberRevenue.multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.CROSS_PERSONAL_SCALE)).divide(new BigDecimal(100)));
				crossAreaAgentRevenue = memberRevenue.multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.CROSS_AREA_SCALE)).divide(new BigDecimal(100)));
			} else {
				personAgentRevenue = memberRevenue.multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.PERSONAL_SCALE)).divide(new BigDecimal(100)));
				
				Integer isTwoHundredAgent =ecmOrder.getIsTwoHundredArea();
				if (null == isTwoHundredAgent || "".equals(isTwoHundredAgent)) {
					log.info("前二百区代标识为空!略过该条数据");
					errors.add("前二百区代标识不能为空!");
				}
				// 判断区代是否为前200区代
				if (isTwoHundredAgent.equals(Integer.valueOf(1))) {
					areaAgentRevenue = memberRevenue.multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.TWO_AREA_SCALE)).divide(new BigDecimal(100)));
					isTwoHundreAgentFlag = true;
				} else {
					areaAgentRevenue = memberRevenue.multiply(new BigDecimal(systemSetting.get(ShareProfitUtil.AREA_SCALE)).divide(new BigDecimal(100)));
					isTwoHundreAgentFlag = false;
				}
	
			}
			
			if(log.isInfoEnabled()){
				log.info("--普通个代信息--");
				log.info("个代分账比例:" + systemSetting.get(ShareProfitUtil.PERSONAL_SCALE) + "%;个代收益:$" + personAgentRevenue.setScale(2, BigDecimal.ROUND_HALF_UP));
				log.info("区代分账比例:" + systemSetting.get(ShareProfitUtil.AREA_SCALE) + "%;区代收益:$" + areaAgentRevenue.setScale(2, BigDecimal.ROUND_HALF_UP));
				if (crossAreaAgentRevenue!=null) {
					log.info("跨区区代分账比例:" + systemSetting.get(ShareProfitUtil.CROSS_AREA_SCALE) + "%;跨区区代收益:$" + crossAreaAgentRevenue.setScale(2, BigDecimal.ROUND_HALF_UP));
				}
			}
		}
		ctx.setPersonAgentRevenue(personAgentRevenue);
		ctx.setAreaAgentRevenue(areaAgentRevenue);
		ctx.setCrossAreaAgentRevenue(crossAreaAgentRevenue);
		ctx.setIsTwoHundreAgentFlag(isTwoHundreAgentFlag);
		
		final List<String> meiduiCompanyAgentNos=new ArrayList<String>();

		if(!StringUtil.isEmpty(ecmOrder.getAgentNameRegion()) && ShareProfitConstants.COMPANY_NAME.equals(ecmOrder.getAgentNameRegion())){
			meiduiCompanyAgentNos.add(ecmOrder.getAgentNoRegion());
		}
		
		if(!StringUtil.isEmpty(ecmOrder.getAgentNameRegionS()) && ShareProfitConstants.COMPANY_NAME.equals(ecmOrder.getAgentNameRegionS())){
			meiduiCompanyAgentNos.add(ecmOrder.getAgentNoRegionS());
		}
		
		EcmMzfShareProfit ecmMzfShareProfit = buildShareProfitModel(ecmOrder,ctx,meiduiCompanyAgentNos);
		return ecmMzfShareProfit;
		
	}

	@Override
	public List<EcmMzfOrderStatus> queryOrderStatus(List<String> orderSns) throws Exception {
		
		return baseMapper.selectList(ImmutableMap.of("orderSns", orderSns), "EcmMzfOrderStatusMapper.queryorderstatus");

	}

	/**
	 * Description : 获取请求接口后的数据提取推荐人手机号
	 * Created By : Fkx 
	 * Creation Time : 2016-10-27 下午5:31:00 
	 * 
	 * @param arrStr
	 * @return
	 */
	public static Map<String, String> getlvlAndPhone(String arrStr) throws Exception{
		Map<String, String> map = new HashMap<String, String>();
		JSONArray array = JSONArray.parseArray(arrStr);
		for (int i = 0; i < array.size(); i++) {
			JSONObject object = array.getJSONObject(i);
			map.put(object.getString("level"), object.getString("phone"));
		}
		return map;
	}
	
public EcmMzfShareProfit buildShareProfitModel(EcmOrder ecmOrder,ShareProfitContext ctx,List<String> meiduiCompanyAgentNos) throws Exception{
		
		BigDecimal merchantRevenue=ctx.getMerchantRevenue();
		BigDecimal sellerRevenue=ctx.getSellerRevenue();
		BigDecimal personAgentRevenue=ctx.getPersonAgentRevenue();
		BigDecimal areaAgentRevenue=ctx.getAreaAgentRevenue();
		BigDecimal crossAreaAgentRevenue=ctx.getCrossAreaAgentRevenue();
		BigDecimal memberRevenue=ctx.getMemberRevenue();
		BigDecimal belongRevenue=ctx.getBelongRevenue();
		BigDecimal firstReferrerCash=ctx.getFirstReferrerCash();
		
		BigDecimal discount=ctx.getDiscount();
		Boolean isTwoHundreAgentFlag=ctx.getIsTwoHundreAgentFlag();
		Map<String,String> systemSetting=ctx.getSystemSetting();
		Map<String,String> belongMap=ctx.getBelongMap();
		String personalAgentType=ctx.getPersonalAgentType();
		
		
		EcmMzfShareProfit ecmMzfShareProfit=new EcmMzfShareProfit();
		// 支付方式为POS
		if (ShareProfitUtil.PAY_POS.equals(ecmOrder.getPaymentCode())) {
			ecmMzfShareProfit.setSellerProfit(new BigDecimal(0));
			ecmMzfShareProfit.setSource("0");
		}else{
			ecmMzfShareProfit.setSellerProfit(merchantRevenue);
			ecmMzfShareProfit.setSource("1");
		}
		ecmMzfShareProfit.setOrderSn(ecmOrder.getOrderSn());
		ecmMzfShareProfit.setOutTradeSn(ecmOrder.getOutTradeSn());
		ecmMzfShareProfit.setSellerId(ecmOrder.getSellerName());
		ecmMzfShareProfit.setSellerPhone(ecmOrder.getSellerPhone());
		ecmMzfShareProfit.setSellerPoint(sellerRevenue.setScale(0, BigDecimal.ROUND_DOWN));
		//如果不存在个代,则个代的数据为null
		if (null != ecmOrder.getAgentNoPersonal() && !"".equals(ecmOrder.getAgentNoPersonal())) {
			ecmMzfShareProfit.setPersonAgentId(ecmOrder.getAgentNoPersonal());
			ecmMzfShareProfit.setPersonAgentProfit(personAgentRevenue.setScale(2, BigDecimal.ROUND_HALF_UP));
			
		}
		//获取区代是否为美兑壹购物
		if(!StringUtils.isEmpty(ecmOrder.getAgentNoRegion())){
			ecmMzfShareProfit.setAreaAgentId(ecmOrder.getAgentNoRegion());
			ecmMzfShareProfit.setAreaAgentProfit(areaAgentRevenue.setScale(2, BigDecimal.ROUND_HALF_UP));
			//判断区代是否为美兑壹购物
			if(meiduiCompanyAgentNos.contains(ecmOrder.getAgentNoRegion())){
				//ecmMzfShareProfit.setAreaAgentId(null);
				ecmMzfShareProfit.setAreaAgentProfit(new BigDecimal(0));
			}
		}
		ecmMzfShareProfit.setPhone(ecmOrder.getBuyerName());
		ecmMzfShareProfit.setOrderFee(ecmOrder.getTotalFee());
		ecmMzfShareProfit.setPoint(memberRevenue.setScale(0, BigDecimal.ROUND_DOWN));
		//ecmMzfShareProfit.setPayTime(ecmOrder.getGmtPayment());
		//ecmMzfShareProfit.setPayTime(new Date(ecmOrder.getPayTime()*1000L));
		ecmMzfShareProfit.setSellerShareprofitRate(discount);
		ecmMzfShareProfit.setAreaShareprofitRate((isTwoHundreAgentFlag != null && isTwoHundreAgentFlag == true) ? new BigDecimal(systemSetting.get(ShareProfitUtil.TWO_AREA_SCALE)) : new BigDecimal(systemSetting.get(ShareProfitUtil.AREA_SCALE)));
		ecmMzfShareProfit.setSellerPointRate(new BigDecimal(systemSetting.get(ShareProfitUtil.SELLER_POINT_RATE)));
		if (null != belongMap && belongMap.size() > 0) {
			if(!StringUtils.isEmpty(belongMap.get("1"))){  //Alex:fix the bug:belongMap={1=} @2016 Dec 13
				ecmMzfShareProfit.setBelongOnePhone(belongMap.get("1"));
				ecmMzfShareProfit.setBelongOnePoint(belongRevenue.setScale(0, BigDecimal.ROUND_DOWN));
				ecmMzfShareProfit.setFirstReferrerCash(firstReferrerCash.setScale(2, BigDecimal.ROUND_HALF_UP));
				ecmMzfShareProfit.setFirstReferrerCashRate(new BigDecimal(systemSetting.get(ShareProfitUtil.FIRST_REFERRER_CASH_RATE)));
			}
			if (!StringUtils.isEmpty(belongMap.get("2"))) {
				ecmMzfShareProfit.setBelongTwoPhone(belongMap.get("2"));
				ecmMzfShareProfit.setBelongTwoPoint(belongRevenue.setScale(0, BigDecimal.ROUND_DOWN));
			}
		}
		if (null != crossAreaAgentRevenue && crossAreaAgentRevenue.compareTo(new BigDecimal(0)) > 0) {
			
			ecmMzfShareProfit.setOutArea("1");
			ecmMzfShareProfit.setOutareaAgentId(ecmOrder.getAgentNoRegionS());
			ecmMzfShareProfit.setOutareaAgentProfit(crossAreaAgentRevenue.setScale(2, BigDecimal.ROUND_HALF_UP));
			//判断跨区区代是否为美兑壹购物
			if(meiduiCompanyAgentNos.contains(ecmOrder.getAgentNoRegionS())){
				ecmMzfShareProfit.setOutareaAgentProfit(new BigDecimal(0));
				//ecmMzfShareProfit.setOutareaAgentId(null);
			}
			
			ecmMzfShareProfit.setOutareaShareprofitRate(new BigDecimal(systemSetting.get(ShareProfitUtil.CROSS_AREA_SCALE)));
			ecmMzfShareProfit.setPersonShareprofitRate(new BigDecimal(systemSetting.get(ShareProfitUtil.CROSS_PERSONAL_SCALE)));
		} else {
			ecmMzfShareProfit.setPersonShareprofitRate(new BigDecimal(systemSetting.get(ShareProfitUtil.PERSONAL_SCALE)));
			ecmMzfShareProfit.setOutArea("0");
			ecmMzfShareProfit.setOutareaShareprofitRate(new BigDecimal(systemSetting.get(ShareProfitUtil.CROSS_AREA_SCALE)));
		}
		
		//直营商家
		if(ShareProfitUtil.PERSONAL_AGENT_TYPE_DIRECT_SALE.equalsIgnoreCase(personalAgentType)){
			//ecmMzfShareProfit.setPersonAgentId(null);  //保留代理ID，不要设置为空，否则导致 出现bug:分润后创建账单后订单的结算状态有可能无法更新为已结算。
			ecmMzfShareProfit.setPersonAgentId(ecmOrder.getAgentNoPersonal());
			ecmMzfShareProfit.setPersonAgentProfit(BigDecimal.ZERO);
			ecmMzfShareProfit.setPersonShareprofitRate(null);
			
			//ecmMzfShareProfit.setAreaAgentId(null);
			ecmMzfShareProfit.setAreaAgentId(ecmOrder.getAgentNoRegion());
			ecmMzfShareProfit.setAreaAgentProfit(BigDecimal.ZERO);
			ecmMzfShareProfit.setAreaShareprofitRate(null);
			
			//ecmMzfShareProfit.setOutareaAgentId(null);
			ecmMzfShareProfit.setOutareaAgentId(ecmOrder.getAgentNoRegionS());
			ecmMzfShareProfit.setOutareaAgentProfit(BigDecimal.ZERO);
			ecmMzfShareProfit.setOutareaShareprofitRate(null);
		}else if(ShareProfitUtil.PERSONAL_AGENT_TYPE_BIG_REGION.equalsIgnoreCase(personalAgentType)){  //大区个代
			//ecmMzfShareProfit.setOutareaAgentId(null);
			ecmMzfShareProfit.setOutareaAgentId(ecmOrder.getAgentNoRegionS());
			ecmMzfShareProfit.setOutareaAgentProfit(BigDecimal.ZERO);
			ecmMzfShareProfit.setOutareaShareprofitRate(null);
		}
		ecmMzfShareProfit.setCreatedDate(DateUtil.getCurrentTimeSec());
		//for build EcmMzfOrderStatus purpose
		ecmMzfShareProfit.setStatus(ecmOrder.getStatus());
		ecmMzfShareProfit.setOrderDate(ecmOrder.getAddTime());
		ecmMzfShareProfit.setPayTime(ecmOrder.getPayTime());
		ecmMzfShareProfit.setRebateAmount(ecmOrder.getRebateAmount());
		ecmMzfShareProfit.setServiceFee(ecmOrder.getServiceFee());
		
		return ecmMzfShareProfit;
	}

/*	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void insertShareProfit(EcmMzfShareProfit ecmMzfShareProfit, String orderSn) throws Exception {
		
		Integer flag = baseMapper.insert(ecmMzfShareProfit, "ShareProfitMapper.shareProfit");

		if (flag <= 0) {
			log.error("OrderServiceImpl-->insertShareProfit-->ShareProfitMapper.shareProfit-->分润数据插入失败!");
			throw new Exception("OrderServiceImpl-->insertShareProfit-->ShareProfitMapper.shareProfit-->分润数据插入失败!");
		} else {
			log.info("OrderServiceImpl-->insertShareProfit-->ShareProfitMapper.shareProfit-->分润数据插入成功!");
			
			EcmMzfOrderStatus orderStatus=ShareProfitUtil.buildOrderStatusObj(ecmMzfShareProfit);
			Integer orderStatusCreated = baseMapper.insert(orderStatus, "EcmMzfOrderStatusMapper.createOrderStatus");
			
			if (orderStatusCreated <= 0) {
				log.error("OrderServiceImpl-->insertShareProfit-->EcmMzfOrderStatusMapper.createOrderStatus-->创建订单状态数据失败!");
				throw new Exception("OrderServiceImpl-->insertShareProfit-->EcmMzfOrderStatusMapper.createOrderStatus-->创建订单状态数据失败!");
			} else {
				log.info("OrderServiceImpl-->insertShareProfit-->EcmMzfOrderStatusMapper.createOrderStatus-->创建订单状态数据成功!");
			}
		}
	}*/

	@Override
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public Boolean syncVerifyStatus(EcmMzfOrderStatus input) throws Exception {
		input.setBillStatus(1);
		input.setStatusDesc("待结算");
		Integer update = baseMapper.update(input, "EcmMzfOrderStatusMapper.syncverifystatus");
		log.info("OrderServiceImpl-->syncverifystatus-->EcmMzfOrderStatusMapper.syncverifystatus-->同步订单审核状态:"+update+"条记录");
		return update>0?true:false;
	}

	
	@Override
	public List<EcmMzfShareProfit> queryShareProfit(Collection<String> orderSns) throws Exception {
		
		List<EcmMzfShareProfit> shareProfits= baseMapper.selectList(ImmutableMap.of("orderSns", orderSns), "ShareProfitMapper.getShareProfitByOrderSns");
		List<EcmMzfBillWaterVO> billWaterVOs= baseMapper.selectList(ImmutableMap.of("orderSns", orderSns), "ShareProfitMapper.getBillDateByOrderSns");

		ShareProfitUtil.updateBillInfo(shareProfits,billWaterVOs);
		return shareProfits;

	}
	

	@Override
	public ShareProfitVO queryProfitByRole(String code, Integer accountRoleType) throws Exception {
		
		ShareProfitVO spVO=null;
		Integer todayStart=DateUtil.getDayBeginBySecond(0).intValue();
		Integer todayEnd=DateUtil.getDayEndBySecond(0).intValue();
		
		final Map<String,Object> params=new HashMap<String,Object>();
		params.put("startDate",todayStart);
		params.put("endDate",todayEnd);
		
		if(accountRoleType!=null){
			if(ShareProfitConstants.ROLE_TYPE_AREA_AGENT==accountRoleType){
				params.put("areaAgentNo", code);
				List<ShareProfitVO> spVOs4Today=baseMapper.selectList(params, "ShareProfitMapper.getAreaAgentShareProfit4Period");
				//一个区代，即可能是区代，也有可能是跨区代。
				BigDecimal areaAgentProfit4Today=ShareProfitUtil.getShareProfitByType("areaAgent",spVOs4Today,"Today");
				BigDecimal outareaAgentProfit4Today=ShareProfitUtil.getShareProfitByType("outAreaAgent",spVOs4Today,"Today");
				
				BigDecimal totalAreaAgentProfit4Today=(areaAgentProfit4Today==null?BigDecimal.ZERO:areaAgentProfit4Today).add(outareaAgentProfit4Today==null?BigDecimal.ZERO:outareaAgentProfit4Today);
				
				//合并汇总today arerAgent and outAreaAgent
				List<ShareProfitVO> spVOs4Settlement=baseMapper.selectList(params, "ShareProfitMapper.getAreaAgentShareProfit4Settlement");
				BigDecimal areaAgentProfit4Settlement=ShareProfitUtil.getShareProfitByType("areaAgent",spVOs4Settlement,"Settlement");
				BigDecimal outareaAgentProfit4Settlement=ShareProfitUtil.getShareProfitByType("outAreaAgent",spVOs4Settlement,"Settlement");
				
				BigDecimal totalAreaAgentProfit4Settlement=(areaAgentProfit4Settlement==null?BigDecimal.ZERO:areaAgentProfit4Settlement).add(outareaAgentProfit4Settlement==null?BigDecimal.ZERO:outareaAgentProfit4Settlement);
				
				//合并汇总settlemt arerAgent and outAreaAgent
				spVO=new ShareProfitVO("AreaAgent",code,totalAreaAgentProfit4Today,totalAreaAgentProfit4Settlement);
				
			}else if(ShareProfitConstants.ROLE_TYPE_PERSONAL_AGENT==accountRoleType){
				params.put("personalAgentNo", code);
				ShareProfitVO spVOs4Today=baseMapper.selectOne(params, "ShareProfitMapper.getPersonalAgentShareProfit4Period");
				ShareProfitVO spVOs4Settlement=baseMapper.selectOne(params, "ShareProfitMapper.getPersonalAgentShareProfit4Settlement");
				
				BigDecimal personalAgentProfit4Today=spVOs4Today.getProfitToday();
				BigDecimal personalAgentProfit4Settlement=spVOs4Settlement.getProfit4Settlement();
				spVO=new ShareProfitVO("PersonalAgent",code,personalAgentProfit4Today,personalAgentProfit4Settlement);
				
			}
		}

		return spVO;

	}

	@Override
	public List<EcmMzfShareProfit> queryProfitByWaterByType(String waterId, Integer loginType, String code,Integer pageNumber,Integer pageSize) throws Exception {
		
		if(pageNumber!=null && pageNumber>0){
			if(pageSize==null || pageSize==0){
				pageSize=10;
			}
			PageHelper.startPage(pageNumber, pageSize);
		}

		
		List<EcmMzfShareProfit> shareProfitList=baseMapper.selectList(waterId, "EcmMzfWaterMapper.getShareProfitByWaterId");
		//loginType:1代理 2商家 3 其他
		
		if(shareProfitList!=null && !shareProfitList.isEmpty()){
			for(EcmMzfShareProfit shareProfit:shareProfitList){
				
				if(loginType!=null && loginType==1){
					if(!StringUtil.isEmpty(code) && code.length()==6){  //个代
						shareProfit.setProfit(shareProfit.getPersonAgentProfit());
						
					}else{  //区代
						shareProfit.setProfit(shareProfit.getAreaAgentProfit().add(shareProfit.getOutareaAgentProfit()));
					}
				}
			}
		}
		
		return shareProfitList;
	}

	@Override
	public int queryProfitCountByWaterId(String waterId) throws Exception {
		return baseMapper.selectOne(waterId, "EcmMzfWaterMapper.getShareProfitCountByWaterId");
	}

	@Override
	public boolean checkShareProfitExisted(String orderSn) throws Exception {
		
		int count= baseMapper.selectOne(orderSn, "ShareProfitMapper.checkShareProfitExisted");
		return count>0?true:false;
	}
	


}