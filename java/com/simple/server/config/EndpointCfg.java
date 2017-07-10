package com.simple.server.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.simple.server.domain.IRec;

public class EndpointCfg {
	
	private Map<String, SessionFactory> sessionFactoryMap = Collections.synchronizedMap(new HashMap());
	private Map<String, LinkedBlockingQueue<IRec>> endpointQueueMap = Collections.synchronizedMap(new HashMap());

	public Map<String, SessionFactory> getSessionFactoryMap() {
		return sessionFactoryMap;
	}

	public void setSessionFactoryMap(Map<String, SessionFactory> sessionFactoryMap) {
		this.sessionFactoryMap = sessionFactoryMap;
	}


	public Session getCurrentSession(String endpointId) throws Exception {
		if(sessionFactoryMap.containsKey(endpointId)){					
			return this.sessionFactoryMap.get(endpointId).getCurrentSession();
		}
		return null;
	}
		
	public Map<String, LinkedBlockingQueue<IRec>> getEndpointQueueMap() {
		return endpointQueueMap;
	}

	public void setEndpointQueueMap(Map<String, LinkedBlockingQueue<IRec>> endpointQueueMap) {
		this.endpointQueueMap = endpointQueueMap;
	}

	public LinkedBlockingQueue<IRec> getQueue(String endpointId){
		if(this.endpointQueueMap.containsKey(endpointId)){
			return this.endpointQueueMap.get(endpointId);
		}
		return null;
	}
}
