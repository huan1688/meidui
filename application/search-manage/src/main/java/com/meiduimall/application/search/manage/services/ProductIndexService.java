package com.meiduimall.application.search.manage.services;

import java.util.List;

import com.meiduimall.application.search.manage.domain.Item;
import com.meiduimall.application.search.manage.pojo.QueryIndexResult;
import com.meiduimall.application.search.manage.pojo.SearchParam;

public interface ProductIndexService {

	/**
	 * 查询索引信息
	 * @param searchParam	查询参数
	 * @return
	 * @throws Exception
	 */
	public QueryIndexResult query(SearchParam searchParam) ;
	
	/**
	 * 根据ID查询索引信息
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Item queryById(String id) ;
	
	/**
	 * 查询索引库ID是否存在
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public boolean isExists(Integer id) ;
	
	/**
	 * 查询索引索引ID
	 * @return
	 * @throws Exception
	 */
	public List<Integer> queryIds() ;
	
	
	/**
	 * 根据条件查询索引ID
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public List<String> queryIndexByQuery(String query) ;
	
}