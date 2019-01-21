package com.simple.server.task;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.config.ErrorType;
import com.simple.server.domain.contract.AContract;
import com.simple.server.domain.contract.ErrDefMsg;
import com.simple.server.domain.contract.ErrPubMsg;
import com.simple.server.domain.contract.ErrSubMsg;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.PubErrRouting;
import com.simple.server.domain.contract.SubErrRouting;
import com.simple.server.domain.contract.SuccessPubMsg;
import com.simple.server.domain.contract.SuccessSubMsg;
import com.simple.server.http.HttpImpl;
import com.simple.server.http.IHttp;
import com.simple.server.mediators.CommandType;
import com.simple.server.service.IService;
import com.simple.server.statistics.time.Timing;
import com.simple.server.util.DateConvertHelper;

@Service("WriteTask")
@Scope("prototype")
public class WriteTask extends ATask {
	
	@Autowired
	private AppConfig appConfig;
	
	private final static Integer MAX_NUM_ELEMENTS = 100000;
	private List<IContract> list = new ArrayList<IContract>();
	@Autowired
	private HttpImpl http;
	
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
	
	
	@Override
	public void task() throws Exception {
		if (getAppConfig() .getQueueWrite().drainTo(list, MAX_NUM_ELEMENTS) == 0) {
            list.add(getAppConfig() .getQueueWrite().take());
        }
                       
    	Thread.currentThread().sleep(Timing.getTimeMaxSleep());
                             
    	getAppConfig() .getQueueWrite().drainTo(list, MAX_NUM_ELEMENTS);
    	 	
    		
		List<ErrPubMsg> errPubList = null;
		List<SuccessPubMsg> successPubList = null;
		List<PubErrRouting> pubRoutes = null;
				
		List<ErrSubMsg> errSubList = null;
		List<SuccessSubMsg> successSubList = null;
		List<SubErrRouting> subRoutes = null;		
		List<ErrDefMsg> errDefList = null;
		
		
        for(IContract msg: list){
        	        	
        	switch(msg.getOperationType()){
	        	case PUB: {
	        				if(errPubList == null)
	        					errPubList = new ArrayList();
	        				if(successPubList == null)
	        					successPubList = new ArrayList();	        				
	        				setPub( msg, errPubList, pubRoutes );  
	        				break;	        	
	        	}
	        	case SUB: {
	        				if(errSubList == null)
	        					errSubList = new ArrayList();
	        				if(successSubList == null)
	        					successSubList = new ArrayList();	        				
	        				setSub( msg, errSubList, subRoutes ); 
	        				break;   
	        	}
	        	default: { 
			        		if(errDefList == null)
			        			errDefList = new ArrayList();
			        		break;
	        	}	        	
        	}
        	        	             
        	try{        					
				if(msg.getEndPointId() == null){
					throw new Exception(
							String.format("msg.[juuid]: < %s > - endpoint id is null  < %s > ", msg.getJuuid()));
				}
				
				IService service = getAppConfig().getServiceFactory().getService(msg.getEndPointId());
				
				if (service == null)
					throw new Exception(
							String.format("NullPointerException, check settings [routing pub success/error]. IService can't initilialized for %s", msg.getEndPointId()));
	        	if(msg.getIsDirectInsert())
	        		service.insertAsIs(msg.getEndPointId(), msg);
	        	else
	        		service.insert(msg.getEndPointId(), msg);       
        	}
        	catch(Exception e){        	
        		switch(msg.getOperationType()){
		        	case PUB: putErr(ErrPubMsg.class, msg, errPubList, e, pubRoutes ); break;	
		        	case SUB: putErr(ErrSubMsg.class, msg, errSubList, e, subRoutes ); break;   
		        	default:  putErr(ErrDefMsg.class, msg, errDefList, e, null ); break;
        		}        														
        	}
        }  
        
        if(errPubList != null && errPubList.size() > 0)
        	sendError(errPubList);
        
        if(errSubList != null && errSubList.size() > 0)
        	sendError(errSubList);
        
        if(errDefList != null && errDefList.size() > 0)
        	sendError(errDefList);
        
        errPubList = null;
        errSubList = null;
        errDefList = null;
        
        list.clear();                
	}      
	
	
	public static <E> E newInstance(Class<E> cls) throws Exception {
	    E instance = cls.newInstance(); 	    
	    return instance;
	}
	
	private void setPub(IContract msg, List<ErrPubMsg> errList, List<PubErrRouting> routes) throws Exception{
		
		IService service = getAppConfig().getServiceFactory().getService(appConfig.LOG_ENDPOINT_NAME);
		
		Map<String, Object> map = new HashMap();
		map.put("eventId", msg.getEventId());
		routes = service.<PubErrRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, PubErrRouting.class, map, 1, null);
//		if (routes == null || routes.size() == 0)
//			putErr(ErrPubMsg.class,msg, errList,
//							new Exception(String.format("[routing Pub err] - no records found by filter %s: < %s > ",
//								 		"eventId", msg.getEventId())),
//							null		
//					);
			
	}
	
	
	private void setSub(IContract msg, List<ErrSubMsg> errList, List<SubErrRouting> routes) throws Exception{
		
		IService service = getAppConfig().getServiceFactory().getService(appConfig.LOG_ENDPOINT_NAME);
		
		Map<String, Object> map = new HashMap();
		map.put("eventId", msg.getEventId());
		routes = service.<SubErrRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, SubErrRouting.class, map, 1, null);
	//	if (routes == null || routes.size() == 0) {}
//			putErr(ErrSubMsg.class, msg, errList,
//							new Exception(String.format("[routing Sub err] - no records found by filter %s: < %s > ",
//								 		"eventId", msg.getEventId())),
//							null		
//					);	
	}
	
	
	private <T extends AContract, Z extends AContract> void putErr(Class<T> clazz, IContract msg, List<T> errors, Exception e, List<Z> routes){
		
		try{
			String logDatetime = DateConvertHelper.getCurDate();
					
			if(routes == null || routes.size() == 0){
				T err = clazz.newInstance();				
				err.setErrorId(ErrorType.WriteTask.toValue());
				if(e.getCause() != null)
					err.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));		
				else 
					err.setDetails(String.format("%s", e.getMessage()));				
				err.setEventId(msg.getEventId());
				err.setJuuid(msg.getJuuid());
				err.setSenderId(msg.getSenderId());
				err.setEndPointId(msg.getSenderId());		
				err.setLogDatetime(logDatetime);
				err.setSubscriberId(msg.getSubscriberId());
				errors.add(err);	
			}else{
				
				for(Z route: routes){
					T err = clazz.newInstance();	
					err.setErrorId(ErrorType.WriteTask.toValue());
					if(e.getCause() != null)
						err.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
					else 
						err.setDetails(String.format("%s", e.getMessage()));					
					err.setEventId(msg.getEventId());
					err.setJuuid(msg.getJuuid());
					err.setSenderId(msg.getSenderId());
					err.setEndPointId(msg.getSenderId());		
					err.setLogDatetime(logDatetime);					
					err.setResponseURI(route.getPublisherHandler());
					err.setResponseContractClass(route.getPublisherStoreClass());
					err.setSubscriberId(msg.getSubscriberId());
					errors.add(err);		
				}				
			}
									
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	

	
	private <T extends IContract>void sendError(List<T> errors){
		
		for(IContract err: errors){
			err.getResponseURI();
			try {
				if (err.getResponseURI() != null && !err.getResponseURI().isEmpty()) {
					http.sendHttp(err, 
								  err.getResponseURI(), 
								  err.getResponseContentType(), 
								  false,
								  err.getJuuid()
								  );
				} else if (err.getResponseContractClass() != null && !err.getResponseContractClass().isEmpty()) {
					IContract contract = null;
					if (err.getClass().getName().equals(err.getResponseContractClass())) {
						err.setIsDirectInsert(true);
						contract = err;
					} else {
						Class<IContract> clazz = (Class<IContract>) Class.forName(err.getResponseContractClass());
						Constructor<IContract> ctor = clazz.getConstructor();
						IContract instance = ctor.newInstance();
						instance.setEndPointId(err.getSenderId());
						instance.setIsDirectInsert(false);
						instance.copyFrom(err);
						contract = instance;
					}
					appConfig.getQueueWrite().put(contract);
				}
				appConfig.getQueueLog().put(err);
			} catch (Exception e) {
				try {					
					
					Class<T> clazz = null;
					T newErr = null;
					newErr = clazz.newInstance();																	
					newErr.setErrorId(ErrorType.WriteTask.toValue());
					newErr.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
					newErr.setEventId(err.getEventId());
					newErr.setJuuid(err.getJuuid());
					newErr.setSenderId(err.getSenderId());
					newErr.setEndPointId(err.getSenderId());
					
					if(!err.getSubscriberId().equals(""))
						newErr.setSubscriberId(err.getSubscriberId());
					if(!err.getPublisherId().equals(""))
						newErr.setPublisherId(err.getPublisherId());
					
					appConfig.getQueueLog().put(err);
					appConfig.getQueueLog().put(newErr);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
}
