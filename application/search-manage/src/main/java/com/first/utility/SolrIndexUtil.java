package com.first.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.first.pojo.ItemModel;
import com.first.pojo.ItemPropValue;
import com.first.pojo.Props;
import com.first.utility.prop.PHPSerializer;

public class SolrIndexUtil {
	
	private static final Logger log = LoggerFactory.getLogger(SolrIndexUtil.class);
	
	/**
	 * 将ItemModel对象转换为SolrInputDocument对象
	 * @param itemModel
	 * @param propMaps 
	 * @return
	 */
	public static SolrInputDocument transform2Document(ItemModel itemModel, Map<Integer, Props> propMaps) {
		try {
			SolrInputDocument doc = new SolrInputDocument();
			double price = itemModel.getPrice();
			double point = itemModel.getPoint();
			double costPrice = itemModel.getCostPrice();
			// 商品的利润（销售价-成本价）(C)
			double profit = price - costPrice;
			
			doc.addField("id", itemModel.getId());
			doc.addField("itemId", itemModel.getItemId());
			doc.addField("title", itemModel.getTitle(), 20);
			doc.addField("image", itemModel.getImage());
			
			String brandId = itemModel.getBrandId();
			String brand = itemModel.getBrand();
			doc.addField("brandId", brandId);
			doc.addField("brand", brand);
			doc.addField("brandInfo", brandId + "_" + brand);
			
			// 类目信息
			String catId = itemModel.getCatId();
			String cat = itemModel.getCat().trim();
			String catParentId = itemModel.getCatParentId();
			
			StringBuffer catInfo = new StringBuffer();
			catInfo.append(catParentId).append("_");
			catInfo.append(catId).append("_");
			catInfo.append(cat);
			
			doc.addField("catId", catId);
			doc.addField("catParentId", catParentId);
			doc.addField("cat", cat);
			doc.addField("catInfo", catInfo);
			
			// 类目路径
			String catPath = itemModel.getCatPath();
			doc.addField("catPath", catPath + catId);
			
			// 店铺信息
			StringBuffer shopInfo = new StringBuffer();
			String shopId = itemModel.getShopId();
			String shopName = itemModel.getShopName();
			String shopDescript = itemModel.getShopDescript();
			String shopLogo = itemModel.getShopLogo();
			String tallyScore = itemModel.getTallyScore();
			String attitudeScore = itemModel.getAttitudeScore();
			String deliverySpeedScore = itemModel.getDeliverySpeedScore();
			
			shopInfo.append(shopId).append("_");
			shopInfo.append(shopName).append("_");
			shopInfo.append(StringUtil.isEmptyByString(shopDescript)?"暂无简介~":shopDescript).append("_");
			shopInfo.append(StringUtil.isEmptyByString(shopLogo)?"default":shopLogo).append("_");
			shopInfo.append(StringUtil.isEmptyByString(tallyScore)?"5":tallyScore).append("_");
			shopInfo.append(StringUtil.isEmptyByString(attitudeScore)?"5":attitudeScore).append("_");
			shopInfo.append(StringUtil.isEmptyByString(deliverySpeedScore)?"5":deliverySpeedScore);
			
			doc.addField("shopId", shopId);
			doc.addField("shopName", shopName);
			doc.addField("shopInfo", shopInfo);
			
			// 商品在若干天内的访问次数(A)
			Integer viewCount = itemModel.getViewCount();
			
			// 商品在若干天内的销量(B)
			Integer soldQuantity = itemModel.getSoldQuantity();
			
			// 离当前的上架天数(D)
			Integer listTime = itemModel.getListTime();
			if (listTime == null) {
				listTime = 0;
			}
			int now = (int) (System.currentTimeMillis()/1000);
			int listDays = now - listTime;
			
			// 若干天内的好评率(E)
			Integer buyCount = itemModel.getBuyCount();			
			Integer rateCount = itemModel.getRateCount();
			Integer rateGoodCount = itemModel.getRateGoodCount();
			double goodRate = (1+rateGoodCount)/(1+rateCount);
			
			// 若干天内的收藏率(F)
			Integer favCount = itemModel.getFavCount();
			if (favCount == null) {
				favCount = 0;
			}
			double favRate = (favCount+1)/(viewCount+1.0);
			
			// 商品的排序系数=[(1+B)/(1+A)]*(1+C)*1/D*(1+E)*[(1+F)/(1+A)]。
			double score = ((1+soldQuantity)/(1+viewCount))*(1+profit)*1.0/listDays*(1+goodRate)*((1+favRate)/(1+viewCount));
			
			doc.addField("score", score);
			
			doc.addField("sold_quantity", soldQuantity);
			doc.addField("rate_count", rateCount);
			doc.addField("buy_count", buyCount);
			doc.addField("view_count", viewCount);
			doc.addField("list_time", listTime);
			
			doc.addField("price", price);
			doc.addField("point", point);
			
			// 转换属性属性值
			String specDesc = itemModel.getSpecDesc();
			if (specDesc != null) {
				Map<Integer, ArrayList<ItemPropValue>> transformProp = transformProp(specDesc);
				Collection<ArrayList<ItemPropValue>> values = transformProp.values();
				Set<String> props = new HashSet<String>();
				for (ArrayList<ItemPropValue> itemPropValues : values) {
					ItemPropValue itemPropValue = itemPropValues.get(0);
					if (itemPropValue != null) {
						Integer pvid = itemPropValue.getPropValueId();
						Props prop = propMaps.get(pvid);
						if (prop == null) {
							continue;
						}
						Integer pid = itemPropValue.getPropId();
						String propName = prop.getPropName();
						String propValueName = prop.getPropValue();
						if (StringUtil.isEmptyByString(propName) || StringUtil.isEmptyByString(propValueName)) {
							continue;
						}
						// 添加动态属性	addField(prop_id_name, valueId_value)
						String field = "prop_" + pid + "_" + propName;
						String value = pvid + "_" + propValueName.trim();
						doc.addField(field , value);
						props.add(propValueName);
						// 动态查询属性
						doc.addField("attr_" + pid, pvid);
					}
				}
				doc.addField("props", props);
			}
			return doc;
		} catch (Exception e) {
			log.error("SolrDocument转换参数异常", e);
			return null;
		}
	}
	
	/**
	 * 转换属性-属性值
	 * @param specDesc
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static Map<Integer, ArrayList<ItemPropValue>> transformProp(String specDesc) throws Exception {
		HashMap<Integer,ArrayList<ItemPropValue>> itemProp = new HashMap<Integer,ArrayList<ItemPropValue>>();
		Object obj = PHPSerializer.unserialize(specDesc.getBytes("utf-8"));
        HashMap  propMap = (HashMap)obj; 
		for ( Object key:propMap.keySet()) {
			Integer propId = (Integer)key;
			ArrayList<ItemPropValue> propValueList = new ArrayList<ItemPropValue>();
			Object obj1 = propMap.get(key);
			if ( obj1 instanceof java.util.ArrayList){
				break;
			}
			
			HashMap  propValueMap = (HashMap)propMap.get(key);
			if ( propValueMap == null ){
				break;
			}
			for (Object key1:propValueMap.keySet()){
				Integer propValueId = (Integer)key1;
				HashMap  valueMap = (HashMap)propValueMap.get(key1);
				
				if (null != isPropValueExist(propId,propValueId,(String)valueMap.get("spec_value"),propValueList)){
					continue;
				}
				ItemPropValue propValue = new ItemPropValue();
				propValue.setPropId(propId);
				propValue.setPropValueId(propValueId);
				propValue.setPropValueName((String)valueMap.get("spec_value"));
				propValueList.add(propValue);
			}

			ArrayList<ItemPropValue> propValueOldList = itemProp.get(propId);
			if ( propValueOldList == null || propValueOldList.size() < propValueList.size()) {
				itemProp.put(propId, propValueList);
			}
		}
		return itemProp;
	}

	private static ItemPropValue isPropValueExist(Integer propId,Integer propValueId,String valueName,ArrayList<ItemPropValue> propValueList){
		for (ItemPropValue propValue:propValueList) {
			if ( propId.equals(propValue.getPropId()) 
					&& valueName != null
					&& valueName.equals(propValue.getPropValueName())){
				return propValue;
			}
			if ( propId.equals(propValue.getPropId()) 
					&& propValueId.equals(propValue.getPropValueId())){
				return propValue;
			}
		}
		return null;
	}
}