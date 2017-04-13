package com.meiduimall.service.settlement.service;

import com.meiduimall.service.settlement.model.SmsReqDTO;

public interface SmsService {
	
	/**
	 * 发送短信 采用RestTemplate请求http
	 * @param smsReqDTO
	 * @return
	 * @
	 */
	public boolean sendMsm(SmsReqDTO smsReqDTO);
	
	/**
	 * 发送短信 采用httpClient请求http
	 * @return
	 * @
	 */
	public boolean sendMessage(SmsReqDTO smsReqDTO);

}
