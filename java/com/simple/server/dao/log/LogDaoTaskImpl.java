package com.simple.server.dao.log;

import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Service;

import com.simple.server.dao.IDao;
import com.simple.server.dao.task.ADaoTask;
import com.simple.server.domain.IRec;


@Service("LogDaoTaskImpl") 
public class LogDaoTaskImpl extends ADaoTask{

	@Override
	public LinkedBlockingQueue<IRec> getDaoQueue() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDao getDao() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
