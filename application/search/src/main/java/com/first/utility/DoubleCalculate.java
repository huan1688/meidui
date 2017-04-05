package com.first.utility;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class DoubleCalculate {
	 /**
     * 提供精确加法计算的add方法
     * 
     * @param value1 被加数
     * @param value2 加数
     * @return 两个参数的和
     */
    public static double add(double value1, double value2) {
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确减法运算的sub方法
     * 
     * @param value1 被减数
     * @param value2 减数
     * @return 两个参数的差
     */
    public static double sub(double value1, double value2) {
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确乘法运算的mul方法
     * 
     * @param value1 被乘数
     * @param value2 乘数
     * @return 两个参数的积
     */
    public static double mul(double value1, double value2) {
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.multiply(b2).doubleValue();
    }
    
    /**
     * 提供精确乘法运算的mul方法
     * 
     * @param value1 被乘数
     * @param value2 乘数
     * @param scale 精确范围
     * @return 两个参数的积
     */
    public static double mul(double value1, double value2, int scale) {
    	BigDecimal b1 = BigDecimal.valueOf(value1);
    	BigDecimal b2 = BigDecimal.valueOf(value2);
    	return b1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 提供精确的除法运算方法div
     * 
     * @param value1 被除数
     * @param value2 除数
     * @param scale 精确范围 =精确度不能小于0
     * @return 两个参数的商
     * @throws IllegalAccessException
     */
    public static double div(double value1, double value2, int scale){
    	 if (scale < 0) {
    		 scale = 8;// throw new IllegalAccessException("精确度不能小于0");
          }
    	MathContext mc = new MathContext(scale, RoundingMode.HALF_DOWN);
        // 如果精确范围小于0，抛出异常信息
       
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.divide(b2, mc).doubleValue();
    }
    
	//传入一个数字，8位小数点
	public static String getFormalValue(String str){
		if(str == null || str == "" ){
			return "0.00000000";
		}else{
			if(str.indexOf(",") != -1){
				str = str.replace(",","");
				Double d = new Double(str); 
				DecimalFormat decimalFormat = new DecimalFormat("0.00000000");
				return decimalFormat.format(d).toString();
			}else{
				Double d = new Double(str); 
				DecimalFormat decimalFormat = new DecimalFormat("0.00000000");
				return decimalFormat.format(d).toString();
			}
		}
	}
	
	//传入一个数字，2位小数点
	public static String getFormalValueTwo(String str){
		if(str == null || str == "" ){
			return "0.00";
		}else{
			if(str.indexOf(",") != -1){
				str = str.replace(",","");
				Double d = new Double(str); 
				DecimalFormat decimalFormat = new DecimalFormat("0.00");
				return decimalFormat.format(d).toString();
			}else{
				Double d = new Double(str); 
				DecimalFormat decimalFormat = new DecimalFormat("0.00");
				return decimalFormat.format(d).toString();
			}
		}
	}
}
