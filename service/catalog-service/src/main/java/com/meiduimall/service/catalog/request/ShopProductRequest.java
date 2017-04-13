package com.meiduimall.service.catalog.request;

public class ShopProductRequest {

	// 店铺ID
	private Integer shop_id;

	// 店铺自定义分类ID
	private Integer shop_cat_id;

	// 排序字段：store 按销量，updateTime 按修改时间，price 按价格，point 按积分；默认 store 按销量
	private String order_by;

	// 排序规则：desc 降序，asc 升序；默认 desc 降序
	private String column;

	private Integer pageNo;
	private Integer pageSize;

	public Integer getShop_cat_id() {
		return shop_cat_id;
	}

	public void setShop_cat_id(Integer shop_cat_id) {
		this.shop_cat_id = shop_cat_id;
	}

	public Integer getShop_id() {
		return shop_id;
	}

	public void setShop_id(Integer shop_id) {
		this.shop_id = shop_id;
	}

	public String getOrder_by() {
		return order_by;
	}

	public void setOrder_by(String order_by) {
		this.order_by = order_by;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
}
