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

配置完成后注入使用的地方使用Qualifier注入即可使用对应的数据源，比如：

```
@Autowired
@Qualifier("mongoTemplate2")
private MongoTemplate seaweedMongoTemplate;
```

上面是直接在项目中配置多数据源的方式，接下来我们讲下一个问题，数据库连接池的问题，Mongodb的驱动中自带了连接池，但是Spring Boot Starter中却没有对连接池的配置，往往我们也需要通过代码的方式去配置，比如：

```
 @Bean
 public MongoClientOptions mongoClientOptions() {
     return new MongoClientOptions.Builder()
          //省略...
          .minConnectionsPerHost(...).build();
 }
```

虽然上面这些方式也都比较简单，但是在多个项目使用的时候都要去配置，重复代码比较严重，既然作为Starter按理来说是越简单越好，直接通过在属性文件中
配置就可以搞定，但是却不行，为此我封装了这个Pool Starter。
