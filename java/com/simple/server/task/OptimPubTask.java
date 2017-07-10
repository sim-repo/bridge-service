package com.simple.server.task;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.config.ContentType;
import com.simple.server.config.EndpointType;
import com.simple.server.config.ErrorType;
import com.simple.server.config.OperationType;
import com.simple.server.domain.contract.BusPubMsg;
import com.simple.server.domain.contract.ErrPubMsg;
import com.simple.server.domain.contract.HotPubMsg;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.PubErrRouting;
import com.simple.server.domain.contract.PubSuccessRouting;
import com.simple.server.domain.contract.SubRouting;
import com.simple.server.domain.contract.SuccessPubMsg;
import com.simple.server.domain.contract.UniMinMsg;
import com.simple.server.domain.contract.UniMsg;
import com.simple.server.http.HttpImpl;
import com.simple.server.http.IHttp;
import com.simple.server.lifecycle.HqlStepsType;
import com.simple.server.mediators.CommandType;
import com.simple.server.service.IService;
import com.simple.server.statistics.time.Timing;
import com.simple.server.util.DateConvertHelper;

@Service("OptimPubTask")
@Scope("prototype")
public class OptimPubTask extends ATask {

	@Autowired
	private AppConfig appConfig;

	private final static Integer MAX_NUM_ELEMENTS = 100000;
	private List<IContract> list = new ArrayList<IContract>();
	private List<ErrPubMsg> errList = new ArrayList();
	private List<SuccessPubMsg> successList = new ArrayList();
	private IHttp http = new HttpImpl();

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

		IService logService = getAppConfig().getServiceFactory().getService(EndpointType.LOG);
		List<PubErrRouting> pubErrRoutes = null;
		List<PubSuccessRouting> pubSuccessRoutes = null;
		List<SubRouting> subRoutes = null;

		if (getAppConfig().getQueuePub().drainTo(list, MAX_NUM_ELEMENTS) == 0) {
			list.add(getAppConfig().getQueuePub().take());
		}
		Thread.currentThread().sleep(Timing.getTimeMaxSleep() + 50l);

		while (basePhaser.getCurrNumPhase() != HqlStepsType.START.ordinal()) {
			Thread.currentThread().sleep(Timing.TIME_MAX_SLEEP); // critically!
			if (getAppConfig().getQueuePub().size() > 0) {
				getAppConfig().getQueuePub().drainTo(list, MAX_NUM_ELEMENTS);
			}
		}
			

		logHots(logService);
		try {
			for (IContract msg : list) {
				try {

					pubErrRoutes = getAppConfig().getRoutesPubErr(msg.getEventId(), msg.getSenderId());
					if (pubErrRoutes == null) {
						this.collectError(errList, msg, null,
								new Exception(String.format(
										"[routing PUB err] - no records found by filters %s: < %s >, %s: < %s > ",
										"[event_id]", msg.getEventId(), "[publisher_id]", msg.getSenderId())),
								null);
					}

					pubSuccessRoutes = getAppConfig().getRoutesPubSuccess(msg.getEventId(), msg.getSenderId());
					if (pubSuccessRoutes == null) {
						this.collectError(errList, msg, null,
								new Exception(String.format(
										"[routing PUB success] - no records by filters %s: < %s >, %s: < %s > ",
										"[event_id]", msg.getEventId(), "[publisher_id]", msg.getSenderId())),
								null);
					}

					subRoutes = getAppConfig().getRoutesSubs(msg.getEventId(), msg.getSenderId());
					if (subRoutes == null) {
						this.collectError(errList, msg, null,
								new Exception(String.format(
										"[routing SUB] - no records found by filters %s: < %s >, %s: < %s > ",
										"eventId", msg.getEventId(), "senderId", msg.getSenderId())),
								pubErrRoutes);
						continue;
					}

					Thread.currentThread().sleep(Timing.getTimeMaxSleep());
					for (SubRouting subRoute : subRoutes) {
						try {

							Thread.currentThread().sleep(Timing.TIME_MID_SLEEP);
							
							if ((subRoute.getSubscriberStoreClass() == null
									|| subRoute.getSubscriberStoreClass().equals(""))
									&& (subRoute.getSubscriberHandler() == null
											|| subRoute.getSubscriberHandler().equals(""))) {
								this.collectError(errList, msg, subRoute,
										new Exception(String.format(
												"[routing SUB].[id]: %s,  [subscriber_handler] && [subscriber_store_class] both are empty or null",
												subRoute.getId())),
										pubErrRoutes);
								continue;
							}

							// ORM
							if (subRoute.getSubscriberStoreClass() != null
									&& !subRoute.getSubscriberStoreClass().equals("")) {
								insertByORM(msg, subRoute);
							} else { // Http
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
										uniMinMsg.bodyTransform(subRoute.getBodyContentType());
										http.sendHttp(uniMinMsg, uniMinMsg.getUrl(), uniMinMsg.getContentType(),
												subRoute.getUseAuth());
									} else {
										newMsg = msg;
										http.sendHttp(newMsg, newMsg.getResponseURI(), newMsg.getResponseContentType(),
												subRoute.getUseAuth());
									}
								}
							}
							this.collectSuccess(successList, msg, subRoute, pubSuccessRoutes);

							BusPubMsg pubMsg = new BusPubMsg();
							pubMsg.copyFrom(msg);
							appConfig.getQueueLog().put(pubMsg);
						} catch (Exception e) {
							this.collectError(errList, msg, subRoute, new Exception(e.getMessage()), pubErrRoutes);
						}
					}
				} catch (Exception e) {
					this.collectError(errList, msg, null, new Exception(e.getMessage()), pubErrRoutes);
				}
			}
		} catch (Exception e) {

		} finally {
			sendErrors(errList);
			sendSuccess(successList);
			errList.clear();
			successList.clear();
			//this.throwToStatistic(list.size());
			list.clear();
		}
		

	}

	private void logHots(IService logService) throws Exception {
		String logDatetime = DateConvertHelper.getCurDate();
		List<IContract> hots = new ArrayList();
		for (IContract msg : list) {
			Thread.currentThread().sleep(Timing.TIME_MIN_SLEEP);
			try {
				if (msg.getPublisherId().equals(EndpointType.UNKNOWN)) {
					msg.setPublisherId(msg.getSenderId());
				}
				HotPubMsg hotPubMsg = new HotPubMsg();
				hotPubMsg.copyFrom(msg);
				hotPubMsg.setLogDatetime(logDatetime);
				hots.add(hotPubMsg);
			} catch (Exception e) {
				// TODO log exceptions
			}
		}
		logService.insertAsIs(hots);
	}

	private void insertByORM(IContract msg, SubRouting subRoute) throws Exception {
		Class<IContract> clazz = (Class<IContract>) Class.forName(subRoute.getSubscriberStoreClass());
		Constructor<IContract> ctor = clazz.getConstructor();
		IContract instance = ctor.newInstance();
		instance.setEndPointId(subRoute.getSubscriberId());
		instance.setIsDirectInsert(subRoute.getIsDirectInsert());
		instance.copyFrom(msg);
		instance.bodyTransform(subRoute.getBodyContentType());		
		//appConfig.getQueueWrite().put(instance);
	}

	private void sendStatus(IContract msg, String storeClass) {
		try {
			msg.setResponseContentType(ContentType.ApplicationJson);

			if (msg.getResponseURI() != null && !msg.getResponseURI().isEmpty()) {
				http.sendHttp(msg, msg.getResponseURI(), msg.getResponseContentType(), false);
			} else if (storeClass != null && storeClass != "") {
				IContract contract = null;
				if (msg.getClass().getName().equals(storeClass)) {
					msg.setIsDirectInsert(true);
					contract = msg;
				} else {
					Class<IContract> clazz = (Class<IContract>) Class.forName(storeClass);
					Constructor<IContract> ctor = clazz.getConstructor();
					IContract instance = ctor.newInstance();
					instance.setEndPointId(msg.getSenderId());
					instance.setIsDirectInsert(false);
					instance.setResponseContentType(msg.getResponseContentType());
					instance.copyFrom(msg);
					contract = instance;
				}
				appConfig.getQueueWrite().put(contract);
			}
			appConfig.getQueueLog().put(msg);
		} catch (Exception e) {
			try {
				ErrPubMsg newErr = new ErrPubMsg();
				String logDatetime = DateConvertHelper.getCurDate();
				setError(newErr, msg, msg.getSubscriberId(), logDatetime, e);
				appConfig.getQueueLog().put(msg);
				appConfig.getQueueLog().put(newErr);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void sendErrors(List<ErrPubMsg> errList) {
		for (ErrPubMsg err : errList) {
			try{
				Thread.currentThread().sleep(Timing.TIME_MIN_SLEEP);
				sendStatus(err, err.getStoreClass());
			}catch(InterruptedException e){
				
			}
		}
		errList = null;
	}

	private void sendSuccess(List<SuccessPubMsg> successList) {
		for (SuccessPubMsg success : successList) {
			try {
				Thread.currentThread().sleep(Timing.TIME_MIN_SLEEP);
				sendStatus(success, success.getStoreClass());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		successList = null;
	}

	private void setError(ErrPubMsg err, IContract msg, EndpointType subscriber, String logDatetime, Exception e) {
		err.setLogDatetime(logDatetime);
		err.setErrorId(ErrorType.PubTask);
		err.setOperationType(OperationType.PUB);
		err.setEventId(msg.getEventId());
		err.setJuuid(msg.getJuuid());
		err.setSenderId(msg.getSenderId());
		err.setEndPointId(msg.getSenderId());
		err.setSubscriberId(subscriber);

		if (e.getCause() != null)
			err.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
		else
			err.setDetails(String.format("%s", e.getMessage()));
	}

	private void collectError(List<ErrPubMsg> list, IContract msg, SubRouting subRouting, Exception e,
			List<PubErrRouting> routings) {
		ErrPubMsg err = null;
		String logDatetime = DateConvertHelper.getCurDate();

		if (routings == null) {
			err = new ErrPubMsg();
			setError(err, msg, subRouting.getSubscriberId(), logDatetime, e);
			list.add(err);
			return;
		}

		for (PubErrRouting routing : routings) {
			err = new ErrPubMsg();
			setError(err, msg, subRouting.getSubscriberId(), logDatetime, e);

			if (routing.getPublisherHandler() != null && routing.getPublisherHandler() != "")
				err.setResponseURI(routing.getPublisherHandler());

			if (routing.getPublisherStoreClass() != null && routing.getPublisherStoreClass() != "")
				err.setStoreClass(routing.getPublisherStoreClass());

			if (subRouting != null)
				err.setSubscriberHandler(subRouting.getSubscriberHandler());

			list.add(err);
		}
	}

	private void setSuccess(SuccessPubMsg success, IContract msg, SubRouting subRouting, String logDatetime) {
		success.setResponseContentType(ContentType.ApplicationJson);
		success.setEventId(msg.getEventId());
		success.setJuuid(msg.getJuuid());
		success.setSenderId(msg.getSenderId());
		success.setEndPointId(msg.getSenderId());
		success.setLogDatetime(logDatetime);
		if (subRouting != null) {
			success.setSubscriberHandler(subRouting.getSubscriberHandler());
			success.setSubscriberStoreClass(subRouting.getSubscriberStoreClass());
			success.setSubscriberId(subRouting.getSubscriberId());
		}
	}

	private void collectSuccess(List<SuccessPubMsg> list, IContract msg, SubRouting subRouting,
			List<PubSuccessRouting> routings) {
		SuccessPubMsg success = null;
		String logDatetime = DateConvertHelper.getCurDate();

		if (routings == null || routings.size() == 0) {
			success = new SuccessPubMsg();
			setSuccess(success, msg, subRouting, logDatetime);
			list.add(success);
			return;
		}

		for (PubSuccessRouting routing : routings) {
			success = new SuccessPubMsg();
			setSuccess(success, msg, subRouting, logDatetime);
			success.setResponseURI(routing.getPublisherHandler());
			success.setStoreClass(routing.getPublisherStoreClass());
			list.add(success);
		}
	}

}