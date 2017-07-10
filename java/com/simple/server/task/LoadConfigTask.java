package com.simple.server.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.config.EndpointType;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.PubErrRouting;
import com.simple.server.domain.contract.PubSuccessRouting;
import com.simple.server.domain.contract.SubRouting;
import com.simple.server.mediators.CommandType;
import com.simple.server.service.IService;


@SuppressWarnings("static-access")
@Service("LoadConfigTask")
@Scope("prototype")
public class LoadConfigTask extends ATask {
	  
	@Autowired
	private AppConfig appConfig;
	
    private List<IContract> list = new ArrayList<>();
	
	
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
		    
		
		setDeactivateMySelfAfterTaskDone(true);
		
		IService service = getAppConfig().getServiceFactory().getService(EndpointType.LOG);
		
				
		Thread.currentThread().sleep(4000);		
		
		try {
			
			List<PubErrRouting> pubErrRoutes = service.<PubErrRouting>readbyCriteria(PubErrRouting.class, null, 0, null);
			if (pubErrRoutes == null || pubErrRoutes.size() == 0) {				
						new Exception("LoadConfigTask: [routing PUB err] - no record was found");											
			}
			
			for (PubErrRouting element: pubErrRoutes){
				//TODO Validate element, log errors
				appConfig.setRoutesPubErr(element.getEventId(), element.getPublisherId(), element);
			}
			
			
			List<PubSuccessRouting> pubSuccessRoutes = service.<PubSuccessRouting>readbyCriteria(PubSuccessRouting.class, null, 0, null);
			if (pubSuccessRoutes == null || pubSuccessRoutes.size() == 0) {				
						new Exception("LoadConfigTask: [routing Success err] - no record was found");	
			}
			for (PubSuccessRouting element: pubSuccessRoutes){
				//TODO Validate element, log errors
				appConfig.setRoutesPubSuccess(element.getEventId(), element.getPublisherId(), element);
			}			
			
						
			List<SubRouting> subRoutes = service.<SubRouting>readbyCriteria(SubRouting.class, null, 0, null);
			if (subRoutes == null || subRoutes.size() == 0) {				
						new Exception("LoadConfigTask: [routing sub] - - no record was found");	
			}
			for (SubRouting element: subRoutes){
				//TODO Validate element, log errors
				appConfig.setRoutesSubs(element.getEventId(), element.getSenderId(), element);
			}										
		} catch (Exception e) {
			//TODO log errors
			e.printStackTrace();
		}		      			   
        list.clear();
    }  	
}