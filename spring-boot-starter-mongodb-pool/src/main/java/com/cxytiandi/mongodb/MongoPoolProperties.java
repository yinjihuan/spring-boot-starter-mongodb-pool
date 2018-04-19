package com.cxytiandi.mongodb;

import lombok.Data;

@Data
public class MongoPoolProperties {
	private String mongoTemplateName = "mongoTemplate";
	
	private String gridFsTemplateName = "gridFsTemplate";
	
	/**
	 * 存储时是否保存_class
	 */
	private boolean showClass = true;
	
	/**
	 * Mongo server host.
	 */
	private String host;

	/**
	 * Mongo server port.
	 */
	private Integer port = 27017;

	/**
	 * Mongo database URI. When set, host and port are ignored.
	 */
	private String uri = "mongodb://localhost/test";

	/**
	 * Database name.
	 */
	private String database;

	/**
	 * Authentication database name.
	 */
	private String authenticationDatabase;

	/**
	 * GridFS database name.
	 */
	private String gridFsDatabase;

	/**
	 * Login user of the mongo server.
	 */
	private String username;

	/**
	 * Login password of the mongo server.
	 */
	private char[] password;

    private int minConnectionsPerHost;
    private int maxConnectionsPerHost = 100;
    private int threadsAllowedToBlockForConnectionMultiplier = 5;
    private int serverSelectionTimeout = 1000 * 30;
    private int maxWaitTime = 1000 * 60 * 2;
    private int maxConnectionIdleTime;
    private int maxConnectionLifeTime;
    private int connectTimeout = 1000 * 10;
    private int socketTimeout = 0;
    private boolean socketKeepAlive = false;
    private boolean sslEnabled = false;
    private boolean sslInvalidHostNameAllowed = false;
    private boolean alwaysUseMBeans = false;

    private int heartbeatFrequency = 10000;
    private int minHeartbeatFrequency = 500;
    private int heartbeatConnectTimeout = 20000;
    private int heartbeatSocketTimeout = 20000;
    private int localThreshold = 15;
}
