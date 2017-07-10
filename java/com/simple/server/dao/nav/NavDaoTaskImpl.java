package com.simple.server.dao.nav;

import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;

import com.simple.server.dao.IDao;
import com.simple.server.dao.task.ADaoTask;
import com.simple.server.domain.IRec;


@Service("NavDaoTaskImpl") 
public class NavDaoTaskImpl extends ADaoTask{


	public IDao getDao() throws Exception{
		return getAppConfig().getNavDao();
	}
	
	@Override	
	public LinkedBlockingQueue<IRec> getDaoQueue() throws Exception {
		return appConfig.getQueueNAV();
	}
}
