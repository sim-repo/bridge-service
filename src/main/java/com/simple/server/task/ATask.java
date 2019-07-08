package com.simple.server.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.simple.server.config.AppConfig;
import com.simple.server.domain.contract.IContract;
import com.simple.server.lifecycle.BasePhaser;
import com.simple.server.lifecycle.Deactivator;
import com.simple.server.mediators.CommandType;
import com.simple.server.mediators.Mediator;
import com.simple.server.mediators.Subscriber;
import com.simple.server.service.IService;
import com.simple.server.statistics.Statistic;
import com.simple.server.statistics.time.Timing;
import com.simple.server.task.state.State;


public abstract class ATask extends Observable implements ITask, Callable, Observer {
    
	private ExecutorService executor;
    protected ReentrantLock lock;
    protected Condition wakeup;
    private Boolean isActive = false;
    private Statistic statistic;
    private static List<Deactivator> deactivators = new ArrayList<Deactivator>();
    private Boolean deactivateAfterTaskDone = false;
    protected Subscriber subscriber;
    protected CommandType onBeforeStartTask;
    protected CommandType onAfterTaskDone;
    protected CommandType onRuntimeError;
    protected BasePhaser basePhaser;
    protected IService baseService;
    
    
	@Autowired
	private AppConfig appConfig;    	
	
    public AppConfig getAppConfig() {
		return appConfig;
	}

	@Override
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;		
	}

	@Override
	public void setReentrantLock(ReentrantLock lock) {
		this.lock = lock;		
	}

	@Override
	public void setCondition(Condition wakeup) {
		this.wakeup = wakeup;
	}

	public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean flag) {
        isActive = flag;
    }
    
    @Override
    public void setStatistic(Statistic statistic) {
        this.statistic = statistic;
    }        
    
    public Statistic getStatistic() {
		return statistic;
	}

	public void throwToStatistic(int qty){
        if (statistic != null)
            statistic.setCurrUnitSum(qty);
    }

    public void setPhase(BasePhaser basePhaser) {
        this.basePhaser = basePhaser;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public CommandType getOnBeforeStartTask() {
        return onBeforeStartTask;
    }

    public void setOnBeforeStartTask(CommandType onBeforeStartTask) {
        this.onBeforeStartTask = onBeforeStartTask;
    }

    public CommandType getOnAfterTaskDone() {
        return onAfterTaskDone;
    }

    public void setOnAfterTaskDone(CommandType onAfterTaskDone) {
        this.onAfterTaskDone = onAfterTaskDone;
    }

    public CommandType getOnRuntimeError() {
        return onRuntimeError;
    }

    public void setOnRuntimeError(CommandType onRuntimeError) {
        this.onRuntimeError = onRuntimeError;
    }        

	public IService getBaseService() {
		return baseService;
	}

	public void setBaseService(IService baseService) {
		this.baseService = baseService;
	}

	@Override
    public void update(Observable o, Object arg){
        try {
            if (!(o instanceof Mediator)) {
                throw new Exception("Observable object must be as instance of Dispatcher");
            }

            if(arg != null){
                if(!(arg instanceof CommandType)){
                    throw new Exception("arg must be as CommandType enum");
                }
            }

            lock.lock();
            if (arg!=null) {
                switch ((CommandType)arg) {
                    case WAKEUP_ALLOW:
                    case WAKEUP_ALL: {
                            setIsActive(true);
                            wakeup.signal();
                        break;
                    }
                    case AWAIT_ALL:
                    case AWAIT_ALLOW: {
                        setIsActive(false);
                        wakeup.signal();
                        break;
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    

    public static void setDeactivators(List<Deactivator> deactivators) {
        ATask.deactivators = deactivators;
        for(Deactivator deactivator: deactivators){
            deactivator.register();
        }
    }

    public void setDeactivateMySelfAfterTaskDone(Boolean deactivateAfterTaskDone) {
        this.deactivateAfterTaskDone = deactivateAfterTaskDone;
    }

    @Override
    public Object call() throws Exception {
        
        while(!executor.isShutdown()){
            try {            	
                lock.lock();                
                while (!getIsActive()){            
                    wakeup.await();     
                }
            }catch (Exception e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }

            try{
                Thread.currentThread().sleep(Timing.getTimeMaxSleep());

                if(getOnBeforeStartTask() != null){
                    setChanged();
                    notifyObservers(getOnBeforeStartTask());
                }

                task();

                if(getOnAfterTaskDone() != null){
                    setChanged();
                    notifyObservers(getOnAfterTaskDone());
                }

                if(deactivateAfterTaskDone){
                    setIsActive(false);
                }
            }catch (Exception e){
                setChanged();
                notifyObservers(getOnRuntimeError());

                setChanged();
                State state = new State();
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                state.setMessage(errors.toString());
                notifyObservers(state);
                //TODO Exception to log
            }
        }

        return null;
    }

    public abstract void task() throws Exception;
}
