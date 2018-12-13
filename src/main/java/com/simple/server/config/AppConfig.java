package com.simple.server.config;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import com.simple.server.dao.endpoint.IEndpointDao;
import com.simple.server.dao.endpoint.EndpointDaoImpl;
import com.simple.server.dao.log.ILogDao;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.log.LogTimeoutPolicies;
import com.simple.server.factory.ContractRecFactory;
import com.simple.server.factory.PhaserRunner;
import com.simple.server.factory.QueueFactory;
import com.simple.server.factory.ServiceFactory;
import com.simple.server.mediators.Mediator;
import com.simple.server.service.IService;
import com.simple.server.service.sender.Sender;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.context.ApplicationContext;

@Service("appConfig")
@Scope("singleton")
public class AppConfig {
	
	public final static String ACC = "jservice";
	public final static String PSW = "j123Service";
	public final static String DOMEN = "SIMPLE";
	public final static String WORKSTATION = "MSK10WEBSVC2";
	
	public final static String DATEFORMAT = "dd.MM.yyyy HH:mm:ss";
	
	public final static String ROLE_ID = "BRIDGE-SERVICE"; 
	public final static String LOG_HEADER_NAME = "clazz";
	
	public final static String LOG_SESSION_FACTORY_BEAN_ID = "logSessionFactory";
	public final static String LOG_ENDPOINT_NAME = "LOG";
	
	
	private static Map<String, SessionFactory> sessionFactories = new HashMap();
	private static Map<String, JdbcTemplate> jdbcTemplates = new HashMap();
	ConcurrentHashMap<String, String> sessionFactoriesString= new ConcurrentHashMap<String, String>();
	
	public LogTimeoutPolicies timeoutPolicies = new LogTimeoutPolicies();
	
	private String serviceId;
	
	@Autowired
    private MessageChannel channelBusLog;	
	@Autowired
	private MessageChannel channelSrvLog;	
	@Autowired
	private MessageChannel channelMonRep;
	
	
	private LinkedBlockingQueue<String> queueDirty;
	private LinkedBlockingQueue<IContract> queueRead;
	private LinkedBlockingQueue<IContract> queueWrite;    
	private LinkedBlockingQueue<IContract> queuePub;      	
	private LinkedBlockingQueue<IContract> queueSub;
	private LinkedBlockingQueue<IContract> queueLog;
	private LinkedBlockingQueue<IContract> queueMon;
	
	private static final Logger logger = LogManager.getLogger(AppConfig.class);
	
	private Mediator mediator = new Mediator();   
	
	
	@Autowired
	private ApplicationContext ctx;
	
			
	@Autowired
	private JdbcTemplate logJdbcTemplate;
	
	@Autowired
	private SessionFactory logSessionFactory;
		

	@Autowired
	private ContractRecFactory contractRecFactory;
	
	@Autowired
	private ServiceFactory serviceFactory;
	
	@Autowired
	private QueueFactory queueFactory;
	
	
	@Autowired
	private ILogDao logDao;	
	
	@Autowired
	private EndpointDaoImpl endpointDao;
	
	@Autowired
	private IService endpointService;
	
	@Autowired
	private IService logService;
	
	
	@Autowired
	private PhaserRunner phaserRunner;
		
	@Autowired 
	private Sender sender;
		
	

	
	
	public Sender getSender() {
		return sender;
	}

	
	public JdbcTemplate getLogJdbcTemplate() {
		return logJdbcTemplate;
	}

	public SessionFactory getLogSessionFactory() {
		return logSessionFactory;
	}

	public ILogDao getLogDao() {
		return logDao;
	}

	public IEndpointDao getEndpointDao() {
		return endpointDao;
	}
	

	public ContractRecFactory getContractRecFactory() {
		return contractRecFactory;
	}

	public ServiceFactory getServiceFactory() {
		return serviceFactory;
	}
		
	
	public void createDynamicBean() {
        AutowireCapableBeanFactory factory = null;        
        factory = ctx.getAutowireCapableBeanFactory();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) factory;
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DriverManagerDataSource.class);
        beanDefinition.setAutowireCandidate(true);
        registry.registerBeanDefinition("mysDS", beanDefinition);
        factory.autowireBeanProperties(this,
                AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
        
        
        DriverManagerDataSource ds = (DriverManagerDataSource)ctx.getBean("mysDS");
        ds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        ds.setUrl("jdbc:sqlserver://MSK10NAV52\\MIRROR;databaseName=SimpleERPCopyNAV");
        ds.setUsername("jservice");
        ds.setPassword("Larina123");
        
        
        beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(LocalSessionFactoryBean.class);
        beanDefinition.setAutowireCandidate(true);
        registry.registerBeanDefinition("mySessionFactory", beanDefinition);
        factory.autowireBeanProperties(this,
                AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
                                               
    }
	
	public String getDefaultEndpointByGroupId(String endpointGroupId) {
		if(sessionFactoriesString.containsKey(endpointGroupId))		
			return  sessionFactoriesString.get(endpointGroupId);
		return null;
	}
	
	public void setGroupSessionFactories(String endpointGroupId, String defaultEndpointId) {
		sessionFactoriesString.put(endpointGroupId, defaultEndpointId);
	}
	
	public SessionFactory getSessionFactoryByEndpointId(String endpointId) {
		if(sessionFactories.containsKey(endpointId))		
			return  sessionFactories.get(endpointId);
		return null;
	}

	public void setSessionFactories(String endpointId, String sessionFactoryBeanId) {
		SessionFactory sf = (SessionFactory)ctx.getBean(sessionFactoryBeanId);		
		sessionFactories.put(endpointId, sf);
	}

	public JdbcTemplate getJdbcTemplateByKey(String endpointId) {
		if(jdbcTemplates.containsKey(endpointId)) {		
			return  jdbcTemplates.get(endpointId);
		}
		return null;
	}

	public void setJdbcTemplates(String endpointId, String jdbcTemplateBeanId) {
		JdbcTemplate template = (JdbcTemplate)ctx.getBean(jdbcTemplateBeanId);		
		jdbcTemplates.put(endpointId, template);
	}
	
	
	public ApplicationContext getApplicationContext() {
		return ctx;
	}
	
	public IService getEndpointService() {
		return endpointService;
	}

	public IService getLogService() {
		return logService;
	}

	public Mediator getMediator() {
		return mediator;
	}
	
	public MessageChannel getChannelBusLog() {
		return channelBusLog;
	}
	
	public MessageChannel getChannelSrvLog() {
		return channelSrvLog;
	}
	
	public MessageChannel getChannelMonRep() {
		return channelMonRep;
	}

	public LinkedBlockingQueue<String> getQueueDirty() {
		return queueDirty;
	}

	public LinkedBlockingQueue<IContract> getQueueRead() {
		return queueRead;
	}

	public LinkedBlockingQueue<IContract> getQueueWrite() {
		return queueWrite;
	}	
	
	public LinkedBlockingQueue<IContract> getQueuePub() {
		return queuePub;
	}		

	public LinkedBlockingQueue<IContract> getQueueSub() {
		return queueSub;
	}	

	public LinkedBlockingQueue<IContract> getQueueLog() {
		return queueLog;
	}		

	public LinkedBlockingQueue<IContract> getQueueMon() {
		return queueMon;
	}

	public QueueFactory getQueueFactory() {
		return queueFactory;
	}

	public PhaserRunner getPhaserRunner() {
		return phaserRunner;
	}

	public static Logger getLogger() {
		return logger;
	}		

	public void initQueueDirty(int size){
		this.queueDirty = new LinkedBlockingQueue<>(size);
	}
	
	public void initRead(int size){
		this.queueRead = new LinkedBlockingQueue<>(size);
	}
	
	public void initWrite(int size){
		this.queueWrite = new LinkedBlockingQueue<>(size);
	}
	
	public void initPub(int size){
		this.queuePub = new LinkedBlockingQueue<>(size);
	}
	
	public void initSub(int size){
		this.queueSub = new LinkedBlockingQueue<>(size);
	}
	
	public void initLog(int size){
		this.queueLog = new LinkedBlockingQueue<>(size);
	}
	
	public void initMon(int size){
		this.queueMon= new LinkedBlockingQueue<>(size);
	}
	
	public void initServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
	public String getServiceId() {
		return this.serviceId;
	}
	
}