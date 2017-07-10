package com.simple.server.service.log;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.context.annotation.Scope;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import com.simple.server.dao.IDao;
import com.simple.server.domain.IRec;
import com.simple.server.domain.contract.IContract;
import com.simple.server.service.AService;
import com.simple.server.util.ObjectConverter;

@Service("logService")
@Scope("singleton")
public class LogServiceImpl extends AService implements ILogService{

	final private static String LOG_HEADER_NAME = "log"; 	
	
	@Override
	public LinkedBlockingQueue<IRec> getDaoQueue()  throws Exception {
		return getAppConfig().getQueueNAV(); //TODO change to log queue		
	}
	
	@Override
	public IDao getDao() throws Exception {
		return getAppConfig().getLogDao();
	}
	
	public void sendAsIs(MessageChannel msgChannel, IContract msg) throws Exception {								
		String json = ObjectConverter.objectToJson(msg);		
		msgChannel.send( MessageBuilder.withPayload( json ).setHeader(LOG_HEADER_NAME, msg.getClass().getSimpleName()).build() );					
	}
}
