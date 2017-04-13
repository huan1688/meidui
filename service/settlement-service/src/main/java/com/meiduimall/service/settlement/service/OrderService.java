package com.meiduimall.service.settlement.service;

import java.util.Collection;
import java.util.List;

import com.meiduimall.service.settlement.model.EcmMzfOrderStatus;
import com.meiduimall.service.settlement.model.EcmMzfShareProfit;
import com.meiduimall.service.settlement.model.EcmOrder;
import com.meiduimall.service.settlement.vo.ShareProfitVO;

/**
 * Copyright (C), 2002-2017, 美兑壹购物
 * FileName: OrderService.java
 * Author:   许彦雄
 * Date:     2017年3月14日 下午3:37:58
 * Description: 订单分润和分润数据查询相关服务
 */
public interface OrderService {
	
	/**
	 * 功能描述:  构建订单分润数据模型
	 * Author: 许彦 雄
	 * Date:   2017年3月14日 下午3:38:26   
	 * param ecmOrder
	 * param errors
	 * return  EcmMzfShareProfit
	 * 
	 */
	public EcmMzfShareProfit buildShareProfit(EcmOrder ecmOrder,Collection<String> errors) ;

	/**
	 * 功能描述:  根据订单号列表查询订单状态
	 * Author: 吴军
	 * Date:   2017年3月14日 下午3:38:26   
	 * param orderSns
	 * return  List<EcmMzfOrderStatus>
	 * 
	 */
	public List<EcmMzfOrderStatus> queryOrderStatus(List<String> orderSns) ;

	/**
	 * 功能描述:  保存分润数据(为了解决 Spring声明式事务 同一类内该方法被saveShareProfit方法调用事务失效,需要抽出为接口)
	 * Author: 许彦雄
	 * Date:   2017年3月14日 下午3:38:26   
	 * param shareProfit
	 * return 
	 * 
	 */
	public void saveShareProfit(EcmMzfShareProfit shareProfit) ;
	
	/**
	 * 功能描述:  同步订单审核状态接口
	 * Author: 许彦雄
	 * Date:   2017年3月14日 下午3:38:26   
	 * param orderStatus
	 * return  Boolean
	 * 
	 */
	public Boolean syncVerifyStatus(EcmMzfOrderStatus orderStatus) ;
	

	/**
	 * 功能描述:  根据订单号列表查询订单分润数据
	 * Author: 许彦雄
	 * Date:   2017年3月14日 下午3:38:26   
	 * param orderSns
	 * return  List<EcmMzfShareProfit>
	 * 
	 */
	public List<EcmMzfShareProfit> queryShareProfit(Collection<String> orderSns) ;
	
	/**
	 * 功能描述:  按登陆的个代或区代查询今日佣金金额和待结算金额
	 * Author: 许彦雄
	 * Date:   2017年3月14日 下午3:38:26   
	 * param code
	 * param accountRoleType
	 * return  ShareProfitVO
	 * 
	 */
	public ShareProfitVO queryProfitByRole(String code, Integer accountRoleType) ;

	/**
	 * 功能描述:  按登陆的个代或区代以及流水编号查询分润数据
	 * Author: 许彦雄
	 * Date:   2017年3月14日 下午3:38:26   
	 * param waterId
	 * param loginType
	 * param code
	 * param pageNumber
	 * param pageSize
	 * return  List<EcmMzfShareProfit>
	 * 
	 */
	public List<EcmMzfShareProfit> queryProfitByWaterByType(String waterId, Integer loginType, String code,Integer pageNumber,Integer pageSize);
	
	/**
	 * 功能描述:  按流水编号查询分润数据记录数
	 * Author: 许彦雄
	 * Date:   2017年3月14日 下午3:38:26   
	 * param waterId
	 * return  int
	 * 
	 */
	public int queryProfitCountByWaterId(String waterId);

	/**
	 * 功能描述:  根据订单号查询分润数据是否存在
	 * Author: 许彦雄
	 * Date:   2017年3月14日 下午3:38:26   
	 * param orderSn
	 * return  boolean
	 * 
	 */
	public boolean checkShareProfitExisted(String orderSn);

	/**
	 * 功能描述:  根据代理或商家编号查询分润数据接口
	 * Author: 许彦 雄
	 * Date:   2017年3月28日 下午14:25:02
	 * param codes
	 * param billStartDate
	 * param billEndDate
	 * return  ResBodyData
	 * 
	 */
	public List<ShareProfitVO> queryTotalProfit(Collection<String> codes, Integer billStartDate, Integer billEndDate) ;


}
