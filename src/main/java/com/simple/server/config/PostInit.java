package com.simple.server.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.simple.server.factory.TaskRunner;
import com.simple.server.statistics.PerfomancerStat;
import com.simple.server.statistics.time.Timing;
import com.simple.server.task.PubTask;

@Component
public class PostInit implements ApplicationListener<ContextRefreshedEvent> {
	  
	@Autowired
	TaskRunner taskRunner;	  
	
	@Autowired
	AppConfig appConfig;	  
	  
	@Value("${queueSizeDirty.int.property:10000}")
	private int queueSizeDirty;
	
	@Value("${queueSizeRead.int.property:10000}")
	private int queueSizeRead;
	  
	@Value("${queueSizeWrite.int.property:10000}")
	private int queueSizeWrite;
	  
	@Value("${queueSizePub.int.property:10000}")
	private int queueSizePub;
  
	@Value("${queueSizeSub.int.property:10000}")
	private int queueSizeSub;  
	
	@Value("${queueSizeLog.int.property:10000}")
	private int queueSizeLog;
	
	@Value("${queueSizeMon.int.property:1}")
	private int queueSizeMon;
	
	@Value("${service.bridge.name:bridge}")
	private String serviceId;
	
	
	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		
		  appConfig.initQueueDirty(queueSizeDirty);
		  appConfig.initRead(queueSizeRead);
		  appConfig.initWrite(queueSizeWrite);
		  appConfig.initPub(queueSizePub);
		  appConfig.initSub(queueSizeSub);
		  appConfig.initLog(queueSizeLog);
		  appConfig.initMon(queueSizeMon);		  
		  appConfig.initServiceId(serviceId);
		  
		  appConfig.setSessionFactories("LOG", appConfig.LOG_SESSION_FACTORY_BEAN_ID);
		  taskRunner.initProcessing();		 
 
	}
}