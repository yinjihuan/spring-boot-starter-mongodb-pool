package com.cxytiandi.mongodb.example;

import java.io.File;
import java.io.FileInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mongodb.BasicDBObject;

@RestController
public class MongoController {

	@Autowired
	@Qualifier("testMongoTemplate")
	private MongoTemplate testMongoTemplate;
	
	@Autowired
	@Qualifier("logsMongoTemplate")
	private MongoTemplate logsMongoTemplate;
	
	@Autowired
	@Qualifier("gridFsTemplate")
	private GridFsTemplate gridFsTemplate;
	
	@Autowired
	@Qualifier("logsGridFsTemplate")
	private GridFsTemplate logsGridFsTemplate;
	
	@GetMapping("/")
	public Object get() throws Exception {
		gridFsTemplate.store(new FileInputStream(new File("D:\\course\\mongodb\\po\\Advertisement.java")), new BasicDBObject("name", "yinjihuan"));
		testMongoTemplate.getCollectionNames().forEach(System.out::println);
		return "success";
	}
	
	@GetMapping("/slave")
	public Object getSlave() throws Exception  {
		logsGridFsTemplate.store(new FileInputStream(new File("D:\\course\\mongodb\\po\\Advertisement.java")), new BasicDBObject("name", "yinjihuan222"));
		logsMongoTemplate.getCollectionNames().forEach(System.out::println);
		return "success";
	}
	
	@GetMapping("/save")
	public Object save() {
		User u = new User();
		u.setName("yinjihuan");
		testMongoTemplate.save(u);
		return "success";
	}
}
