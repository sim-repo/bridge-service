package com.simple.server.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;

@Service("busMsgHandler1")
public class BusMsgHandler extends AbstractMsgHandler{
	
	@Autowired
	AppConfig appConfig;
	public void handleBusJsonMsg(String json) throws Exception {	
//		if (appConfig.closeAmqHandler) {
//			throw new ArithmeticException();
//		}
		getAppConfig().getQueueDirty().put(json);
	}
	public void handleBusXmlMsg(String xml) throws Exception {
		// TODO Auto-generated method stub
	}
}
