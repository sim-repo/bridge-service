package com.simple.server.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.simple.server.config.AppConfig;
import com.simple.server.config.OperationType;
import com.simple.server.domain.contract.IContract;

@Component("queueFactory")
public class QueueFactory {
	
	@Autowired
	AppConfig appConfig;
	
	public void put(IContract msg) throws Exception{
		if(OperationType.READ.equals(msg.getOperationType())){
			appConfig.getQueueRead().put(msg);
			return;
		}
		else if(OperationType.WRITE.equals(msg.getOperationType())){
			appConfig.getQueueWrite().put(msg);
			return;
		}
		else if(OperationType.PUB.equals(msg.getOperationType())){			
			appConfig.getQueuePub().put(msg);
			return;
		}
		else if(OperationType.SUB.equals(msg.getOperationType())){			
			appConfig.getQueueSub().put(msg);
			return;
		}	
		else if(OperationType.SUB.equals(msg.getOperationType())){			
			appConfig.getQueueSub().put(msg);
			return;
		}
		else if(OperationType.MON_START.equals(msg.getOperationType())){
			appConfig.getQueueMon().put(msg);
			return;
		}
		else if(OperationType.MON_REP.equals(msg.getOperationType())){
			appConfig.getQueueMon().put(msg);
			return;
		}
		throw new Exception("bridge-service: operation type can not be null");
		//TODO: Exception to log		
	}
}
