package com.meiduimall.application.md1gwaccess.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import com.meiduimall.application.md1gwaccess.constant.OauthConst;
import com.meiduimall.application.md1gwaccess.constant.SysParaNameConst;
import com.meiduimall.application.md1gwaccess.model.EctoolsPayments;
import com.meiduimall.application.md1gwaccess.model.EctoolsPaymentsSucc;
import com.meiduimall.application.md1gwaccess.model.PaymentTrade;
import com.meiduimall.application.md1gwaccess.model.PaymentTradePay;
import com.meiduimall.application.md1gwaccess.model.SystradePTrade;
import com.meiduimall.application.md1gwaccess.model.SysuserAccount;
import com.meiduimall.application.md1gwaccess.model.SysuserUser;
import com.meiduimall.application.md1gwaccess.model.SysuserWalletPaylog;

import net.sf.json.JSONObject;

public class CommonUtil {

	
	/**
	 * 组装签名 公共的key value
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> CommonMap(Map<String,String> map) throws Exception{
		map.put(OauthConst.CLIENT_ID, OauthConst.CLIENT_ID_VALUE);
		map.put(OauthConst.TIMESATAMP, String.valueOf(System.currentTimeMillis()));
		map.put(OauthConst.SIGN, GatewaySignUtil.sign(OauthConst.SECRETKEY_VALUE, map));
		return map;
		
	}
	/**
	 * 组装签名 公共的JSON
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static JSONObject CommonJSON(JSONObject json) throws Exception{
		json.put(OauthConst.CLIENT_ID, OauthConst.CLIENT_ID_VALUE);
		json.put(OauthConst.TIMESATAMP, String.valueOf(System.currentTimeMillis()));
		json.put(OauthConst.SIGN, GatewaySignUtil.buildsign(OauthConst.SECRETKEY_VALUE, json));
		return json;
		
	}
	
	/**
	 * 组装更新 EctoolsPayments
	 * @param paymentTradePay
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public static EctoolsPayments UpdateEctoolsPaymentsCommon(PaymentTrade paymentTrade) throws Exception{
		EctoolsPayments ectoolsPayments = new EctoolsPayments();
		ectoolsPayments.setCurMoney(new BigDecimal(paymentTrade.getMoney()));     //支付货币金额 (第三方支付金额,必须money)
		ectoolsPayments.setWalletPay(new BigDecimal(paymentTrade.getUse_money()));   //钱包余额支付金额
		ectoolsPayments.setShopingPay(new BigDecimal(0));     //购物券支付金额 shoping_pay 忽略
		ectoolsPayments.setCoupPay(new BigDecimal(0));     //消费券支付金额 coup_pay 忽略
		ectoolsPayments.setPointPay(Integer.valueOf(paymentTrade.getUse_point())); //积分支付金额
		ectoolsPayments.setCartXfc(new BigDecimal(0));     //忽略
		ectoolsPayments.setStatus(SysParaNameConst.paying);  
		ectoolsPayments.setPaymentId(paymentTrade.getPayment_id()); //支付单号
		Operate operate = new Operate();
		operate.serializable(paymentTrade);
		ectoolsPayments.setReturnUrl(SystemConfig.getInstance().configMap.get("Finish_URL")+"?paymentTrade="+paymentTrade);    //支付返回地址
		return ectoolsPayments;
	}

	/**
	 * 组装更新到平台订单表 systradePTrade 
	 * @param paymentBill
	 * @return
	 * @throws Exception
	 */
	public static SystradePTrade UpdateCSPCommon(Map<String, Object> paymentBill) throws Exception{
	   SystradePTrade systradePTrade = new SystradePTrade();
	   systradePTrade.setCoupPay(new BigDecimal(paymentBill.get("coupPay").toString()));
	   systradePTrade.setShopingPay(new BigDecimal(paymentBill.get("shopingPay").toString()));
	   systradePTrade.setPointPay(Integer.valueOf(paymentBill.get("pointPay").toString()));
	   systradePTrade.setPlatformId(new BigInteger(paymentBill.get("platformId").toString()));
	   return systradePTrade;
	}

	/**
	 * 组装 【会员钱包支付接口】,写入钱包支付记录 SysuserWalletPaylog 
	 * @param paymentBill
	 * @param userMoney
	 * @param paymentTradePay
	 * @param sysuserAccount
	 * @return
	 * @throws Exception
	 */
	public static SysuserWalletPaylog UpdateUsersWalletPayCommon(Map<String, Object> paymentBill,
			SysuserUser userMoney,PaymentTradePay paymentTradePay,SysuserAccount sysuserAccount) throws Exception{
		SysuserWalletPaylog sysuserWalletPaylog = new SysuserWalletPaylog();
		sysuserWalletPaylog.setUid(Integer.valueOf(paymentTradePay.getUser_id()));
		sysuserWalletPaylog.setLoginName(sysuserAccount.getLoginAccount());
		sysuserWalletPaylog.setOrderNo(paymentBill.get("platformId").toString());
		sysuserWalletPaylog.setVorMoney(userMoney.getMoney());
		sysuserWalletPaylog.setMoney(new BigDecimal(paymentBill.get("walletPay").toString()));
		double AfMoney = userMoney.getMoney().subtract(new BigDecimal(paymentBill.get("walletPay").toString())).doubleValue();
		sysuserWalletPaylog.setAfMoney(new BigDecimal(AfMoney));
		return sysuserWalletPaylog;
	}

	/**
	 * 组装 查询支付单信息 EctoolsPayments 
	 * @param result_payment
	 * @param ectoolsPaymentsSucc
	 * @return
	 */
	public static EctoolsPayments getIsPaySuccCommon(Map<String, Object> result_payment,
			EctoolsPaymentsSucc ectoolsPaymentsSucc) throws Exception{
		EctoolsPayments ectoolsPayments = new EctoolsPayments();
		ectoolsPayments.setPlatformId(result_payment.get("platformId").toString());
		ectoolsPayments.setPaymentId(ectoolsPaymentsSucc.getPaymentId());
		ectoolsPayments.setStatus(SysParaNameConst.SUCC);
		return ectoolsPayments;
	}

	/**
	 * //1.获取用户余额和已冻结金额  //2.更新余额、更新冻结金额
	 * @param userMoney
	 * @param paymentTradePay
	 * @return
	 */
	public static SysuserUser UpdateMF(SysuserUser userMoney, PaymentTrade paymentTrade) throws Exception{
		SysuserUser sysuserUser = new SysuserUser();
		sysuserUser.setMoney(userMoney.getMoney());
		sysuserUser.setFrozenMoney(userMoney.getFrozenMoney());
		sysuserUser.setUserId(Integer.valueOf(paymentTrade.getUser_id()));
		return sysuserUser;
	}
	
}