package com.simple.server.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.dao.nav.NavDaoTaskImpl;
import com.simple.server.lifecycle.BasePhaser;
import com.simple.server.lifecycle.HqlStepsType;
import com.simple.server.mediators.CommandType;
import com.simple.server.mediators.Mediator;
import com.simple.server.mediators.ParameterType;
import com.simple.server.task.ATask;
import com.simple.server.task.DispatcherTask;
import com.simple.server.task.ReadTask;
import com.simple.server.task.StatTask;
import com.simple.server.task.SubTask;
import com.simple.server.task.WriteTask;
import com.simple.server.task.ITask;
import com.simple.server.task.LoadConfigTask;
import com.simple.server.task.LogSenderTask;
import com.simple.server.task.OptimPubTask;

@Service("taskRunner")
@Scope("singleton")
public class TaskRunner  {

	@Autowired
	private ApplicationContext appContext;
	
	@Autowired
	private AppConfig appConfig;
		
    CopyOnWriteArrayList<ExecutorService> executors = new CopyOnWriteArrayList<>();
    ConcurrentHashMap<Object, List<ITask>> tasks = new ConcurrentHashMap<>();
    CopyOnWriteArrayList<ITask> ltasks = new CopyOnWriteArrayList<ITask>();
    ConcurrentHashMap<Class<ATask>, Integer> classToRun = new ConcurrentHashMap<>();
    
    
    
	public <T extends ATask> List<T> newRunTask(Mediator mediator, Class<T> clazz, int num) throws Exception{
    	 ReentrantLock lock = new ReentrantLock();
         Condition condition = lock.newCondition();
         ExecutorService executor = Executors.newFixedThreadPool(num);
         List<T> list = new ArrayList<>();
         for (int i =0; i < num; i++) {
        	 T t =  (T)appContext.getBean(clazz.getSimpleName());
        	 t.setExecutor(executor);
        	 t.setReentrantLock(lock);
        	 t.setCondition(condition);                	
        	 mediator.addObserver(t);
        	 t.addObserver(mediator);
        	 executor.submit(t);
        	 addTask(t.getClass(), t);
        	 list.add(t);
         }         
         executors.add(executor);         
         return list;
    }
   
    private void addTask(Object o, ITask t){
        List<ITask> list = null;
        if(tasks.containsKey(o)){
            list = tasks.get(o);
        }else{
            list = new ArrayList<>();
        }
        list.add(t);
        tasks.put(o,list);
        ltasks.add(t);
    }

   
    public List<ITask> getTasksByClass(Class clazz) {
        if(tasks.containsKey(clazz))
            return tasks.get(clazz);
        return null;
    }

    public List<ITask> getAllTasks(){    	
    	return ltasks;
    }
    
    public void setParam(ParameterType parameterType, Class clazz, Object object){
        if(tasks.containsKey(clazz)){
            List<ITask> taskList = tasks.get(clazz);
            for(ITask task: taskList){
                switch (parameterType) {
                    case ON_BEFORE_START_TASK: task.setOnBeforeStartTask((CommandType)object); break;
                    case ON_AFTER_TASK_DONE: task.setOnAfterTaskDone((CommandType)object); break;
                    case DEACTIVATE_AFTER_TASK_DONE: task.setDeactivateMySelfAfterTaskDone((Boolean)object); break;
                    case ON_RUNTIME_ERROR: task.setOnRuntimeError((CommandType)object); break;
                }
            }
        }
    }
    
  
    public void initProcessing() {        
        	try {		        		 
        		newRunTask(appConfig.getMediator(), LoadConfigTask.class, 1);
        		newRunTask(appConfig.getMediator(), DispatcherTask.class, 1);
        		newRunTask(appConfig.getMediator(), ReadTask.class, 1);
        		newRunTask(appConfig.getMediator(), WriteTask.class, 1);
        		//newRunTask(appConfig.getMediator(), PubTask.class, 4);
        		newRunTask(appConfig.getMediator(), OptimPubTask.class, 1);
        		newRunTask(appConfig.getMediator(), SubTask.class, 1);
        		newRunTask(appConfig.getMediator(), LogSenderTask.class, 1);
        		newRunTask(appConfig.getMediator(), StatTask.class, 1);        	            		        	        		
        		newRunTask(appConfig.getMediator(), NavDaoTaskImpl.class, 1);
        		
        		
        		for(Map.Entry<Class<ATask>, Integer> pair: classToRun.entrySet()){
        			Class<ATask> clazz = pair.getKey();
        			int num = pair.getValue();
        			newRunTask(appConfig.getMediator(), clazz, num);
        		}
        		
        		BasePhaser hqlPhaser = appConfig.getPhaserRunner().newRunPhaser(appConfig.getMediator(), BasePhaser.class, HqlStepsType.FINISH.ordinal());                        		 	
            	appConfig.getMediator().wakeupAll();                     
            	
        	} catch (Exception e) {
    			e.printStackTrace();
    		}           		
    }
    
    
    public void addClassToRun(Class<ATask> clazz, int num){
    	classToRun.put(clazz, num);
    }
    
}
