package com.meiduimall.service.settlement.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import com.github.pagehelper.StringUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.meiduimall.core.Constants;
import com.meiduimall.core.util.ToolUtils;
import com.meiduimall.service.settlement.context.MemberSystemDataContext;
import com.meiduimall.service.settlement.model.EcmMzfOrderStatus;
import com.meiduimall.service.settlement.model.EcmMzfShareProfit;
import com.meiduimall.service.settlement.model.EcmSystemSetting;
import com.meiduimall.service.settlement.util.DateUtil;
import com.meiduimall.service.settlement.vo.EcmMzfBillWaterVO;
import com.meiduimall.service.settlement.vo.ShareProfitVO;


/**
 * @author 许彦雄
 *
 */
public class ShareProfitUtil {
	
	//大区个代提成比例
	public final static String PERSONAL_SCALE_FOR_BIG_REGION = "person_shareprofit_rate_for_big_region";
	
	//个代提成比例
	public final static String PERSONAL_SCALE = "person_shareprofit_rate";
	
	//区代提成比例
	public final static String AREA_SCALE = "area_shareprofit_rate";
	
	//跨区个代提成比例
	public final static String CROSS_PERSONAL_SCALE = "outperson_shareprofit_rate";
	
	//跨区区代提成比例
	public final static String CROSS_AREA_SCALE = "outarea_shareprofit_rate";
	
	//前200区代提成
	public final static String TWO_AREA_SCALE = "two_hundred_area_shareprofit_rate";
	
	//推荐人积分提成
	public final static String BELONG_SCALE = "belong_rate";
	
	//一级推荐人所获现金比例
	public final static String FIRST_REFERRER_CASH_RATE = "first_referrer_cash_rate";
	
	//商家所获积分提成
	public final static String SELLER_POINT_RATE = "seller_point_rate";
	
	//接收短信的手机号码
	public final static String SMS_PHONES = "sms.phones";
	public final static String CLIENT_ID = "clientID";

	public final static String TEMPLATE_ID_O2O_1009="O2O_1009";
	public final static String TEMPLATE_ID_O2O_1008 = "O2O_1008";
	
	//请求方式_get
	public final static String REQUEST_METHOD_GET = "GET";
	
	//请求方式_post
	public final static String REQUEST_METHOD_POST = "POST";
	
	//加载鉴权配置文件
	//public final static Map<String, String> AUTHORIZED_MAP = ToolUtils.loadProperty("config/authorized.properties");
	public static Map<String, String> AUTHORIZED_MAP =null;

	
	//直营前缀编码
	public static final String CODE_DIRECT_SALE="888888";
	
	//大区前缀编码
	public static final String CODE_BIG_REGION="999999";
	
	//直营个代(888888开头)
	public static final String PERSONAL_AGENT_TYPE_DIRECT_SALE="directSale";
	
	//大区个代(999999开头)
	public static final String PERSONAL_AGENT_TYPE_BIG_REGION="bigRegion";
	
	//(普通个代类型)
	public static final String PERSONAL_AGENT_TYPE_NORMAL="normal";
	
	public static final Map<Integer,String> O2O_SETTLEMENT_STATUS_CODE_MAP=ImmutableMap.of(ShareProfitConstants.O2O_SETTLEMENT_STATUS_CODE_SCORE,"积分已送出",
			ShareProfitConstants.O2O_SETTLEMENT_STATUS_CODE_BILL,"订单已结算",ShareProfitConstants.O2O_SETTLEMENT_STATUS_CODE_CASH,"一级推荐人现金奖励已送出");

	//接口参数url拼接
	public static String getBelongInfoUrl(String phone) {
		String timeStamp = String.valueOf(System.currentTimeMillis() / 1000L);
		String nonce = ToolUtils.getRandomNum();
		String belongInfoUrl = AUTHORIZED_MAP.get("authorized.url") 
				+ AUTHORIZED_MAP.get("authorized.belong")
				+ "?oauth_signature_method=" + AUTHORIZED_MAP.get("authorized.oauth_signature_method")
				+ "&oauth_accessor_secret=" + AUTHORIZED_MAP.get("authorized.oauth_accessor_secret") 
				+ "&oauth_consumer_key=" + AUTHORIZED_MAP.get("authorized.oauth_consumer_key")
				+ "&oauth_timestamp=" + timeStamp 
				+ "&oauth_nonce=" + nonce 
				+ "&oauth_version=" + AUTHORIZED_MAP.get("authorized.oauth_version")
				+ "&oauth_signature=" + oauthSignature(phone, timeStamp, nonce) 
				+ "&share_man=" + phone;
		return belongInfoUrl;
	}
	
	//MD5加密串拼接
	public static String oauthSignature(String phone, String timeStamp, String nonce) {
		String md5Str = "get&" + AUTHORIZED_MAP.get("authorized.url") 
				+ AUTHORIZED_MAP.get("authorized.belong")
				+ "&oauth_signature_method=" + AUTHORIZED_MAP.get("authorized.oauth_signature_method") 
				+ "&oauth_accessor_secret=" + AUTHORIZED_MAP.get("authorized.oauth_accessor_secret")
				+ "&oauth_consumer_key=" + AUTHORIZED_MAP.get("authorized.oauth_consumer_key") 
				+ "&oauth_timestamp=" + timeStamp 
				+ "&oauth_nonce=" + nonce 
				+ "&oauth_version=" + AUTHORIZED_MAP.get("authorized.oauth_version") 
				+ "&share_man=" + phone;
		return ToolUtils.MD5Encrypt(ToolUtils.encode(md5Str, "UTF-8"));
	}
	
	
	
	/** 支付方式 */
	public final static String PAY_WECHAT = "pay_wechat";
	public final static String PAY_ALIPAY = "pay_alipay";
	public final static String PAY_CMB = "pay_cmb";
	public final static String PAY_POS = "pay_pos";
	
	/**
	 * Description : 查询分润数据配置
	 * Created By : Fkx 
	 * Creation Time : 2016-10-19 上午10:58:37 
	 * 
	 * @return
	 */
	public static Map<String, String> queryShareProfit(List<EcmSystemSetting> systemSettings) {
		Map<String, String> ShareProfit = new HashMap<String, String>();
		try {
			for (EcmSystemSetting systemSetting : systemSettings) {
				ShareProfit.put(systemSetting.getScode(), systemSetting.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ShareProfit;
	}
	
	
	//接口参数url拼接
	public static String belongInfoUrl(Map<String, String> map) {
			String timeStamp = String.valueOf(System.currentTimeMillis() / 1000L);
			String nonce = ToolUtils.getRandomNum();
			map.put("timeStamp", timeStamp);
			map.put("nonce", nonce);
			
			String url =  map.get("url")!=null?map.get("url"):"";
			String user_id =  map.get("user_id")!=null?map.get("user_id"):"";
			String consume_points_count =  map.get("consume_points_count")!=null?map.get("consume_points_count"):"";
			String order_source =  map.get("order_source")!=null?map.get("order_source"):"";
			String order_id =  map.get("order_id")!=null?map.get("order_id"):"";
			
			String belongInfoUrl = AUTHORIZED_MAP.get("authorized.url")+url;
			 
			String belongInfo = "oauth_signature_method=" + AUTHORIZED_MAP.get("authorized.oauth_signature_method")
					+ "&oauth_accessor_secret=" + AUTHORIZED_MAP.get("authorized.oauth_accessor_secret") 
					+ "&oauth_consumer_key=" + AUTHORIZED_MAP.get("authorized.oauth_consumer_key")
					+ "&oauth_timestamp=" + timeStamp 
					+ "&oauth_nonce=" + nonce 
					+ "&oauth_version=" + AUTHORIZED_MAP.get("authorized.oauth_version");
					
			
			String belongInfoend = "&user_id=" + user_id
					+ "&consume_points_count=" + consume_points_count
					+ "&order_source=" + order_source
			        + "&order_id=" + order_id;
					
			String oauth_signature  = "&oauth_signature=" + ToolUtils.MD5Encrypt(ToolUtils.encode("get&"+belongInfoUrl+"&"+belongInfo+belongInfoend, "UTF-8")); 
			
			return belongInfoUrl+"?"+belongInfo+oauth_signature+belongInfoend;
	}
	
	public static String getPersonalAgentType(String personalAgentNo){
		if(StringUtils.isEmpty(personalAgentNo)){
			return PERSONAL_AGENT_TYPE_NORMAL;
		}else{
			String prefix6=personalAgentNo.trim().substring(0, 6);
			if(CODE_DIRECT_SALE.equals(prefix6)){
				return PERSONAL_AGENT_TYPE_DIRECT_SALE;
			}else if(CODE_BIG_REGION.equals(prefix6)){
				return PERSONAL_AGENT_TYPE_BIG_REGION;
			}else{
				return PERSONAL_AGENT_TYPE_NORMAL;
			}
		}
		
	}

		//构建更新一级推荐人所获现金余额到会员系统接口的URL以及请求参数
		public static String buildMemberSystemAmoutUrl(MemberSystemDataContext ctx) {
			
			String userId = ctx.getUserId();

			String signatureMethod=AUTHORIZED_MAP.get(MemberSystemDataContext.KEY_AUTHORIZED_OAUTH_SIGNATURE_METHOD);
			String accessorSecret=AUTHORIZED_MAP.get(MemberSystemDataContext.KEY_AUTHORIZED_OAUTH_ACCESS_SECRET);
			String consumerKey=AUTHORIZED_MAP.get(MemberSystemDataContext.KEY_AUTHORIZED_OAUTH_CONSUMER_KEY);
			String apiDomain=AUTHORIZED_MAP.get(MemberSystemDataContext.KEY_AUTHORIZED_DOMAIN);
			String apiPath=AUTHORIZED_MAP.get(MemberSystemDataContext.KEY_AUTHORIZED_API_UPD_FIRST_REFERRER_CASH);
			String version=AUTHORIZED_MAP.get(MemberSystemDataContext.KEY_AUTHORIZED_OAUTH_VERSION);
			
			String timeStamp = String.valueOf(System.currentTimeMillis() / 1000L);
			String nonce = ToolUtils.getRandomNum();
			String source=ctx.getSource();
			String orderId=ctx.getOrderSn();
			String tradeType=ctx.getTradeType();
			String tradeAmount=ctx.getAmount().toString();
			String direction=ctx.getDirection();
			String tradeTime=String.valueOf(ctx.getTradeTime());
			String remark=ctx.getRemark();
			String remarkEncoded=ToolUtils.encodeStr(remark,Constants.ENCODE_UTF8);
				
			String api = apiDomain+apiPath;
			 
			String oauthParams = "oauth_signature_method=" + signatureMethod
					+ "&oauth_accessor_secret=" + accessorSecret 
					+ "&oauth_consumer_key=" + consumerKey
					+ "&oauth_timestamp=" + timeStamp 
					+ "&oauth_nonce=" + nonce 
					+ "&oauth_version=" + version;
			
			String dataParams = "&user_id=" + userId
					+ "&source=" + source
					+ "&trade_type=" + tradeType
			        + "&order_id=" + orderId 
			        + "&direction=" + direction 
			        + "&trade_amount=" + tradeAmount 
			        + "&trade_time=" + tradeTime
			        + "&remark=" + remarkEncoded;
			
			String dataParams4Sign = "&user_id=" + userId
					+ "&source=" + source
					+ "&trade_type=" + tradeType
			        + "&order_id=" + orderId 
			        + "&direction=" + direction 
			        + "&trade_amount=" + tradeAmount 
			        + "&trade_time=" + tradeTime
			        + "&remark=" + remark;
					
			String signature = "&oauth_signature=" + ToolUtils.MD5Encrypt(ToolUtils.encodeStr("get&"+api+"&"+oauthParams+dataParams4Sign, Constants.ENCODE_UTF8)); 
			
			return api+"?"+oauthParams+signature+dataParams;  //不嫩直接将中文字符的remark字段传到请求参数，不然API服务器那边接收到时会出现乱码。
		}

		public static EcmMzfOrderStatus buildOrderStatusObj(EcmMzfShareProfit ecmMzfShareProfit) {
			EcmMzfOrderStatus orderStatus=new EcmMzfOrderStatus();
			orderStatus.setOrderSn(ecmMzfShareProfit.getOrderSn());
			orderStatus.setStatus(ecmMzfShareProfit.getStatus());
			orderStatus.setAddTime(ecmMzfShareProfit.getOrderDate());
			orderStatus.setPayTime(ecmMzfShareProfit.getPayTime());
			orderStatus.setCreatedDate(DateUtil.getCurrentTimeSec());
			orderStatus.setShareStatus(1);  //1:已分润
			return orderStatus;
		}

		public static BigDecimal getShareProfitByType(String roleType, List<ShareProfitVO> shareProfitVOs,String profitType) {
			BigDecimal value=null;
			if(shareProfitVOs!=null && !shareProfitVOs.isEmpty() && !StringUtil.isEmpty(roleType)){
				Collection<ShareProfitVO> col=Collections2.filter(shareProfitVOs, new Predicate<ShareProfitVO>(){
					
					@Override
					public boolean apply(ShareProfitVO soVo) {
						return roleType.equals(soVo.getType());
					}});
				
				if(col!=null){
					final List<ShareProfitVO> list=new ArrayList(col);
					ShareProfitVO spVO=list.get(0);
					if(spVO!=null){
						if("Today".equals(profitType)){
							return spVO.getProfitToday();
						}
						
						if("Settlement".equals(profitType)){
							return spVO.getProfit4Settlement();
						}
					}
				}

			}
			return value;
			
			
		}


		public static void updateBillInfo(List<EcmMzfShareProfit> shareProfits, List<EcmMzfBillWaterVO> billWaterVOs) {
			if(shareProfits!=null && !shareProfits.isEmpty() && billWaterVOs!=null && !billWaterVOs.isEmpty()){
				Map<String, EcmMzfShareProfit> shareProfitMap=SettlementUtil.convert2Map(shareProfits, "orderSn");
				Map<String, EcmMzfBillWaterVO> billWaterMap=SettlementUtil.convert2Map(billWaterVOs, "orderSn");
				for(Map.Entry<String, EcmMzfShareProfit> entry:shareProfitMap.entrySet()){
					String orderSn=entry.getKey();
					if(billWaterMap.get(orderSn)!=null){
						EcmMzfBillWaterVO bwVO=billWaterMap.get(orderSn);
						entry.getValue().setBillDate(bwVO.getBillDate());
						entry.getValue().setBillAddDate(bwVO.getBillAddDate());
						entry.getValue().setBillDateStr(DateUtil.format(bwVO.getBillDate(), DateUtil.YYYY_MM_DD));
						entry.getValue().setBillAddDateStr(DateUtil.format(bwVO.getBillAddDate(), DateUtil.YYYY_MM_DD));
						
					}
				}
				
			}
		}

	 
}