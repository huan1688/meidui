package com.meiduimall.application.search.manage.services.impl;

import java.util.Date;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meiduimall.application.search.manage.IDao.IndexLogMapper;
import com.meiduimall.application.search.manage.page.PageView;
import com.meiduimall.application.search.manage.page.QueryResult;
import com.meiduimall.application.search.manage.pojo.IndexLog;
import com.meiduimall.application.search.manage.services.IndexLogService;

@Service
public class IndexLogServiceImpl implements IndexLogService {

	@Autowired
	private IndexLogMapper indexLogMapper;
	
	@Override
	public QueryResult queryIndexLogs(PageView pageView)  {
		List<IndexLog> indexLogs =indexLogMapper.queryIndexLogs(pageView);
		int count =indexLogMapper.queryIndexLogCount();
		QueryResult qr = new QueryResult();
		qr.setDateList(indexLogs);
		qr.setTotalCount(count);
		return qr;
	}

	@Override
	public int insertIndexLog(String remark)  {
		IndexLog indexLogs = new IndexLog();
		indexLogs.setRemark(remark);
		indexLogs.setLogTime(new Date());
		return indexLogMapper.insertIndexLog(indexLogs);
	}

	@Override
	public int deleteIndexLogById(int logId) {
		return indexLogMapper.deleteIndexLogById(logId);
	}

}