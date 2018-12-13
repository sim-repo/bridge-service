package com.simple.server.task;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.JDBCException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.config.ContentType;
import com.simple.server.config.ErrorType;
import com.simple.server.config.OperationType;
import com.simple.server.domain.contract.ErrPubMsg;
import com.simple.server.domain.contract.HotPubMsg;
import com.simple.server.domain.contract.SubRouting;
import com.simple.server.domain.contract.SuccessPubMsg;
import com.simple.server.domain.contract.UniMinMsg;
import com.simple.server.domain.contract.UniMsg;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.PubErrRouting;
import com.simple.server.domain.contract.PubSuccessRouting;
import com.simple.server.domain.contract.BusPubMsg;
import com.simple.server.http.HttpImpl;
import com.simple.server.http.IHttp;
import com.simple.server.mediators.CommandType;
import com.simple.server.service.IService;
import com.simple.server.statistics.time.Timing;
import com.simple.server.util.DateConvertHelper;
import com.simple.server.util.HttpNotFoundException;

@Service("PubTask")
@Scope("prototype")
public class PubTask extends ATask {

	@Autowired
	private AppConfig appConfig;

	private final static Integer MAX_NUM_ELEMENTS = 100000;
	private List<IContract> list = new ArrayList<IContract>();

	private IHttp http = new HttpImpl();
	
	private static final Logger logger = LogManager.getLogger(PubTask.class);
	
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

	@SuppressWarnings("static-access")
	@Override
	public void task() throws Exception {
		if (getAppConfig().getQueuePub().drainTo(list, MAX_NUM_ELEMENTS) == 0) {
			list.add(getAppConfig().getQueuePub().take());
		}
		
			
		Thread.currentThread().sleep(Timing.getTimeMaxSleep());
	    getAppConfig().getQueuePub().drainTo(list, MAX_NUM_ELEMENTS);
	        
		IService service = getAppConfig().getServiceFactory().getService(appConfig.LOG_ENDPOINT_NAME);
		
		List<PubErrRouting> pubErrRoutes = null;
		List<PubSuccessRouting> pubSuccessRoutes = null;
		List<ErrPubMsg> errList = new ArrayList();
		List<SuccessPubMsg> successList = new ArrayList();
		List<SubRouting> subRoutes = null;
		String logDatetime = DateConvertHelper.getCurDate();
		IContract gmsg = null;
		SubRouting gsubRoute = null;
		try {
			for (IContract msg : list) {
				try {
					
					gmsg = msg;
					if (msg.getPublisherId()==null || msg.getPublisherId().equals("")) {
						msg.setPublisherId(msg.getSenderId());
					}
					
					logger.info(
							String.format("[PubTask] [tic-tac] - %s : %s : %s", 
								logDatetime, 						
								msg.getEventId(), 
								msg.getJuuid()									
							));	

				
					Map<String, Object> map = new HashMap();
					map = new HashMap();
					map.put("eventId", msg.getEventId());
					map.put("senderId", msg.getSenderId());
					if (msg.getSubscriberId() != null && !msg.getSubscriberId().equals(""))
						map.put("subscriberId", msg.getSubscriberId());
					subRoutes = service.<SubRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, SubRouting.class, map, 0, null);
					if (subRoutes == null || subRoutes.size() == 0) {
						logger.error(
								String.format("[PubTask] [sub routes] - no records found by filters < %s >, < %s > , "
								+ "thread id: %s , thread name:  %s", msg.getEventId(), msg.getSenderId(), Thread.currentThread().getId(), Thread.currentThread().getName()));
						
						this.collectError(errList, msg, null,
								new Exception(String.format(
										"[routing SUB] - no records found by filters %s: < %s >, %s: < %s > ",
										"eventId", msg.getEventId(), "senderId", msg.getSenderId())),
								pubErrRoutes,
								false);
						continue;
					}
					
					
					Boolean saveToHots = false;
					Boolean disableRoutePubErrSuccess = false;
					
					
					for (SubRouting route : subRoutes) {
						if (route.getSaveBodyToHots() != null && route.getSaveBodyToHots() == true) {
							saveToHots = true;
						}
						
						if (route.getDisableRoutingPubErrSuccess() != null && route.getDisableRoutingPubErrSuccess() == true) {
							disableRoutePubErrSuccess = true;
						}
					}
					
					
					if (disableRoutePubErrSuccess == false) {
						map = new HashMap();
						map.put("eventId", msg.getEventId());
						map.put("publisherId", msg.getSenderId());
						pubErrRoutes = service.<PubErrRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, PubErrRouting.class, map, 1, null);
						if (pubErrRoutes == null || pubErrRoutes.size() == 0) {
							
							logger.error(
									String.format("[PubTask] [routing PUB err] - no records found by filters < %s >, < %s > , "
									+ "thread id: %s , thread name:  %s", msg.getEventId(), msg.getSenderId(), Thread.currentThread().getId(), Thread.currentThread().getName()));
							
							
							this.collectError(errList, msg, null,
									new Exception(String.format(
											"[routing PUB err] - no records found by filters %s: < %s >, %s: < %s > ",
											"[event_id]", msg.getEventId(), "[publisher_id]", msg.getSenderId())),
									null, false);
						}
					
						pubSuccessRoutes = service.<PubSuccessRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, PubSuccessRouting.class, map, 1, null);
						if (pubSuccessRoutes == null || pubSuccessRoutes.size() == 0) {
							
							logger.error(
									String.format("[PubTask] [routing PUB success] - no records found by filters < %s >, < %s > , "
									+ "thread id: %s , thread name:  %s", msg.getEventId(), msg.getSenderId(), Thread.currentThread().getId(), Thread.currentThread().getName()));
							
							
							this.collectError(errList, msg, null,
									new Exception(String.format(
											"[routing PUB success] - no records by filters %s: < %s >, %s: < %s > ",
											"[event_id]", msg.getEventId(), "[publisher_id]", msg.getSenderId()
	
									)), null, false);
						}
					}
					
					
					try {							
							msg.setSaveBodyToHots(saveToHots);
							HotPubMsg hotPubMsg = new HotPubMsg();
							hotPubMsg.setAppConfig(appConfig);
							hotPubMsg.copyFrom(msg);
							hotPubMsg.setLogDatetime(logDatetime);												
							service.insertAsIs(appConfig.LOG_ENDPOINT_NAME, hotPubMsg);
						}catch(JDBCException e) {
							String detail = String.format("[hot pub] - %s",e.getSQLException());
							if(subRoutes != null) {
								for (SubRouting r : subRoutes) 							
										this.collectError(errList, msg, r, new Exception(detail), pubErrRoutes, false);						
							}
							else		
								this.collectError(errList, msg, null, new Exception(detail), pubErrRoutes, false);							
							continue;
					}					
				
					
					SubRouting subRoute = null;
					for (IContract route : subRoutes) {
						subRoute = (SubRouting) route;
						BusPubMsg pubMsg = new BusPubMsg();
						pubMsg.setAppConfig(appConfig);
						if (subRoute.getSubscriberHandler() != null
								&& !subRoute.getSubscriberHandler().equals("")) {
							pubMsg.setResponseURI(subRoute.getSubscriberHandler());
						}else if (subRoute.getSubscriberStoreClass() != null
									&& !subRoute.getSubscriberStoreClass().equals("")) {
							pubMsg.setResponseURI(subRoute.getSubscriberStoreClass());
						}
												
						pubMsg.copyFrom(msg);
						appConfig.getQueueLog().put(pubMsg);												
					}
				
					
					for (IContract r : subRoutes) {
						try {
							subRoute = (SubRouting) r;
							
							gsubRoute = subRoute;
							if ((subRoute.getSubscriberStoreClass() == null
									|| subRoute.getSubscriberStoreClass().equals(""))
									&& (subRoute.getSubscriberHandler() == null
											|| subRoute.getSubscriberHandler().equals(""))) {
								this.collectError(errList, msg, subRoute,
										new Exception(String.format(
												"[routing SUB].[id]: %s,  [subscriber_handler] && [subscriber_store_class] both are empty or null",
												subRoute.getId())),
										pubErrRoutes, 
										false);
								continue;
							}
							
						

							if (subRoute.getSubscriberHandler() != null
									&& !subRoute.getSubscriberHandler().equals("")) {
								msg.setResponseURI(subRoute.getSubscriberHandler());

								IContract newMsg = null;
								if (msg instanceof UniMsg) {
									UniMinMsg uniMinMsg = new UniMinMsg();

									UniMsg oldMsg = (UniMsg) msg;
									uniMinMsg.setBody(oldMsg.getBody());
									uniMinMsg.setEventId(oldMsg.getEventId());
									uniMinMsg.setJuuid(oldMsg.getJuuid());
									uniMinMsg.setDatetime(oldMsg.getLogDatetime());
									uniMinMsg.setContentType(subRoute.getResponseContentType());
									uniMinMsg.setUrl(oldMsg.getResponseURI());
									uniMinMsg.bodyTransform(subRoute.getBodyContentType(), subRoute.getBodyFldSeparator(), subRoute.getRemoveXmlAttributes(), subRoute.getUseCharsetBase64(), subRoute.getUseXmlDeclaration());									
							
									http.sendHttp(uniMinMsg, 
												  uniMinMsg.getUrl(), 
												  uniMinMsg.getContentType(),
												  subRoute.getUseAuth(),
												  uniMinMsg.getJuuid()
												  );
							
								} else {
									newMsg = msg;
									http.sendHttp(newMsg, 
												  newMsg.getResponseURI(), 
												  newMsg.getResponseContentType(),
												  subRoute.getUseAuth(),
												  newMsg.getJuuid()
												  );
								}

							} else if (subRoute.getSubscriberStoreClass() != null
									&& !subRoute.getSubscriberStoreClass().equals("")) {
								Class<IContract> clazz = (Class<IContract>) Class
										.forName(subRoute.getSubscriberStoreClass());
								Constructor<IContract> ctor = clazz.getConstructor();
								IContract instance = ctor.newInstance();
								instance.setEndPointId(subRoute.getSubscriberId());
								instance.setIsDirectInsert(subRoute.getIsDirectInsert());
								instance.copyFrom(msg);
								instance.bodyTransform(subRoute.getBodyContentType(), subRoute.getBodyFldSeparator(), subRoute.getRemoveXmlAttributes(), subRoute.getUseCharsetBase64(), subRoute.getUseXmlDeclaration());
								appConfig.getQueueWrite().put(instance);
							}

							this.collectSuccess(successList, msg, subRoute, pubSuccessRoutes);														
						} 
						catch (HttpNotFoundException e){
							this.collectError(errList, msg, subRoute, new Exception(e.getMessage()), pubErrRoutes, true);
						}
						catch (Exception e) {							
							this.collectError(errList, msg, subRoute, new Exception(e.getMessage()), pubErrRoutes, false);
						}
						catch (Error e) {
							this.collectError(errList, gmsg, gsubRoute, new Exception(e.getMessage()), pubErrRoutes, true);
						} 
					}
				} 
				catch (JDBCException e) {
					if(subRoutes != null) {
						for (SubRouting r : subRoutes) 							
								this.collectError(errList, msg, r, new Exception(e.getSQLException()), pubErrRoutes, false);						
					}
					else		
						this.collectError(errList, msg, null, new Exception(e.getSQLException()), pubErrRoutes, false);					
				}
				catch (Exception e) {
					if(subRoutes != null) {
						for (SubRouting r : subRoutes) 
							this.collectError(errList, msg, r, new Exception(e.getMessage()), pubErrRoutes, false);						
					}
					else	
						this.collectError(errList, msg, null, new Exception(e.getMessage()), pubErrRoutes, false);
				}
				catch (Error e) {				
					if(subRoutes != null) {
						for (SubRouting r : subRoutes) 
							this.collectError(errList, msg, r, new Exception(e.getMessage()), pubErrRoutes, false);						
					}
					else	
						this.collectError(errList, msg, null, new Exception(e.getMessage()), pubErrRoutes, false);
				} 
			}
			
		} catch (Error e) {				
				this.collectError(errList, gmsg, gsubRoute, new Exception(e.getMessage()), pubErrRoutes, true);
		} finally {
			sendErrors(errList);
			sendSuccess(successList);
			this.throwToStatistic(list.size());
			list.clear();
		}
	}
	
	
	private void sendErrors(List<ErrPubMsg> errList) {
		for (ErrPubMsg err : errList) {
			try {
				if (err.getResponseURI() != null && !err.getResponseURI().isEmpty()) {					
					http.sendHttp(err, 
								  err.getResponseURI(), 
								  err.getResponseContentType(), 
								  err.getUseAuth(), 
								  err.getJuuid());
				} else if (err.getStoreClass() != null && !err.getStoreClass().isEmpty()) {
					IContract contract = null;
					if (err.getClass().getName().equals(err.getStoreClass())) {
						err.setIsDirectInsert(true);						
						contract = err;
					} else {
						Class<IContract> clazz = (Class<IContract>) Class.forName(err.getStoreClass());
						Constructor<IContract> ctor = clazz.getConstructor();
						IContract instance = ctor.newInstance();
						instance.setEndPointId(err.getSenderId());
						instance.setIsDirectInsert(false);
						instance.setResponseContentType(err.getResponseContentType());
						instance.copyFrom(err);
						contract = instance;
					}
					appConfig.getQueueWrite().put(contract);
				}
				appConfig.getQueueLog().put(err);	
			} 			
			catch (HttpNotFoundException e){	// catch external errors caused by some problems at endpoint
				try {
					ErrPubMsg newErr = new ErrPubMsg();
					newErr.setErrorId(ErrorType.PubTask.toValue());
					newErr.setOperationType(OperationType.PUB);
					newErr.setResponseContentType(ContentType.ApplicationJson);
					if (e.getCause() != null)
						newErr.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
					else
						newErr.setDetails(String.format("%s", e.getMessage()));
					newErr.setEventId(err.getEventId());
					newErr.setJuuid(err.getJuuid());
					newErr.setSenderId(err.getSenderId());
					newErr.setEndPointId(err.getSenderId());
					newErr.setSubscriberId(err.getSubscriberId());
					newErr.setExternalError(true);	// external error attibute
					appConfig.getQueueLog().put(err);
					appConfig.getQueueLog().put(newErr);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			catch (Exception e) {
				try {
					ErrPubMsg newErr = new ErrPubMsg();
					newErr.setErrorId(ErrorType.PubTask.toValue());
					newErr.setOperationType(OperationType.PUB);
					newErr.setResponseContentType(ContentType.ApplicationJson);
					if (e.getCause() != null)
						newErr.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
					else
						newErr.setDetails(String.format("%s", e.getMessage()));
					newErr.setEventId(err.getEventId());
					newErr.setJuuid(err.getJuuid());
					newErr.setSenderId(err.getSenderId());
					newErr.setEndPointId(err.getSenderId());
					newErr.setSubscriberId(err.getSubscriberId());
					appConfig.getQueueLog().put(err);
					appConfig.getQueueLog().put(newErr);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		errList = null;
	}

	private void sendSuccess(List<SuccessPubMsg> successList) {
		for (SuccessPubMsg success : successList) {
			try {
				if (success.getResponseURI() != null && !success.getResponseURI().isEmpty()) {
					http.sendHttp(success, 
								  success.getResponseURI(), 
								  success.getResponseContentType(), 
								  success.getUseAuth(),
								  success.getJuuid() 
								 );
				} else if (success.getStoreClass() != null && !success.getStoreClass().isEmpty()) {
					IContract contract = null;
					if (success.getClass().getName().equals(success.getStoreClass())) {
						success.setIsDirectInsert(true);					
						contract = success;
					} else {
						Class<IContract> clazz = (Class<IContract>) Class.forName(success.getStoreClass());
						Constructor<IContract> ctor = clazz.getConstructor();
						IContract instance = ctor.newInstance();
						instance.setEndPointId(success.getSenderId());
						instance.setIsDirectInsert(false);
						instance.setResponseContentType(success.getResponseContentType());
						instance.copyFrom(success);
						contract = instance;
					}
					appConfig.getQueueWrite().put(contract);
				}
				appConfig.getQueueLog().put(success);
			} 			
			catch (HttpNotFoundException e){	// catch external errors caused by some problems at endpoint 
				try {
					ErrPubMsg newErr = new ErrPubMsg();
					newErr.setErrorId(ErrorType.PubTask.toString());
					if (e.getCause() != null)
						newErr.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
					else
						newErr.setDetails(String.format("%s", e.getMessage()));
					newErr.setEventId(success.getEventId());
					newErr.setJuuid(success.getJuuid());
					newErr.setSenderId(success.getSenderId());
					newErr.setEndPointId(success.getSenderId());
					newErr.setSubscriberId(success.getSubscriberId());
					newErr.setExternalError(true);	// external error attibute					
					appConfig.getQueueLog().put(success);
					appConfig.getQueueLog().put(newErr);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			catch (Exception e) {
				try {
					ErrPubMsg newErr = new ErrPubMsg();
					newErr.setErrorId(ErrorType.PubTask.toString());
					if (e.getCause() != null)
						newErr.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
					else
						newErr.setDetails(String.format("%s", e.getMessage()));
					newErr.setEventId(success.getEventId());
					newErr.setJuuid(success.getJuuid());
					newErr.setSenderId(success.getSenderId());
					newErr.setEndPointId(success.getSenderId());
					newErr.setSubscriberId(success.getSubscriberId());
					appConfig.getQueueLog().put(success);
					appConfig.getQueueLog().put(newErr);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		successList = null;
	}

	private void collectError(List<ErrPubMsg> list, IContract msg, SubRouting subRouting, Exception e,
			List<PubErrRouting> routings, Boolean isExternal) {
		ErrPubMsg err = null;
		String logDatetime = DateConvertHelper.getCurDate();

		if (routings == null || routings.size() == 0) {	// for internal logs 
			err = new ErrPubMsg();
			err.setErrorId(ErrorType.PubTask.toValue());
			err.setOperationType(OperationType.PUB);
			if (e.getCause() != null)
				err.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
			else
				err.setDetails(String.format("%s", e.getMessage()));
			err.setEventId(msg.getEventId());
			err.setJuuid(msg.getJuuid());
			err.setSenderId(msg.getSenderId());
			err.setEndPointId(msg.getSenderId());
			err.setExternalError(isExternal);
			//err.setPublisherId(msg.getSenderId());
			
			if (subRouting != null) {
				err.setUseAuth(subRouting.getUseAuth());	// use domain-auth
				err.setSubscriberId(subRouting.getSubscriberId());
			}
			err.setLogDatetime(logDatetime);
			list.add(err);			
			logger.info(
					String.format("[PubTask] [log pub err] - %s %s , event: %s , sender: %s , subscriber: %s, auth: %s, sub handler: %s,  thread id: %s , thread name:  %s", 
						logDatetime, 
						msg.getJuuid(), 
						msg.getEventId(), 
						msg.getSenderId(), 
						subRouting != null ? subRouting.getSubscriberId() : "xxxxxx",
						subRouting.getUseAuth(),
						subRouting.getSubscriberHandler() != null ? subRouting.getSubscriberHandler() : subRouting.getSubscriberStoreClass(),
						Thread.currentThread().getId(), 
						Thread.currentThread().getName()									
					));		

		} else {
			for (PubErrRouting routing : routings) {	// for internal logs and send feedback to endpoint-senders
				err = new ErrPubMsg();
				err.setErrorId(ErrorType.PubTask.toValue());
				err.setOperationType(OperationType.PUB);
				if (e.getCause() != null)
					err.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
				else
					err.setDetails(String.format("%s", e.getMessage()));
				err.setEventId(msg.getEventId());
				err.setJuuid(msg.getJuuid());
				err.setSenderId(msg.getSenderId());
				//err.setPublisherId(msg.getSenderId());
				err.setResponseContentType(routing.getResponseContentType());
				
				err.setEndPointId(msg.getSenderId());
				err.setExternalError(isExternal);
				
				if (routing.getPublisherHandler() != null && routing.getPublisherHandler() != "")
					err.setResponseURI(routing.getPublisherHandler());
				if (routing.getPublisherStoreClass() != null && routing.getPublisherStoreClass() != "")
					err.setStoreClass(routing.getPublisherStoreClass());

				if (subRouting != null) {
					err.setUseAuth(subRouting.getUseAuth());	// use domain-auth
					err.setSubscriberId(subRouting.getSubscriberId());
					err.setSubscriberHandler(subRouting.getSubscriberHandler());
				}
				err.setLogDatetime(logDatetime);

				list.add(err);
				
				logger.info(
						String.format("[PubTask] [log pub err] - %s %s , event: %s , sender: %s , subscriber: %s, auth: %s, sub handler: %s,  thread id: %s , thread name:  %s", 
							logDatetime, 
							msg.getJuuid(), 
							msg.getEventId(), 
							msg.getSenderId(), 
							subRouting != null ? subRouting.getSubscriberId() : "xxxxxx",
							subRouting.getUseAuth(),
							routing.getSubscriberHandler() != null ? routing.getSubscriberHandler() : routing.getSubscriberStoreClass(),
							Thread.currentThread().getId(), 
							Thread.currentThread().getName()									
						));		
			}
		}
	}

	private void collectSuccess(List<SuccessPubMsg> list, IContract msg, SubRouting subRouting,
			List<PubSuccessRouting> routings) {
		SuccessPubMsg success = null;
		String logDatetime = DateConvertHelper.getCurDate();

		if (routings == null || routings.size() == 0) {	// for internal logs 
			success = new SuccessPubMsg();
			success.setEventId(msg.getEventId());
			success.setJuuid(msg.getJuuid());
			success.setSenderId(msg.getSenderId());
			success.setEndPointId(msg.getSenderId());
			success.setOperationType(OperationType.PUB);
			
			//success.setPublisherId(msg.getSenderId());
			success.setResponseContentType(ContentType.ApplicationJson);
			if (subRouting != null) {
				success.setUseAuth(subRouting.getUseAuth());	// use domain-auth
				success.setSubscriberHandler(subRouting.getSubscriberHandler());
				success.setSubscriberStoreClass(subRouting.getSubscriberStoreClass());
				success.setSubscriberId(subRouting.getSubscriberId());
			}
			success.setLogDatetime(logDatetime);
			list.add(success);
			logger.info(
					String.format("[PubTask] [log pub success] - %s %s , event: %s , sender: %s , subscriber: %s, auth: %s, sub handler: %s,  thread id: %s , thread name:  %s", 
						logDatetime, 
						msg.getJuuid(), 
						msg.getEventId(), 
						msg.getSenderId(), 
						subRouting != null ? subRouting.getSubscriberId() : "xxxxxx",
						subRouting.getUseAuth(),
						subRouting.getSubscriberHandler() != null ? subRouting.getSubscriberHandler() : subRouting.getSubscriberStoreClass(),
						Thread.currentThread().getId(), 
						Thread.currentThread().getName()									
					));										
			
		} else {
			for (PubSuccessRouting routing : routings) {	// for internal logs and send feedback to endpoint-senders	
				success = new SuccessPubMsg();
				success.setResponseContentType(routing.getResponseContentType());
				success.setEventId(msg.getEventId());
				success.setJuuid(msg.getJuuid());
				success.setOperationType(OperationType.PUB);
				//success.setPublisherId(msg.getSenderId());
				success.setSenderId(msg.getSenderId());				
				success.setEndPointId(msg.getSenderId());
				success.setResponseURI(routing.getPublisherHandler());
				success.setStoreClass(routing.getPublisherStoreClass());
				success.setLogDatetime(logDatetime);
				if (subRouting != null) {
					success.setUseAuth(subRouting.getUseAuth());	// use domain-auth
					success.setSubscriberId(subRouting.getSubscriberId());
					success.setSubscriberHandler(subRouting.getSubscriberHandler());
					success.setSubscriberStoreClass(subRouting.getSubscriberStoreClass());
				}
				list.add(success);
				
				logger.info(
						String.format("[PubTask] [log pub success] - %s %s , event: %s , sender: %s , subscriber: %s, auth: %s, sub handler: %s,  thread id: %s , thread name:  %s", 
							logDatetime, 
							msg.getJuuid(), 
							msg.getEventId(), 
							msg.getSenderId(), 
							subRouting != null ? subRouting.getSubscriberId() : "xxxxxx",
							subRouting.getUseAuth(),
							subRouting.getSubscriberHandler() != null ? subRouting.getSubscriberHandler() : subRouting.getSubscriberStoreClass(),
							Thread.currentThread().getId(), 
							Thread.currentThread().getName()									
						));				
			}
		}
	}
}
