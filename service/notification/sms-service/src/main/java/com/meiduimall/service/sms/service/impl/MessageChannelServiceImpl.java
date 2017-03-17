package com.meiduimall.service.sms.service.impl;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.meiduimall.core.Constants;
import com.meiduimall.core.util.JacksonUtil;
import com.meiduimall.redis.util.JedisUtil;
import com.meiduimall.service.sms.entity.MessageChannel;
import com.meiduimall.service.sms.entity.TemplateInfo;
import com.meiduimall.service.sms.mapper.MessageChannelMapper;
import com.meiduimall.service.sms.mapper.TemplateInfoMapper;
import com.meiduimall.service.sms.service.MessageChannelService;


@Service
public class MessageChannelServiceImpl implements MessageChannelService{
	
	private static Logger Logger = LoggerFactory.getLogger(MessageChannelServiceImpl.class);

	@Autowired
	private MessageChannelMapper messageChannelMapper;
	
	@Autowired
	private TemplateInfoMapper templateInfoMapper;
	


	

	public String getChannelList(String key){
		
		String channelListJsonStr =JedisUtil.getJedisInstance().execGetFromCache(key);
		if(Strings.isNullOrEmpty(channelListJsonStr)){
			try{
				List<MessageChannel> channelList = messageChannelMapper.getChannelList();
				if(null != channelList && channelList.size() > 0){
					channelListJsonStr =JacksonUtil.listToJson(channelList);
					JedisUtil.getJedisInstance().execSetexToCache(key, Constants.REDIS_NINETY, channelListJsonStr);
				}
				
			} catch (Exception e) {
				Logger.error("获取渠道异常:{}", e.toString());
			}
		}
		return channelListJsonStr;
		
	}
	
	
	
	public String getTemplateList(String key){
		String templateListJsonStr = JedisUtil.getJedisInstance().execGetFromCache(key);
		if(Strings.isNullOrEmpty(templateListJsonStr)){
			try {
				List<TemplateInfo> templateInfo = templateInfoMapper.getTemplateInfoList();
				if(null != templateInfo && templateInfo.size() > 0){
					templateListJsonStr =JacksonUtil.listToJson(templateInfo);
					JedisUtil.getJedisInstance().execSetexToCache(key, Constants.REDIS_TENMINUTE, templateListJsonStr);
				}
				
			} catch (Exception e) {
				Logger.error("获取模板异常:{}", e.toString());
			}
		}
		return templateListJsonStr;
		
	}

}