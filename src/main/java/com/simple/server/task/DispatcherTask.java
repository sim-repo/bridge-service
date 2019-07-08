package com.simple.server.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.server.domain.contract.IContract;
import com.simple.server.mediators.CommandType;
import com.simple.server.statistics.time.Timing;
import com.simple.server.util.MyLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("DispatcherTask")
@Scope("prototype")
public class DispatcherTask extends ATask {

    private final static Integer MAX_NUM_ELEMENTS = 100000;
    private List<String> list = new ArrayList<String>();
    private ObjectMapper mapper = new ObjectMapper();    
    
    @Override
    public void update(Observable o, Object arg) {
        if(arg != null && arg.getClass() == CommandType.class) {
            switch ((CommandType) arg) {
                case WAKEUP_CONSUMER:
                case WAKEUP_ALL:
                    arg = CommandType.WAKEUP_ALLOW;
                    super.update(o, arg);
                    break;
                case AWAIT_CONSUMER:
                case AWAIT_ALL:
                    arg = CommandType.AWAIT_ALLOW;
                    super.update(o, arg);
                    break;
            } 
        }
    }


    @SuppressWarnings("static-access")
	@Override
    public void task() throws Exception {
        if (getAppConfig().getQueueDirty().drainTo(list, MAX_NUM_ELEMENTS) == 0) {
            list.add(getAppConfig().getQueueDirty().take());
        }      
        
        Thread.currentThread().sleep(Timing.getTimeMaxSleep());
        getAppConfig().getQueueDirty().drainTo(list, MAX_NUM_ELEMENTS);
        
                
        try{
	        for(String json: list) {  	        	
	        	IContract msg = mapper.readValue(json, IContract.class);
	        	getAppConfig().getQueueFactory().put(msg);
	        	Thread.currentThread().sleep(Timing.getTimeMaxSleep());	  			        		 
	        }
        }catch(Exception e){
        	MyLogger.error(getClass(), e);         	
        }                     
        list.clear();
    }

}