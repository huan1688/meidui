package com.meiduimall.application.search.manage.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meiduimall.application.search.manage.IDao.SuggestWordMapper;
import com.meiduimall.application.search.manage.page.PageView;
import com.meiduimall.application.search.manage.pojo.SuggestWord;
import com.meiduimall.application.search.manage.services.SuggestWordService;
import com.meiduimall.application.search.manage.utility.Pinyin4jUtil;

@Service
public class SuggestWordServiceImpl implements SuggestWordService {
	
	@Autowired
	private SuggestWordMapper suggestWordMapper;

	@Override
	public List<SuggestWord> querySuggestWords(PageView pageView) {
		return suggestWordMapper.querySuggestWords(pageView);
	}

	@Override
	public long querySuggestWordCount(String kw)  {
		return suggestWordMapper.querySuggestWordCount(kw);
	}

	@Override
	public SuggestWord querySuggestWordById(Integer suggestId)  {
		return suggestWordMapper.querySuggestWordById(suggestId);
	}

	@Override
	public SuggestWord querySuggestWordByKey(String key) {
		List<SuggestWord> words = suggestWordMapper.querySuggestWordByKey(key);
		if (words == null || words.isEmpty()) {
			return null;
		}
		return words.get(0);
	}

	@Override
	public List<SuggestWord> fuzzyQuerySuggestWord(String key)  {
		return suggestWordMapper.fuzzyQuerySuggestWord(key);
	}

	@Override
	public int addSuggestWord(SuggestWord suggestWord)  {
		return suggestWordMapper.addSuggestWord(getSuggestWord(suggestWord));
	}

	@Override
	public int updateSuggestWord(SuggestWord suggestWord) {
		return suggestWordMapper.updateSuggestWord(getSuggestWord(suggestWord));
	}

	@Override
	public int deleteSuggestWordById(Integer suggestId)  {
		return suggestWordMapper.deleteSuggestWordById(suggestId);
	}

	private SuggestWord getSuggestWord(SuggestWord suggestWord) {
		String key = suggestWord.getKw();
		String pinyin = Pinyin4jUtil.getPinyin(key);
		String abbre = Pinyin4jUtil.getPinyinShort(key);
		suggestWord.setAbbre(abbre);
		suggestWord.setPinyin(pinyin);
		long updateTime = System.currentTimeMillis()/1000;
		suggestWord.setUpdateTime((int)updateTime);
		return suggestWord;
	}
}
