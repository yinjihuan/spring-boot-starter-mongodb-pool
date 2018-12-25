package com.cxytiandi.mongodb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

/**
 * 创建多数据源MongoTemplate
 * 
 * @author yinjihuan
 * 
 * @about http://cxytiandi.com/about
 *
 **/
@Component
public class MongoPoolInit implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

	private List<MongoPoolProperties> pools = new ArrayList<MongoPoolProperties>();

	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	}

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		int index = 0;
		for (MongoPoolProperties properties : pools) {
			MongoClientOptions options = buildMongoOptions(properties);
			List<ServerAddress> seeds = Arrays.asList(new ServerAddress(properties.getHost(), properties.getPort()));
			MongoClient mongoClient = new MongoClient(seeds, options);
			SimpleMongoDbFactory mongoDbFactory = null;
			if (StringUtils.hasText(properties.getDatabase())) {
				mongoDbFactory = new SimpleMongoDbFactory(mongoClient, properties.getDatabase());
			} else {
				mongoDbFactory = new SimpleMongoDbFactory(mongoClient, properties.getGridFsDatabase());
			}
			MappingMongoConverter converter = buildConverter(mongoDbFactory, properties.isShowClass());
			boolean primary = false;
			if (index == 0) {
				primary = true;
				index++;
			}
			registryMongoTemplate(registry, primary, properties, mongoDbFactory, converter);
			registryGridFsTemplate(registry, primary, properties, mongoDbFactory, converter);
		}

	}
	
	private void registryGridFsTemplate(BeanDefinitionRegistry registry, boolean primary, MongoPoolProperties properties,
			SimpleMongoDbFactory mongoDbFactory, MappingMongoConverter converter) {
		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(GridFsTemplate.class);
		ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
		abd.setScope(scopeMetadata.getScopeName());
		abd.getConstructorArgumentValues().addGenericArgumentValue(mongoDbFactory);
		abd.getConstructorArgumentValues().addGenericArgumentValue(converter);
		abd.setPrimary(primary);
		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, properties.getGridFsTemplateName());
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}

	private void registryMongoTemplate(BeanDefinitionRegistry registry, boolean primary, MongoPoolProperties properties,
			SimpleMongoDbFactory mongoDbFactory, MappingMongoConverter converter) {
		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(MongoTemplate.class);
		ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
		abd.setScope(scopeMetadata.getScopeName());
		abd.getConstructorArgumentValues().addGenericArgumentValue(mongoDbFactory);
		abd.getConstructorArgumentValues().addGenericArgumentValue(converter);
		abd.setPrimary(primary);
		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, properties.getMongoTemplateName());
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}

	private MappingMongoConverter buildConverter(SimpleMongoDbFactory mongoDbFactory, boolean showClass) {
		MappingMongoConverter converter = new MappingMongoConverter(
				new DefaultDbRefResolver(mongoDbFactory),
				new MongoMappingContext());
		if (!showClass) {
			converter.setTypeMapper(new DefaultMongoTypeMapper(null));
		}
		return converter;
	}

	private MongoClientOptions buildMongoOptions(MongoPoolProperties properties) {
		MongoClientOptions options = new MongoClientOptions.Builder()
				.applicationName(properties.getApplicationName())
				.connectionsPerHost(properties.getMaxConnectionsPerHost())
				.minConnectionsPerHost(properties.getMinConnectionsPerHost())
				.threadsAllowedToBlockForConnectionMultiplier(
						properties.getThreadsAllowedToBlockForConnectionMultiplier())
				.serverSelectionTimeout(properties.getServerSelectionTimeout())
				.maxWaitTime(properties.getMaxWaitTime())
				.maxConnectionIdleTime(properties.getMaxConnectionIdleTime())
				.maxConnectionLifeTime(properties.getMaxConnectionLifeTime())
				.connectTimeout(properties.getConnectTimeout()).socketTimeout(properties.getSocketTimeout())
				.socketKeepAlive(properties.isSocketKeepAlive()).sslEnabled(properties.isSslEnabled())
				.sslInvalidHostNameAllowed(properties.isSslInvalidHostNameAllowed())
				.alwaysUseMBeans(properties.isAlwaysUseMBeans())
				.heartbeatFrequency(properties.getHeartbeatFrequency())
				.minHeartbeatFrequency(properties.getMinHeartbeatFrequency())
				.heartbeatConnectTimeout(properties.getHeartbeatConnectTimeout())
				.heartbeatSocketTimeout(properties.getSocketTimeout())
				.localThreshold(properties.getLocalThreshold()).build();
		return options;
	}

	public void setEnvironment(Environment environment) {
		// 初始化配置信息到对象的映射
		Map<String, Object> map = Binder.get(environment).bind("spring.data.mongodb", Map.class).get();
		Set<String> mongoTemplateNames = new TreeSet<String>();
		Set<String> keys = map.keySet();

		for (String key : keys) {
			String mongoTemplateName = key.split("\\.")[0];
			mongoTemplateNames.add(mongoTemplateName);
		}

		for (String name : mongoTemplateNames) {
			MongoPoolProperties pro = new MongoPoolProperties();
			buildProperties((Map)map.get(name), name, pro);
			pools.add(pro);
		}
	}

	private void buildProperties(Map<String, Object> map, String name, MongoPoolProperties pro) {
		pro.setShowClass(formatBoolValue(map, PoolAttributeTag.SHOW_CLASS, true));
		pro.setMongoTemplateName(name);
		pro.setGridFsTemplateName(formatStringValue(map, PoolAttributeTag.GRID_FS_TEMPLATE_NAME, name + "GridFsTemplate"));
		pro.setHost(formatStringValue(map, PoolAttributeTag.HOST, "localhost"));
		pro.setPort(formatIntValue(map, PoolAttributeTag.PORT, 27017));
		pro.setDatabase(formatStringValue(map, PoolAttributeTag.DATABASE, "test"));
		pro.setAuthenticationDatabase(formatStringValue(map, PoolAttributeTag.AUTH_DATABASE, "admin"));
		pro.setGridFsDatabase(formatStringValue(map, PoolAttributeTag.GRIDFS_DATABASE, "test"));
		pro.setUsername(formatStringValue(map, PoolAttributeTag.USERNAME, null));
		pro.setPassword(formatChatValue(map, PoolAttributeTag.PASSWORD));
		pro.setApplicationName(formatStringValue(map, PoolAttributeTag.APPLICATIONNAME, null));
		
		pro.setMinConnectionsPerHost(formatIntValue(map, PoolAttributeTag.MIN_CONN_PERHOST, 0));
		pro.setMaxConnectionsPerHost(formatIntValue(map, PoolAttributeTag.MAX_CONN_PERHOST, 100));
		pro.setThreadsAllowedToBlockForConnectionMultiplier(formatIntValue(map, PoolAttributeTag.THREADS_ALLOWED_TO_BLOCK_FOR_CONN_MULTIPLIER, 5));
		pro.setServerSelectionTimeout(formatIntValue(map, PoolAttributeTag.SERVER_SELECTION_TIMEOUT, 1000 * 30));
		pro.setMaxWaitTime(formatIntValue(map, PoolAttributeTag.MAX_WAIT_TIME, 1000 * 60 * 2));
		pro.setMaxConnectionIdleTime(formatIntValue(map, PoolAttributeTag.MAX_CONN_IDLE_TIME, 0));
		pro.setMaxConnectionLifeTime(formatIntValue(map, PoolAttributeTag.MAX_CONN_LIFE_TIME, 0));
		pro.setConnectTimeout(formatIntValue(map, PoolAttributeTag.CONN_TIMEOUT, 1000 * 10));
		pro.setSocketTimeout(formatIntValue(map, PoolAttributeTag.SOCKET_TIMEOUT, 0));
		
		pro.setSocketKeepAlive(formatBoolValue(map, PoolAttributeTag.SOCKET_KEEP_ALIVE, false));
		pro.setSslEnabled(formatBoolValue(map, PoolAttributeTag.SSL_ENABLED, false));
		pro.setSslInvalidHostNameAllowed(formatBoolValue(map, PoolAttributeTag.SSL_INVALID_HOSTNAME_ALLOWED, false));
		pro.setAlwaysUseMBeans(formatBoolValue(map, PoolAttributeTag.ALWAYS_USE_MBEANS, false));
		
		pro.setHeartbeatFrequency(formatIntValue(map, PoolAttributeTag.HEARTBEAT_FREQUENCY, 10000));
		pro.setMinHeartbeatFrequency(formatIntValue(map, PoolAttributeTag.MIN_HEARTBEAT_FREQUENCY, 500));
		pro.setHeartbeatConnectTimeout(formatIntValue(map, PoolAttributeTag.HEARTBEAT_CONN_TIMEOUT, 20000));
		pro.setHeartbeatSocketTimeout(formatIntValue(map, PoolAttributeTag.HEARTBEAT_SOCKET_TIMEOUT, 20000));
		pro.setLocalThreshold(formatIntValue(map, PoolAttributeTag.LOCAL_THRESHOLD, 15));
	}

	private String formatStringValue(Map<String, Object> map, String key, String defaultValue) {
		if (map.containsKey(key)) {
			return map.get(key).toString();
		}
		return defaultValue;
	}
	
	private int formatIntValue(Map<String, Object> map, String key, int defaultValue) {
		if (map.containsKey(key)) {
			return Integer.valueOf(map.get(key).toString());
		}
		return defaultValue;
	}
	
	private boolean formatBoolValue(Map<String, Object> map, String key, boolean defaultValue) {
		if (map.containsKey(key)) {
			return Boolean.valueOf(map.get(key).toString());
		}
		return defaultValue;
	}
	
	private char[] formatChatValue(Map<String, Object> map, String key) {
		if (map.containsKey(key)) {
			return map.get(key).toString().toCharArray();
		}
		return new char[0];
	}
}
