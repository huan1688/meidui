package com.meiduimall.service.settlement.service;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.meiduimall.service.settlement.model.EcmMzfBillWater;
import com.meiduimall.service.settlement.vo.BilledWaterVO2Merge;
import com.meiduimall.service.settlement.vo.OrderToBilledVO;

/**
 * Copyright (C), 2002-2017, 美兑壹购
 * FileName: BillService.java
 * Author:   许彦雄
 * Date:     2017年2月21日 下午6:15:47
 * Description: 创建账单相关服务
 */
public interface BillService {
	

	/**
	 * 功能描述:  生成账单
	 * Author: 许彦 雄
	 * Date:   2017年3月14日 下午3:38:26   
	 * return  Collection<String>
	 * throws Exception
	 */
	public Collection<String> createBills() throws Exception;
	
	/**
	 * 功能描述:  生成账单流水
	 * Author: 许彦 雄
	 * Date:   2017年3月14日 下午3:38:26
	 * param bill
	 * param billCreatedtime
	 * param billtime
	 * return  
	 * throws Exception
	 */
	public void handleBill(EcmMzfBillWater bill,Date billCreatedtime,Date billtime,Timestamp opTime) throws Exception;
	

	/**
	 * 功能描述:  生成billId和orderSn关系表
	 * Author: 许彦 雄
	 * Date:   2017年3月14日 下午3:38:26
	 * param bill
	 * param orderToBilledList
	 * return  
	 * throws Exception
	 */
	public void createBillAndOrderMapping(EcmMzfBillWater bill,List<OrderToBilledVO> orderToBilledList) throws Exception;
	
	/**
	 * 功能描述:  更新订单结算状态
	 * Author: 许彦 雄
	 * Date:   2017年3月14日 下午3:38:26
	 * param orderSnList
	 * return  
	 * throws Exception
	 */
	public void updateOrderBillStatus(Collection<String> orderSnList) throws Exception;
	
	/**
	 * 功能描述:  合并账单流水
	 * Author: 许彦 雄
	 * Date:   2017年3月14日 下午3:38:26
	 * param waterVOList
	 * return  
	 * throws Exception
	 */
	public void mergeBilledWaters(List<BilledWaterVO2Merge> waterVOList) throws Exception;
}
