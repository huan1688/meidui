package com.meiduimall.application.mall.catalog.service;

public interface GoodsDetailService {

	/**
	 * 请求微服务，根据商品itemId获取商品详情
	 * 
	 * @param itemId
	 *            商品ID
	 * @param memId
	 *            会员系统ID
	 * @return
	 */
	public String getItemDetailHttp(int itemId, String memId);
}
