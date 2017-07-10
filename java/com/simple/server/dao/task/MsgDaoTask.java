package com.simple.server.dao.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;

import com.simple.server.config.AppConfig;
import com.simple.server.dao.IDao;
import com.simple.server.domain.IRec;
import com.simple.server.lifecycle.HqlStepsType;
import com.simple.server.mediators.CommandType;
import com.simple.server.statistics.time.Timing;
import com.simple.server.task.ATask;

public class MsgDaoTask extends ADaoTask{
	
	@Autowired
	protected AppConfig appConfig;
	
	private final static Integer MAX_NUM_ELEMENTS = 100000;
	private List<IRec> list = new ArrayList<IRec>();
	
	@Override
	public AppConfig getAppConfig(){
		return appConfig;
	}	

	@Override
	public void update(Observable o, Object arg) {

        if(arg.getClass().equals(CommandType.class)) {
            switch ((CommandType) arg) {
                case WAKEUP_PROCESSING:
                case WAKEUP_ALL:
                    super.update(o, CommandType.WAKEUP_ALLOW);
                    break;
                case AWAIT_PROCESSING:
                case AWAIT_ALL:
                    super.update(o, CommandType.AWAIT_ALLOW);
                    break;
            }
        }      
	}
	
	
	 @Override	 	
	public void task() throws Exception {  			
	 	if (this.getDaoQueue().drainTo(list, MAX_NUM_ELEMENTS) == 0) {
			list.add(this.getDaoQueue().take());
		}
		Thread.currentThread().sleep(Timing.getTimeMaxSleep());

		while (basePhaser.getCurrNumPhase() != HqlStepsType.START.ordinal()) {
			Thread.currentThread().sleep(Timing.TIME_MAX_SLEEP); // critically!
			if (this.getDaoQueue().size() > 0) {
				this.getDaoQueue().drainTo(list, MAX_NUM_ELEMENTS);
			}
		}
		System.out.println("ADaoTask: "+list.size());
		this.getDao().insert(list);
	}



}
