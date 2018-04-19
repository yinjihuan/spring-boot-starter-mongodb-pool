package com.cxytiandi.mongodb.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.cxytiandi.mongodb.EnableMongoPool;

/**
 * Mongodb连接池/多数据源扩展使用 Spring Boot集成案例
 * 
 * @author yinjihuan
 * 
 * @about http://cxytiandi.com/about
 *
 */
@SpringBootApplication
@EnableMongoPool
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
}