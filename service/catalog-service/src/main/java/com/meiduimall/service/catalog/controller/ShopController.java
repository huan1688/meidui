package com.meiduimall.service.catalog.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.meiduimall.core.BaseApiCode;
import com.meiduimall.core.ResBodyData;
import com.meiduimall.service.catalog.annotation.HasToken;
import com.meiduimall.service.catalog.entity.SysuserAccount;
import com.meiduimall.service.catalog.service.ShopService;

@RestController
@RequestMapping("/mall/catalog-service/v1/shopInfo")
public class ShopController {

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(ShopController.class);

	@Autowired
	private ShopService shopService;

	@Autowired
	private HttpServletRequest request;

	/**
	 * 获取店铺详情
	 * 
	 * @param shop_id
	 * @param token
	 * @return
	 */
	@RequestMapping(value = "/getShopDetail")
	public ResBodyData getShopDetail(String shop_id, String token) {
		try {
			logger.info("根据店铺ID，查询店铺详细信息，店铺ID：" + shop_id);

			int shopId = 0;
			try {
				shopId = Integer.parseInt(shop_id);
			} catch (Exception e) {
				logger.error("根据店铺ID，查询店铺详细信息，店铺ID错误：" + e);
				ResBodyData result = new ResBodyData();
				result.setStatus(BaseApiCode.REQUEST_PARAMS_ERROR);
				result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.REQUEST_PARAMS_ERROR));
				result.setData(new JSONObject());
				return result;
			}
			return shopService.getShopDetail(shopId, token);
		} catch (Exception e) {
			logger.error("根据店铺ID，查询店铺详细信息，服务器异常：" + e);
			ResBodyData result = new ResBodyData();
			result.setStatus(BaseApiCode.OPERAT_FAIL);
			result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.OPERAT_FAIL));
			result.setData(new JSONObject());
			return result;
		}
	}

	/**
	 * 收藏或者取消收藏店铺
	 * 
	 * @HasToken 验证token是否有效，并通过request传递SysuserAccount对象
	 * 
	 * @param shop_id
	 * @param is_collect
	 *            1代表收藏，0代表取消收藏
	 * @return
	 */
	@HasToken
	@RequestMapping(value = "/collectShop")
	public ResBodyData cancelOrCollectShop(String shop_id, String is_collect) {
		try {
			if ("1".equals(is_collect)) {
				logger.info("收藏店铺：" + shop_id);
			} else {
				logger.info("取消shouc收藏店铺：" + shop_id);
			}
			int shopId = 0;
			int isCollect = 0;
			try {
				shopId = Integer.parseInt(shop_id);
				isCollect = Integer.parseInt(is_collect);
			} catch (Exception e) {
				logger.error("收藏店铺，服务器异常：" + e);
				ResBodyData result = new ResBodyData();
				result.setStatus(BaseApiCode.REQUEST_PARAMS_ERROR);
				result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.REQUEST_PARAMS_ERROR));
				result.setData(new JSONObject());
				return result;
			}
			SysuserAccount sysuserAccount = (SysuserAccount) request.getAttribute("sysuserAccount");
			return shopService.collectOrCancelShop(shopId, sysuserAccount, isCollect);
		} catch (Exception e) {
			logger.error("收藏店铺，服务器异常：" + e);
			ResBodyData result = new ResBodyData();
			result.setStatus(BaseApiCode.OPERAT_FAIL);
			result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.OPERAT_FAIL));
			result.setData(new JSONObject());
			return result;
		}
	}

	/**
	 * 获取店铺商品分类
	 * 
	 * @param shop_id
	 * @return
	 */
	@RequestMapping(value = "/getShopCatalog")
	public ResBodyData getShopProductCatalog(String shop_id) {
		try {
			logger.info("获取店铺商品分类，店铺ID：" + shop_id);
			int shopId = 0;
			try {
				shopId = Integer.parseInt(shop_id);
			} catch (Exception e) {
				logger.error("获取店铺商品分类，店铺ID错误：" + e);
				ResBodyData result = new ResBodyData();
				result.setStatus(BaseApiCode.REQUEST_PARAMS_ERROR);
				result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.REQUEST_PARAMS_ERROR));
				result.setData(new JSONObject());
				return result;
			}
			return shopService.getShopProductCatalog(shopId);
		} catch (Exception e) {
			logger.error("获取店铺商品分类，服务器异常：" + e);
			ResBodyData result = new ResBodyData();
			result.setStatus(BaseApiCode.OPERAT_FAIL);
			result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.OPERAT_FAIL));
			result.setData(new JSONObject());
			return result;
		}
	}

	/**
	 * 获取店铺列表
	 * 
	 * @param shop_id
	 * @param order_by
	 *            排序字段：store按销量，updateTime按修改时间，price按价格，point按积分；默认store按销量
	 * @param column
	 *            排序规则：desc降序，asc升序；默认desc降序
	 * @return
	 */
	@RequestMapping(value = "/getProductList")
	public ResBodyData getShopProductList(String shop_id, String order_by, String column) {
		try {
			logger.info("获取店铺商品列表，店铺ID：" + shop_id);
			int shopId = 0;
			try {
				shopId = Integer.parseInt(shop_id);
			} catch (Exception e) {
				logger.error("获取店铺商品列表，店铺ID错误：" + e);
				ResBodyData result = new ResBodyData();
				result.setStatus(BaseApiCode.REQUEST_PARAMS_ERROR);
				result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.REQUEST_PARAMS_ERROR));
				result.setData(new JSONObject());
				return result;
			}
			return shopService.getShopProductList(shopId, order_by, column);
		} catch (Exception e) {
			logger.error("获取店铺商品列表，服务器异常：" + e);
			ResBodyData result = new ResBodyData();
			result.setStatus(BaseApiCode.OPERAT_FAIL);
			result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.OPERAT_FAIL));
			result.setData(new JSONObject());
			return result;
		}
	}
}