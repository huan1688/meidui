/*package com.meiduimall.application.usercenter.api;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.meiduimall.core.util.JsonUtils;


*//**
 * 会员基本操作
 * @author chencong
 *
 *//*
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BaseOpV1ControllerTest extends BaseControllerTest {
	
	private final static Logger logger=LoggerFactory.getLogger(BaseOpV1ControllerTest.class);
	 

    
    *//**会员登出 
	 * @throws Exception *//*
    @Test
    public void exit () throws Exception{
    	Map<String, Object> mapCondition=new HashMap<>();
    	mapCondition.put("token",token);
    	ResultActions resultActions=mockMvc.perform(MockMvcRequestBuilders.post(baseUrl+"/exit")
    			.contentType(MediaType.APPLICATION_JSON_UTF8)
    			.content(JsonUtils.beanToJson(mapCondition)))
    			.andExpect(status().isOk())
    			.andExpect(jsonPath("$.status",is(0)));
    	
    	resultActions.andDo(new ResultHandler() {
			@Override
			public void handle(MvcResult result) throws Exception {
				logger.info("单元测试>>会员登陆退出API>>执行结果:{}",result.getResponse().getContentAsString());;
			}
		});
    }
	      
}*/