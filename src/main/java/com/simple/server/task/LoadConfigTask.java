package com.simple.server.task;

import java.util.List;
import java.util.Observable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.domain.IRec;
import com.simple.server.domain.contract.AContract;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.PubErrRouting;
import com.simple.server.domain.contract.PubSuccessRouting;
import com.simple.server.domain.contract.RedirectRouting;
import com.simple.server.domain.contract.SubRouting;
import com.simple.server.domain.log.LogJDBCTemplate;
import com.simple.server.domain.log.LogSessionFactory;
import com.simple.server.domain.log.LogTimeoutPolicies;
import com.simple.server.mediators.CommandType;
import com.simple.server.service.IService;
import com.simple.server.util.MyLogger;



@SuppressWarnings("static-access")
@Service("LoadConfigTask")
@Scope("prototype")
public class LoadConfigTask  extends ATask {
	
	@Autowired
	private AppConfig appConfig;

    @Override
    public void update(Observable o, Object arg) {

        if(arg.getClass().equals(CommandType.class)) {
            switch ((CommandType) arg) {
                case WAKEUP_PROCESSING:
                case WAKEUP_ALL:
                    super.update(o, CommandType.WAKEUP_ALLOW);
                    break;
                case AWAIT_PROCESSING:
                case AWAIT_ALL:
                    super.update(o, CommandType.AWAIT_ALLOW);
                    break;
            }
        }      
    }
    
    
    @Override
    public void task() throws Exception {  
		
    
		List<IContract> res = null;
		RedirectRouting redirect = null;
		List<IContract> res3 = null;
		
		setDeactivateMySelfAfterTaskDone(true);
		
		Thread.currentThread().sleep(4000);		
		
		
		try {					
			MyLogger.warnStartBlock(getClass(), "SERVICE START CONFIG...");
			
			IService service = getAppConfig().getServiceFactory().getService(appConfig.LOG_ENDPOINT_NAME);
			
			//TODO logSessionFactory
			List<IRec> sessionFactories = appConfig.getLogService().readAll(appConfig.LOG_ENDPOINT_NAME, LogSessionFactory.class);
			List<IRec> jdbcTemplates = appConfig.getLogService().readAll(appConfig.LOG_ENDPOINT_NAME, LogJDBCTemplate.class);
			List<IRec> timeoutPolicies = appConfig.getLogService().readAll(appConfig.LOG_ENDPOINT_NAME, LogTimeoutPolicies.class);
			
			List<SubRouting> routes = service.<SubRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, SubRouting.class, null, 0, null);			 					
			List<PubSuccessRouting> successRoutes = service.<PubSuccessRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, PubSuccessRouting.class, null, 0, null);	
			List<PubErrRouting> errRoutes = service.<PubErrRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, PubErrRouting.class, null, 0, null);	
						
			MyLogger.warnSingleHeader(getClass(),"PROPERTY:;SIZE:;");	
			
			if(sessionFactories != null && sessionFactories.size() > 0) {
				MyLogger.warn(getClass(), "Session Factories;"+sessionFactories.size());				
				for(IRec rec: sessionFactories) {
					LogSessionFactory sf = (LogSessionFactory)rec;
					appConfig.setSessionFactories(sf.getEndpointId(), sf.getStrSessionFactory());					
				}
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: session factories is null!");
			}
			
			
			
			if(jdbcTemplates != null && jdbcTemplates.size() > 0) {							
				MyLogger.warn(getClass(), "JDBC Templates;"+jdbcTemplates.size());
				for(IRec rec: jdbcTemplates) {
					LogJDBCTemplate template = (LogJDBCTemplate)rec;
					appConfig.setJdbcTemplates(template.getEndpointId(), template.getStrJdbcTemplate());					
				}
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: jdbc templates is null!");
			}
			
			
			
			if(timeoutPolicies != null && timeoutPolicies.size() > 0) {
				MyLogger.warn(getClass(), "Timeout Policies;"+timeoutPolicies.size());				
				for(IRec rec: timeoutPolicies) {
					appConfig.timeoutPolicies = (LogTimeoutPolicies)rec;					
				}
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: timeout policies is null!");
			}
			
			
			
			appConfig.clearTmpRoutes();
			if(routes != null && routes.size() > 0) {
				MyLogger.warn(getClass(), "Sub Routes;"+routes.size());					
				for(IContract rec: routes) {
					SubRouting r = (SubRouting)rec;					
					appConfig.setRoutes(r.getSenderId()+r.getEventId(), r);					
				}				
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: routes is null!");
			}
			
			
			
			if(successRoutes != null && successRoutes.size() > 0) {
				MyLogger.warn(getClass(), "Success Routes;"+successRoutes.size());					
				for(IContract rec: successRoutes) {
					PubSuccessRouting r = (PubSuccessRouting)rec;					
					appConfig.setSuccessRoutes(r.getPublisherId()+r.getEventId(), r);					
				}							
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: success routes is null!");
			}
			
			
			
			if(errRoutes != null && errRoutes.size() > 0) {
				MyLogger.warn(getClass(), "Err Routes;"+errRoutes.size());					
				for(IContract rec: errRoutes) {
					PubErrRouting r = (PubErrRouting)rec;					
					appConfig.setErrRoutes(r.getPublisherId()+r.getEventId(), r);					
				}				
			} else {
				MyLogger.error(getClass(), "SERVICE START CONFIG: err routes is null!");
			}
			
			appConfig.setAllRoutesWhenStarting();			
			appConfig.logSizeRoutes();
			appConfig.logSizeSuccessRoutes();
			appConfig.logSizeErrRoutes();			
			MyLogger.warnEndBlock(getClass(), "SERVICE COMPLETE CONFIG...");											
		} catch (Exception e) {
			MyLogger.error(getClass(), e);
		}		      			
       
    }	
  
}
