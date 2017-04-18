package com.meiduimall.password;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.meiduimall.password.util.MD5;

public class GatewaySignUtil {
	
	
	/**
	 * 功能描述:  产生签名
	 * Author: 陈建宇
	 * Date:   2016年12月19日 上午10:28:34
	 * @param appKey
	 * @param param
	 * @return String   
	 */
	public static String  sign(String appKey,Map<String,String> param) {
		Map<String, String> map = new TreeMap<String, String>();
		map.putAll(param);
		Set<String> keySet = map.keySet();
		StringBuilder buffer = new StringBuilder();
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            if (key.equals("sign")){
            	continue;
            }
            String value = map.get(key);
            if ( value == null || value.length() < 1 ) {
            	continue;
            }
            buffer.append(key);
            buffer.append("=");
            buffer.append(value);
            buffer.append("&");
        }
        buffer.append("key=");
        buffer.append(appKey);
        return MD5.MD5Encode(buffer.toString()).toUpperCase();
	}
	
	
	
	

}
