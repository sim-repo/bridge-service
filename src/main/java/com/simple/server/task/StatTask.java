package com.simple.server.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.MonMsg;
import com.simple.server.mediators.CommandType;
import com.simple.server.statistics.PerfomancerStat;
import com.simple.server.statistics.Statistic;
import com.simple.server.statistics.time.Timing;
import com.simple.server.util.DateConvertHelper;

@Service("StatTask")
@Scope("prototype")
public class StatTask extends ATask {

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private ApplicationContext ctx;

	private List<IContract> list = new ArrayList();

	private final static Integer MAX_NUM_ELEMENTS = 1;

	private Class<ITask> taskClazz = null;

	@Override
	public void update(Observable o, Object arg) {
		if (arg != null && arg.getClass() == CommandType.class) {
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

		if (getAppConfig().getQueueMon().drainTo(list, MAX_NUM_ELEMENTS) == 0) {
			list.add(getAppConfig().getQueueMon().take());
		}
		
		for (IContract msg : list) {
			if (msg instanceof MonMsg) {
				
				MonMsg mon = (MonMsg) msg;				
				
				if(mon.getBeanStatClazz()== null)
					throw new Exception("[Timing].[newInstance]: mon.statClazz is null ");
				
				Statistic stat = (Statistic) ctx.getBean(mon.getBeanStatClazz());		
				
				schedule(stat, mon);
			}
		}
		list.clear();
	}

	
	void schedule(Statistic stat, MonMsg mon ) throws Exception {				
		
		Timing newTask = null;
		Timing oldTask = null;
		
		Date when = null;
		Date until = null;				
				
		if (mon.getWhen() != null)
			when = DateConvertHelper.parse(mon.getWhen());
		
		if (mon.getUntil() != null)
			until = DateConvertHelper.parse(mon.getUntil());

		oldTask = Timing.getAndPrepare(mon, stat);
		if(oldTask == null)
			newTask = (Timing) ctx.getBean("timing");
				
		newTask.addStatistic(stat, when, mon.getDelay(), mon.getPeriod(), until);
	}

}
