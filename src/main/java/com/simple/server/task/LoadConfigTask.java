package com.simple.server.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.domain.IRec;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.RedirectRouting;
import com.simple.server.domain.log.LogJDBCTemplate;
import com.simple.server.domain.log.LogSessionFactory;
import com.simple.server.mediators.CommandType;



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
		
		setDeactivateMySelfAfterTaskDone(true);
		
		Thread.currentThread().sleep(4000);		
			
		
		try {
		
			//TODO logSessionFactory
			List<IRec> sessionFactories = appConfig.getLogService().readAll(appConfig.LOG_ENDPOINT_NAME, LogSessionFactory.class);
			List<IRec> jdbcTemplates = appConfig.getLogService().readAll(appConfig.LOG_ENDPOINT_NAME, LogJDBCTemplate.class);
			
						
			
			if(sessionFactories != null) {
				for(IRec rec: sessionFactories) {
					LogSessionFactory sf = (LogSessionFactory)rec;
					appConfig.setSessionFactories(sf.getEndpointId(), sf.getStrSessionFactory());					
				}
			}
			
			if(jdbcTemplates != null) {
				for(IRec rec: jdbcTemplates) {
					LogJDBCTemplate template = (LogJDBCTemplate)rec;
					appConfig.setJdbcTemplates(template.getEndpointId(), template.getStrJdbcTemplate());					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		      			
       
    }
   	
  
}
