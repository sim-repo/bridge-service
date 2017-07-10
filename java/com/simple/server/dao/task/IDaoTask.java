package com.simple.server.dao.task;

import java.util.concurrent.LinkedBlockingQueue;

import com.simple.server.config.AppConfig;
import com.simple.server.dao.IDao;
import com.simple.server.domain.IRec;

public interface IDaoTask {	

	LinkedBlockingQueue<IRec> getDaoQueue() throws Exception;
	IDao getDao() throws Exception;
}
