package com.cxytiandi.mongodb;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Mongodb数据库连接池信息自动配置
 * 
 * @author yinjihuan
 * 
 * @about http://cxytiandi.com/about
 *
 **/
@Configuration
@Component
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class MongoPoolAutoConfiguration {
	
	@Bean
	public MongoPoolInit mongoPoolInit() {
		return new MongoPoolInit();
	}
	
}
