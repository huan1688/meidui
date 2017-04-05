package com.meiduimall.service.catalog.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.meiduimall.core.BaseApiCode;
import com.meiduimall.core.ResBodyData;
import com.meiduimall.service.catalog.dao.BaseDao;
import com.meiduimall.service.catalog.entity.CheckGoodsResult;
import com.meiduimall.service.catalog.entity.JsonItemDetailResult;
import com.meiduimall.service.catalog.entity.JsonItemDetailResult_ItemData;
import com.meiduimall.service.catalog.entity.JsonItemDetailResult_Prop_Values;
import com.meiduimall.service.catalog.entity.JsonItemDetailResult_Props;
import com.meiduimall.service.catalog.entity.JsonItemDetailResult_ShopData;
import com.meiduimall.service.catalog.entity.JsonItemDetailResult_Sku;
import com.meiduimall.service.catalog.entity.SyscategoryProps;
import com.meiduimall.service.catalog.entity.SysitemItemCount;
import com.meiduimall.service.catalog.entity.SysitemItemDesc;
import com.meiduimall.service.catalog.entity.SysitemItemStatus;
import com.meiduimall.service.catalog.entity.SysitemItemWithBLOBs;
import com.meiduimall.service.catalog.entity.SysitemSkuExample;
import com.meiduimall.service.catalog.entity.SysitemSkuWithBLOBs;
import com.meiduimall.service.catalog.entity.SysrateDsrWithBLOBs;
import com.meiduimall.service.catalog.entity.SysshopShopWithBLOBs;
import com.meiduimall.service.catalog.service.GoodsDetailService;
import com.meiduimall.service.catalog.util.Logger;
import com.meiduimall.service.catalog.util.NumberFormatUtil;
import com.meiduimall.service.catalog.util.ParserItemSpecDescBean;
import com.meiduimall.service.catalog.util.ParserItemSpecDescBean.PropBean;
import com.meiduimall.service.catalog.util.ParserItemSpecDescUtil;
import com.meiduimall.service.catalog.util.ParserSkuSpecDescBean;
import com.meiduimall.service.catalog.util.ParserSkuSpecDescUtil;
import com.meiduimall.service.catalog.util.ParserSysRateDsrInfo;

@Service
public class GoodsDetailServiceImpl implements GoodsDetailService {

	@Autowired
	private Environment env;

	@Autowired
	private BaseDao baseDao;

	@Override
	public ResBodyData checkItemIsExistById(int item_id) {
		ResBodyData result = new ResBodyData();
		try {
			
			/** TODO --------查询这个商品ID是否存在-------- */
			int count = baseDao.selectOne(item_id, "SysitemItemMapper.getItemCountByItemId");
			if (count > 0) {
				// 返回访问这个商品的详情页的地址
				CheckGoodsResult bean = new CheckGoodsResult();
				String base_url = env.getProperty("estore.base-url");
				String url = base_url + "/item.html?item_id=" + item_id;
				bean.setUrl(url);

				result.setData(bean);
				result.setStatus(BaseApiCode.SUCCESS);
				result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.SUCCESS));
			} else {
				result.setData("{}");
				result.setStatus(BaseApiCode.NONE_DATA);
				result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.NONE_DATA));
			}
			
			/** TODO ----------查询该商品状态------- */
			/*SysitemItemStatus itemStatus = baseDao.selectOne(item_id, "SysitemItemStatusMapper.selectByPrimaryKey");
			String approveStatus = itemStatus.getApproveStatus();
			if ("onsale".equals(approveStatus)) {
				// 返回访问这个商品的详情页的地址
				CheckGoodsResult bean = new CheckGoodsResult();
				String base_url = env.getProperty("estore.base-url");
				String url = base_url + "/item.html?item_id=" + item_id;
				bean.setUrl(url);

				result.setData(bean);
				result.setStatus(BaseApiCode.SUCCESS);
				result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.SUCCESS));
			} else {
				result.setData("{}");
				result.setStatus(BaseApiCode.NONE_DATA);
				result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.NONE_DATA));
			}*/
			
		} catch (Exception e) {
			result.setData("{}");
			result.setStatus(BaseApiCode.OPERAT_FAIL);
			result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.OPERAT_FAIL));
			Logger.error("查询商品信息，service报异常：%s", e);
		}
		return result;
	}

	@Override
	public ResBodyData getItemDetailById(String token, Integer item_id) {
		/**
		 * <table schema="" tableName="sysitem_item">
		 * <table schema="" tableName="sysitem_item_desc">
		 * <table schema="" tableName="sysitem_item_status">
		 * <table schema="" tableName="sysitem_item_count">
		 * 
		 * <table schema="" tableName="sysitem_sku">
		 * 
		 * <table schema="" tableName="sysshop_shop">
		 * <table schema="" tableName="sysrate_dsr">
		 * 
		 * <table schema="" tableName="syscategory_props">
		 * <table schema="" tableName="syscategory_prop_values">
		 * 
		 * <table schema="" tableName="sysuser_account">
		 * <table schema="" tableName="sysuser_user">
		 * <table schema="" tableName="sysuser_user_fav">
		 */
		ResBodyData result = new ResBodyData();// 最终返回的数据

		try {
			SysitemItemWithBLOBs itemWithBLOBs = baseDao.selectOne(item_id, "SysitemItemMapper.selectByPrimaryKey");
			if (itemWithBLOBs == null) {// 查询不到该商品
				result.setStatus(BaseApiCode.NONE_DATA);
				result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.NONE_DATA));
				result.setData("{}");
				return result;
			}

			JsonItemDetailResult jsonResult = new JsonItemDetailResult();
			// ----------1、开始拼接商品规格数据-----------
			List<JsonItemDetailResult_Props> itemPropsList = new ArrayList<JsonItemDetailResult_Props>();

			// 反序列化数据---解析商品的规格参数
			List<ParserItemSpecDescBean> parseList = ParserItemSpecDescUtil.parser(itemWithBLOBs.getSpecDesc());
			if (parseList != null && parseList.size() > 0) {
				JsonItemDetailResult_Props itemProps = null;
				for (int i = 0; i < parseList.size(); i++) {

					// 规格
					itemProps = new JsonItemDetailResult_Props();

					// 获取每一组规格
					ParserItemSpecDescBean parserItemSpecDescBean = parseList.get(i);

					if (parserItemSpecDescBean != null) {
						// 根据规格ID查找规格名称--TODO 待优化
						System.out.println(
								"parserItemSpecDescBean.getProp_id()----------" + parserItemSpecDescBean.getProp_id());
						SyscategoryProps categoryProps = baseDao.selectOne(parserItemSpecDescBean.getProp_id(),
								"SyscategoryPropsMapper.selectByPrimaryKey");

						itemProps.setProp_id(parserItemSpecDescBean.getProp_id() + "");
						itemProps.setProp_name(categoryProps.getPropName());

						// 遍历该规格下的每一种规格属性
						List<PropBean> propBeanList = parserItemSpecDescBean.getPropBeanList();
						if (propBeanList != null && propBeanList.size() > 0) {
							List<JsonItemDetailResult_Prop_Values> prop_list = new ArrayList<JsonItemDetailResult_Prop_Values>();
							JsonItemDetailResult_Prop_Values propValues = null;
							for (int j = 0; j < propBeanList.size(); j++) {
								PropBean propBean = propBeanList.get(j);
								if (propBean != null) {
									propValues = new JsonItemDetailResult_Prop_Values();
									Integer spec_value_id = propBean.getPropValueBean().getSpec_value_id();
									String spec_value = propBean.getPropValueBean().getSpec_value();
									propValues.setProp_value_id(spec_value_id + "");
									propValues.setProp_value(spec_value);
									prop_list.add(propValues);
									propValues = null;
								} else {
									// can not reach
									continue;
								}
							}
							itemProps.setProp_list(prop_list);
						} else {
							// 只有规格名称，没有规格对应的规格属性
							continue;
						}
						itemPropsList.add(itemProps);
					} else {
						continue;
					}
				}
			} else {
				// TODO 查找不到规格参数
			}
			jsonResult.setItemPropsList(itemPropsList);

			// --------2、开始拼接商品信息数据-----------
			JsonItemDetailResult_ItemData itemData = new JsonItemDetailResult_ItemData();

			// 获取商品详情的HTML页面地址
			String html_detail_url = "";
			SysitemItemDesc itemDesc = baseDao.selectOne(item_id, "SysitemItemDescMapper.selectByPrimaryKey");
			String wapDesc = itemDesc.getWapDesc();
			String pcDesc = itemDesc.getPcDesc();
			if (!StringUtils.isEmpty(wapDesc)) {
				html_detail_url = wapDesc;
			} else {
				html_detail_url = pcDesc;
			}
			itemData.setHtml_detail_url(html_detail_url);

			SysitemItemCount itemCount = baseDao.selectOne(item_id, "SysitemItemCountMapper.selectByPrimaryKey");
			// 评论数量
			Integer rateCount = itemCount.getRateCount();
			if (rateCount == null) {
				itemData.setRate_count("0");
			} else {
				itemData.setRate_count("" + rateCount);
			}

			// 商品销量=虚拟销量+实际销量
			Integer soldQuantity = itemCount.getSoldQuantity();
			Integer vituralQuantity = itemCount.getVituralQuantity();
			int sold = 0;
			int vitural = 0;
			if (soldQuantity != null) {
				sold = soldQuantity;
			}
			if (vituralQuantity != null) {
				vitural = vituralQuantity;
			}
			int rate_count = sold + vitural;
			itemData.setSales_volume(rate_count + "");

			SysitemItemStatus itemStatus = baseDao.selectOne(item_id, "SysitemItemStatusMapper.selectByPrimaryKey");
			// 商品状态
			String approveStatus = itemStatus.getApproveStatus();
			if ("onsale".equals(approveStatus)) {
				itemData.setApprove_status("出售中");
			} else {
				itemData.setApprove_status("库中");
			}

			// 商品上架时间
			Integer listTime = itemStatus.getListTime();
			if (listTime != null) {
				itemData.setList_time("" + listTime);
			} else {
				itemData.setList_time("");
			}

			itemData.setBn(itemWithBLOBs.getBn());
			itemData.setImage_default_id(itemWithBLOBs.getImageDefaultId());
			itemData.setItme_id(itemWithBLOBs.getItemId() + "");
			itemData.setList_image(itemWithBLOBs.getListImage());
			itemData.setPoint(itemWithBLOBs.getPoint() + "");
			itemData.setPrice(itemWithBLOBs.getPrice() + "");

			String subTitle = itemWithBLOBs.getSubTitle();
			String title = itemWithBLOBs.getTitle();
			if (StringUtils.isEmpty(subTitle)) {
				subTitle = title;
			}
			itemData.setSub_title(subTitle);
			itemData.setTitle(title);
			BigDecimal weight = itemWithBLOBs.getWeight();
			if (weight != null) {
				itemData.setWeight(weight.intValue() + "");
			} else {
				itemData.setWeight("");
			}

			// 检查用户是否收藏了该商品
			if (StringUtils.isEmpty(token)) {
				// 没有token，不需要处理
				itemData.setIs_collect("0");
			} else {
				// TODO 处理token
				itemData.setIs_collect("1");
			}
			jsonResult.setItemData(itemData);

			// -------------3、开始拼接商品SKU数据-----------
			List<JsonItemDetailResult_Sku> skuList = new ArrayList<JsonItemDetailResult_Sku>();

			SysitemSkuExample skuExample = new SysitemSkuExample();
			SysitemSkuExample.Criteria criteria = skuExample.createCriteria();
			criteria.andItemIdEqualTo(item_id);
			List<SysitemSkuWithBLOBs> itemSkuWithBLOBsList = baseDao.selectList(skuExample,
					"SysitemSkuMapper.selectByExampleWithBLOBs");

			if (itemSkuWithBLOBsList != null && itemSkuWithBLOBsList.size() > 0) {
				JsonItemDetailResult_Sku result_sku = null;
				System.out.println("itemSkuWithBLOBsList.size()--------" + itemSkuWithBLOBsList.size());
				for (int i = 0; i < itemSkuWithBLOBsList.size(); i++) {
					SysitemSkuWithBLOBs sysitemSkuWithBLOBs = itemSkuWithBLOBsList.get(i);
					if (sysitemSkuWithBLOBs == null) {
						continue;// can not reach
					}
					result_sku = new JsonItemDetailResult_Sku();

					result_sku.setPoint(sysitemSkuWithBLOBs.getPoint() + "");
					result_sku.setPrice(sysitemSkuWithBLOBs.getPrice() + "");
					result_sku.setSku_id(sysitemSkuWithBLOBs.getSkuId() + "");
					result_sku.setSpec_info(sysitemSkuWithBLOBs.getSpecInfo());
					String sku_status = sysitemSkuWithBLOBs.getStatus();
					if ("normal".equals(sku_status)) {
						result_sku.setStatus("正常");
					} else {
						result_sku.setStatus("删除");
					}
					result_sku.setWeight(sysitemSkuWithBLOBs.getWeight() + "");

					// 反序列化数据---解析每一个商品对应的SKU数据
					List<ParserSkuSpecDescBean> skuSpecDescBeanList = ParserSkuSpecDescUtil
							.parser(sysitemSkuWithBLOBs.getSpecDesc());
					if (skuSpecDescBeanList != null && skuSpecDescBeanList.size() > 0) {
						StringBuffer sb = new StringBuffer();
						for (int j = 0; j < skuSpecDescBeanList.size(); j++) {
							Integer prop_value_id = skuSpecDescBeanList.get(j).getProp_value_id();
							sb.append(prop_value_id + "_");
						}
						if (sb.length() > 1) {
							String prop_value_ids = sb.substring(0, sb.length() - 1);
							result_sku.setProp_value_ids(prop_value_ids);
						} else {
							// TODO 没有找到prop_value_id(一般不会发生)
						}
					} else {
						// TODO 反序列化失败
					}
					skuList.add(result_sku);
					result_sku = null;
				}
			} else {
				// TODO 没有SKU
			}
			jsonResult.setSkuList(skuList);

			// -------------4、开始拼接商家数据-----------
			JsonItemDetailResult_ShopData shopData = new JsonItemDetailResult_ShopData();
			Integer shopId = itemWithBLOBs.getShopId();
			SysshopShopWithBLOBs shopWithBLOBs = baseDao.selectOne(shopId, "SysshopShopMapper.selectByPrimaryKey");
			SysrateDsrWithBLOBs rateDsrWithBLOBs = baseDao.selectOne(new Long(shopId),
					"SysrateDsrMapper.selectByPrimaryKey");

			// 反序列化数据---解析店铺信息中的：描述相符、服务态度、发货速度的分值
			if (rateDsrWithBLOBs != null) {
				float fTallyDsr = ParserSysRateDsrInfo.getValue(rateDsrWithBLOBs.getTallyDsr());
				float fDeliverySpeedDsr = ParserSysRateDsrInfo.getValue(rateDsrWithBLOBs.getDeliverySpeedDsr());
				float fAttitudeDsr = ParserSysRateDsrInfo.getValue(rateDsrWithBLOBs.getAttitudeDsr());

				shopData.setAttitude_dsr(NumberFormatUtil.formatString(fAttitudeDsr, 1));
				shopData.setDelivery_speed_dsr(NumberFormatUtil.formatString(fDeliverySpeedDsr, 1));
				shopData.setTally_dsr(NumberFormatUtil.formatString(fTallyDsr, 1));
			} else {
				shopData.setAttitude_dsr("5.0");
				shopData.setDelivery_speed_dsr("5.0");
				shopData.setTally_dsr("5.0");
			}

			shopData.setShop_descript(shopWithBLOBs.getShopDescript());
			shopData.setShop_id(shopId + "");
			shopData.setShop_logo(shopWithBLOBs.getShopLogo());
			shopData.setShop_name(shopWithBLOBs.getShopName());
			String shopType = shopWithBLOBs.getShopType();
			if ("brand".equals(shopType)) {
				shopData.setShop_type("品牌专卖店");
			} else if ("cat".equals(shopType)) {
				shopData.setShop_type("类目专营店");
			} else if ("flag".equals(shopType)) {
				shopData.setShop_type("品牌旗舰店");
			} else if ("self".equals(shopType)) {
				shopData.setShop_type("运营商自营店铺");
			} else {
				shopData.setShop_type("未知");
			}
			jsonResult.setShopData(shopData);

			result.setData(jsonResult);
			result.setStatus(BaseApiCode.SUCCESS);
			result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.SUCCESS));

		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("根据商品编号，获取商品详情，service报异常：%s", e);
			result.setStatus(BaseApiCode.OPERAT_FAIL);
			result.setMsg(BaseApiCode.getZhMsg(BaseApiCode.OPERAT_FAIL));
			result.setData("{}");
		}
		return result;
	}

}