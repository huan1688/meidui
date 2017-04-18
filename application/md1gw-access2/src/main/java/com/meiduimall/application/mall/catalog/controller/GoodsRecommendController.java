package com.meiduimall.application.mall.catalog.controller;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.meiduimall.application.mall.catalog.constant.ApplicationMallApiCode;
import com.meiduimall.application.mall.catalog.service.impl.GoodsRecommendServiceImpl;
import com.meiduimall.exception.ApiException;

/**
 * 商品推荐相关类
 * 
 * @author yangchangfu
 */
@RestController
@RequestMapping("/md1gwmall/md1gw_access/v1/goodsRecommend")
public class GoodsRecommendController {

	private static Logger logger = LoggerFactory.getLogger(GoodsRecommendController.class);

	@Autowired
	private HttpServletResponse response;

	@Autowired
	private GoodsRecommendServiceImpl goodsRecommendService;

	/**
	 * 根据推荐类型，获取推荐商品
	 * 
	 * @param type
	 *            推荐类型
	 * @param sourceId
	 *            请求来源
	 * @return
	 */
	@RequestMapping("/getRecommend")
	public String getFirstRecommendGoods(String type,
			@RequestParam(value = "sourceId", required = false, defaultValue = "1") String sourceId) {

		int reco_type = 0;
		int reco_source_id = 0;
		try {
			reco_type = Integer.parseInt(type);
			reco_source_id = Integer.parseInt(sourceId);
		} catch (NumberFormatException e) {
			logger.error("根据推荐类型，获取推荐商品，服务器异常：" + e);
			throw new ApiException(ApplicationMallApiCode.REQUEST_PARAMS_ERROR);
		}
		
		String result = goodsRecommendService.getFirstRecommendGoodsHttp(reco_type, reco_source_id);
		// 增加头部--解决JS跨域问题
		response.setHeader("Access-Control-Allow-Origin", "*");
		return result;
	}
}
