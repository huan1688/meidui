package com.meiduimall.service.catalog.controller.test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.meiduimall.service.catalog.test.BaseTest;

public class ShopControllerTest extends BaseTest {
	
	@Test
	public void testGetShopDetail1() throws Exception {
		ResultActions results = mockMvc.perform(MockMvcRequestBuilders
				.post("/mall/catalog-service/v1/shopInfo/getShopDetail")
				.param("shop_id", "600")
				.param("mem_id", "92331632-8ce5-4865-ba09-83c362ef6b85"))
				.andExpect(status().isOk());
		
		results.andDo(new ResultHandler() {
			@Override
			public void handle(MvcResult result) throws Exception {
				System.out.println("*********" + result.getResponse().getContentAsString());
			}
		});
	}
	
	@Test
	public void testGetShopDetail2() throws Exception {
		ResultActions results = mockMvc.perform(MockMvcRequestBuilders
				.post("/mall/catalog-service/v1/shopInfo/getShopDetail")
				.param("shop_id", "600"))
				.andExpect(status().isOk());
		
		results.andDo(new ResultHandler() {
			@Override
			public void handle(MvcResult result) throws Exception {
				System.out.println("*********" + result.getResponse().getContentAsString());
			}
		});
	}
	
	@Test
	public void testGetShopDetail3() throws Exception {
		ResultActions results = mockMvc.perform(MockMvcRequestBuilders
				.post("/mall/catalog-service/v1/shopInfo/getShopDetail")
				.param("shop_id", ""))
				.andExpect(status().isOk());
		
		results.andDo(new ResultHandler() {
			@Override
			public void handle(MvcResult result) throws Exception {
				System.out.println("*********" + result.getResponse().getContentAsString());
			}
		});
	}
	
	@Test
	public void testCollectOrCancelShop1() throws Exception {
		
	}
}