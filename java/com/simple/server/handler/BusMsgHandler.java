package com.simple.server.handler;

import org.springframework.stereotype.Service;

@Service("busMsgHandler1")
public class BusMsgHandler extends AbstractMsgHandler{
	
	
	public void handleBusJsonMsg(String json) throws Exception {			
		getAppConfig().getQueueDirty().put(json);
	}

	public void handleBusXmlMsg(String xml) throws Exception {
		// TODO Auto-generated method stub
	}
}
