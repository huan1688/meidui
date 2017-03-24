package com.meiduimall.service.settlement.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

/**
 * Copyright (C), 2002-2017, 美兑壹购物
 * FileName: RedisConfig.java
 * Author:   陈建宇
 * Date:     2017年3月14日 下午3:37:58
 * Description: Redis启动配置类
 */
@Configuration
@ImportResource("classpath:applicationContext-common.xml")
public class RedisConfig {
	
	@Bean(name ="shardedJedisPool")
	public ShardedJedisPool shardedJedisPool(@Qualifier("jedis.config")JedisPoolConfig config,
			@Qualifier("jedisShardInfo") JedisShardInfo jedisShardInfo) {
		List<JedisShardInfo> list=new ArrayList<JedisShardInfo>();
		list.add(jedisShardInfo);
		return new ShardedJedisPool(config,list);
	}

	
	@Bean(name="jedisShardInfo")
	public JedisShardInfo jedisShardInfo(@Value("${config.jedis.jedisShardInfo.host}") String host,
			@Value("${config.jedis.jedisShardInfo.port}") int port,@Value("${config.jedis.jedisShardInfo.password}") String password) {
		JedisShardInfo jedisShardInfo=new JedisShardInfo(host, port);
		jedisShardInfo.setPassword(password);
		return jedisShardInfo;
	}
	
	
	@Bean(name = "jedis.config")
	public JedisPoolConfig jedisPoolConfig(@Value("${config.jedis.maxTotal}") int maxTotal,
			@Value("${config.jedis.maxIdle}") int maxIdle,
			@Value("${config.jedis.maxWaitMillis}")int maxWaitMillis,
			@Value("${config.jedis.testOnBorrow}")boolean testOnBorrow,
			@Value("${config.jedis.testOnReturn}")boolean testOnReturn) {
		
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		return config;
	}
}
