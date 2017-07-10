package com.simple.server.statistics.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.config.OperationType;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.MonMsg;
import com.simple.server.statistics.Statistic;
import com.simple.server.task.ITask;
import com.simple.server.util.DateConvertHelper;

@Service("timing")
@Scope("prototype")
public class Timing extends TimerTask {

	@Autowired
	private AppConfig appConfig;
	
	public static final Long TIME_MAX_SLEEP = 15l;
	public static final Long TIME_MID_SLEEP = 5l;
	public static final Long TIME_MIN_SLEEP = 1l;
	
	public static final Long PHASE_TIME_SLEEP = 1000l;
	private static final ConcurrentHashMap<Class<ITask>, TimerTask> TIMING_REGISTER= new ConcurrentHashMap<>();
	
	Timer timer = null;
	private Date until;
	private Date when;	
	private List<Statistic> statistics = new ArrayList<>();

		
	
	
	public static Timing getAndPrepare(IContract msg, Statistic stat) throws Exception{
		
		if(!(msg instanceof MonMsg))
			return null;
		
		MonMsg mon = (MonMsg)msg;
		
		if(mon.getTaskClazz() == null)
			throw new Exception("[Timing].[newInstance]: mon.taskClazz is null ");
												
		Class<ITask> target = (Class<ITask>) Class.forName(mon.getTaskClazz());
						
		Timing oldTask = null;
		if(TIMING_REGISTER.containsKey(target)){			
			oldTask = (Timing)TIMING_REGISTER.get(target);			
			oldTask.interrupt();								
		}
		stat.addStatToTask(target);
		
		return oldTask;
	}
	
	
	
	public void addStatistic(Statistic statistic, Date when, long delay, long period, Date until) {				
		addStatToTiming(statistic);
		initTimer(when, delay, period, until);
	}

	public void addStatistic(List<Statistic> statistics, Date when, long delay, long period, Date until) {
		addStatsToTiming(statistics);
		initTimer(when, delay, period, until);
	}

	void initTimer(Date when, long delay, long period, Date until) {
		this.until = until;
		this.when = when;
		if (when != null) {
			schedule(when, period);
		} else if (delay != 0l) {
			schedule(delay, period);
		}
	}

	void schedule(Date when, long period) {
		timer = new Timer(false);
		timer.scheduleAtFixedRate(this, when, period);
	}

	void schedule(long delay, long period) {
		timer = new Timer(false);
		timer.scheduleAtFixedRate(this, delay, period);
	}

	public static Long getTimeMaxSleep() {
		return TIME_MAX_SLEEP;
	}

	public static Long getPhaseTimeSleep() {
		return PHASE_TIME_SLEEP;
	}

	public void addStatToTiming(Statistic statistic) {
		statistics.add(statistic);
	}

	public void addStatsToTiming(List<Statistic> statistics) {
		this.statistics.addAll(statistics);

	}

	public void reset() {
		for (Statistic statistic : statistics) {
			statistic.reset();
		}
	}
	
	public void clear(){
		for (Statistic statistic : statistics) {
			statistic.clear();
		}
	}

	@Override
	public void run() {
		if (until != null) {
			Date date = new Date();
			if (date.after(until)) {
				timer.cancel();
				try {
					send();
				} catch (Exception e) {
					// TODO Exception to log
					e.printStackTrace();
				}
			}
		}
		saveAll();
	}

	public void interrupt() {
		if (timer != null) {
			timer.cancel();
			try {
				send();
				clear();
			} catch (Exception e) {
				// TODO Exception to log
				e.printStackTrace();
			}
		}
	}

	private void send() throws Exception {
		for (Statistic statistic : statistics) {
			MonMsg mon = new MonMsg();
			mon.setOperationType(OperationType.MON_REP);
			mon.setUnitTotalSum(statistic.getUnitTotalSum());
			mon.setMeasurementQty(statistic.getMeasurementQty());
			mon.setWhen(DateConvertHelper.format(when));
			mon.setUntil(DateConvertHelper.format(until));
			appConfig.getSender().send(appConfig.getChannelMonRep(), mon);
		}
	}

	private void saveAll() {
		for (Statistic statistic : statistics) {
			statistic.save();
		}
	}
}
