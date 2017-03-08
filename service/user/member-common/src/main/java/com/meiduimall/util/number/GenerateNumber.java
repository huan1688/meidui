package com.meiduimall.util.number;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import com.meiduimall.constant.ApplicationConstant;

/**
 * 生成订单/流水号工具类
 * @author chencong
 *
 */
public class GenerateNumber implements Serializable{

	private static final long serialVersionUID = -969719718316672621L;
	
	/**
	 * 生成流水号
	 * @return
	 */
	public static String getSerialnumber() {  
	         int machineId = (int)(Math.random()*9)+1;//1-9的随机数 
	         int hashCodeV = UUID.randomUUID().toString().hashCode();  
	          if(hashCodeV < 0) {//有可能是负数  
	             hashCodeV = - hashCodeV;  
	       }  
	        // 0 代表前面补充0       
	         // 4 代表长度为4       
	         // d 代表参数为正数型  
	    return machineId+String.format("%015d", hashCodeV);  
	  }  
	  
	/**
	 * 生成订单号
	 * @return
	 */
	public static String generateOrderId() {  
        SimpleDateFormat simpleDateFormat;  
  	  
        simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmSSSS");  
  
        Date date = new Date();  
  
        String str = simpleDateFormat.format(date);  
  
        Random random = new Random();  
  
        int rannum = (int) (random.nextDouble() * (9999 - 1000 + 1)) + 1000;// 获取4位随机数  
  
        return  str + rannum;// 当前时间  
	    } 
	
	  /**
	   * 生成招行一网通支付需要的订单号
	   * @return
	   */
	public static String generateCmbOrderId() {  
		  
		  SimpleDateFormat simpleDateFormat;  
		  
		  simpleDateFormat = new SimpleDateFormat("yyMMSSS");  
		  
		  Date date = new Date();  
		  
		  String str = simpleDateFormat.format(date);  
		  
		  Random random = new Random();  
		  
		  int rannum = (int) (random.nextDouble() * (999 - 100 + 1)) + 100;// 获取4位随机数  
		  
		  return  str + rannum;// 当前时间  
	  } 
	  
	  
	/**
	 * 根据业务类型获取业务订单号 <br>
	 * @param tradeType
	 * @return
	 */
	public static String generateBusinessNo(String tradeType){
		  /** 未待完续 */
		  String key = tradeType.toUpperCase();
		  /** 业务类型+年月日+6位自增 */
		  String bsNo = (key + new SimpleDateFormat("yyyyMMdd").format(new Date()));
		  Random random = new Random();
		  int rannum = (int) (random.nextDouble() * 100000);// 获取6位随机数  
		  switch (key) {
			case ApplicationConstant.MONEY_TRADE_TYPE_YETX:
				bsNo = bsNo + rannum;
				break;
	
			default:
				bsNo = generateOrderId();
				break;
			}
		  return bsNo;
	  }
}