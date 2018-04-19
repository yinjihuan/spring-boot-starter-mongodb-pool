# spring-boot-starter-mongodb-pool

在日常工作中，我们通过Spring Data Mongodb来操作Mongodb数据库，在Spring Boot中只需要引入spring-boot-starter-data-mongodb即可。

很多时候我们往往需要操作多个数据库（微服务架构下一个服务一个独立的库），最简单的方式就是在项目中为每个数据库配置下，比如：

```
@Bean 
@Primary
public MongoDbFactory mongoDbFactory() throws Exception {
    // 省略...
   return new SimpleMongoDbFactory(mongoClient, "db1");
}

@Bean
@Primary
public MongoTemplate mongoTemplate() throws Exception {
	  //省略...
	  MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory(), converter);
	  return mongoTemplate;
}

@Bean 
@Qualifier("mongoDbFactory2")
public MongoDbFactory mongoDbFactory2()hrows Exception {
    // 省略...
   return new SimpleMongoDbFactory(mongoClient, "db2);
}

@Bean
@Qualifier("mongoTemplate2")
public MongoTemplate mongoTemplate2() hrows Exception {
	  //省略...
	  MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory(), converter);
	  return mongoTemplate;
}
```
