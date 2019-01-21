package com.simple.server.task;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.server.config.AppConfig;
import com.simple.server.config.ErrorType;
import com.simple.server.config.MiscType;
import com.simple.server.config.OperationType;
import com.simple.server.domain.contract.ErrSubMsg;
import com.simple.server.domain.contract.HotPubMsg;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.RoutingPubConfirmMsg;
import com.simple.server.domain.contract.SubErrRouting;
import com.simple.server.domain.contract.SuccessSubMsg;
import com.simple.server.domain.contract.BusPubMsg;
import com.simple.server.http.HttpImpl;
import com.simple.server.http.IHttp;
import com.simple.server.lifecycle.HqlStepsType;
import com.simple.server.mediators.CommandType;
import com.simple.server.service.IService;
import com.simple.server.statistics.time.Timing;
import com.simple.server.util.DateConvertHelper;

@Service("SubTask")
@Scope("prototype")
public class SubTask extends ATask {

	private final static Integer MAX_NUM_ELEMENTS = 100000;
	private static Map<String, MiscType> orderMap = new HashMap();

	@Autowired
	private AppConfig appConfig;
	
	@Autowired
	private HttpImpl http;
	
	private List<IContract> list = new ArrayList<IContract>();
	private ObjectMapper mapper = new ObjectMapper();

	static {
		orderMap.put("id", MiscType.desc);
	}

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

		if (getAppConfig().getQueueSub().drainTo(list, MAX_NUM_ELEMENTS) == 0) {
			list.add(getAppConfig().getQueueSub().take());
		}
		Thread.currentThread().sleep(Timing.getTimeMaxSleep());
		while (basePhaser.getCurrNumPhase() != HqlStepsType.START.ordinal()) {
			if (getAppConfig().getQueueSub().size() > 0)
				getAppConfig().getQueueSub().drainTo(list, MAX_NUM_ELEMENTS);
		}

		List<ErrSubMsg> errList = new ArrayList();
		List<SuccessSubMsg> successList = new ArrayList();
		List<SubErrRouting> subErrRoutesList = null;

		SubErrRouting subErrRouting = null;
		BusPubMsg pubLog = null;
		Integer lock = 0;
		for (IContract msg : list) {

			try {
				Map<String, Object> map = null;
				String logDatetime = DateConvertHelper.getCurDate();

				IService service = getAppConfig().getServiceFactory().getService(appConfig.LOG_ENDPOINT_NAME);
				map = new HashMap();
				map.put("subscriberId", msg.getSenderId());
				map.put("eventId", msg.getEventId());
				subErrRoutesList = service.<SubErrRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, SubErrRouting.class, map, 1, orderMap);
				map = null;
				if (subErrRoutesList == null || subErrRoutesList.size() == 0) {
					this.collectError(errList, msg, null,
							new Exception(String.format(
									"[routing sub err] - no records found by filters %s: < %s >, %s: <%s> ",
									"[sender_id]", msg.getSenderId(), "[event_id]", msg.getEventId())));
				} else{
					//TODO: маршрутов может быть много - реализовать, предусмотреть проверку enabled
					subErrRouting = subErrRoutesList.get(0);
				}
				
				

				map = new HashMap();
				map.put("juuid", msg.getJuuid());
				List<HotPubMsg> hotPubList = service.<HotPubMsg>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, HotPubMsg.class, map, 1, orderMap);
				if (hotPubList == null || hotPubList.size() == 0) {
					this.collectError(errList, msg, subErrRouting, new Exception(String
							.format("[hot pub] - no records found by filter %s: < %s >", "[guid]", msg.getJuuid())));
					continue;
				}

				map = new HashMap();
				map.put("eventId", msg.getEventId());
				List<RoutingPubConfirmMsg> confList = service
						.<RoutingPubConfirmMsg>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, RoutingPubConfirmMsg.class, map, 1, orderMap);
				if (confList == null || confList.size() == 0) {
					this.collectError(errList, msg, subErrRouting,
							new Exception(
									String.format("[routing pub confirmation] - no records found by filters %s: < %s >",
											"[event_id]", msg.getEventId())));
					continue;
				}

				RoutingPubConfirmMsg confirm = confList.get(0);

				
				if ((confirm.getPublisherHandler() == null || confirm.getPublisherHandler().equals(""))
						&& (confirm.getPublisherStoreClass() == null || confirm.getPublisherStoreClass().equals(""))) {
					this.collectError(errList, msg, subErrRouting,
							new Exception(String.format(
									"[routing pub confirmation].[id]: %s,  [subscriber_handler] && [subscriber_store_class] both are empty or null",
									confirm.getId())));
				}

				msg.setPublisherId(confirm.getEndPointId());
				msg.setSubscriberId(msg.getSenderId());

				try {
					if (confirm.getPublisherHandler() != null && !confirm.getPublisherHandler().equals("")) {
						msg.setLogDatetime(logDatetime);
						msg.setOperationType(OperationType.SUB);
						msg.setResponseContentType(confirm.getResponseContentType());
						msg.setResponseURI(confirm.getPublisherHandler());
						http.sendHttp(msg, 
									  msg.getResponseURI(), 
									  msg.getResponseContentType(), 
									  confirm.getUseAuth(),
									  msg.getJuuid()
									  );
					} else if (confirm.getPublisherStoreClass() != null
							&& !confirm.getPublisherStoreClass().equals("")) {
						Class<IContract> clazz = (Class<IContract>) Class.forName(confirm.getPublisherStoreClass());
						Constructor<IContract> ctor = clazz.getConstructor();
						IContract instance = ctor.newInstance();
						instance.setEndPointId(confirm.getEndPointId());
						instance.setIsDirectInsert(confirm.getIsDirectInsert());
						instance.setLogDatetime(logDatetime);
						instance.setOperationType(OperationType.SUB);
						instance.copyFrom(msg);
						appConfig.getQueueWrite().put(instance);
					}
					this.collectSuccess(successList, msg);
				} catch (Exception e) {
					this.collectError(errList, msg, subErrRouting, new Exception(e.getMessage()));
				}
			} catch (Exception e) {
				this.collectError(errList, msg, subErrRouting, new Exception(e.getMessage()));
			}
		}

		sendErrors(errList);
		sendSuccess(successList);
		list.clear();
	}

	private void sendErrors(List<ErrSubMsg> errList) {
		for (ErrSubMsg err : errList) {
			try {
				if (err.getResponseURI() != null && err.getResponseURI() != "") {
					http.sendHttp(err, 
								  err.getResponseURI(), 
								  err.getResponseContentType(), 
								  false,
								  err.getJuuid()
								  );
				} else if (err.getStoreClass() != null && err.getStoreClass() != "") {
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
						instance.setOperationType(OperationType.SUB);
						instance.copyFrom(err);
						contract = instance;
					}
					appConfig.getQueueWrite().put(contract);
				}
				appConfig.getQueueLog().put(err);
			} catch (Exception e) {
				try {
					
					System.out.println(e.getMessage());
					ErrSubMsg newErr = new ErrSubMsg();
					newErr.setErrorId(ErrorType.SubTask.toValue());
					newErr.setOperationType(OperationType.SUB);
					if (e.getCause() != null)
						newErr.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
					else
						newErr.setDetails(String.format("%s", e.getMessage()));
					newErr.setEventId(err.getEventId());
					newErr.setJuuid(err.getJuuid());
					newErr.setSenderId(err.getSenderId());
					newErr.setEndPointId(err.getSenderId());
					newErr.setSubscriberId(err.getSubscriberId());
					newErr.setPublisherId(err.getPublisherId());
					appConfig.getQueueLog().put(err);
					appConfig.getQueueLog().put(newErr);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		errList = null;
	}

	private void sendSuccess(List<SuccessSubMsg> list) {
		for (SuccessSubMsg success : list) {
			try {
				appConfig.getQueueLog().put(success);
			} catch (Exception e) {
				try {
					ErrSubMsg newErr = new ErrSubMsg();
					newErr.setErrorId(ErrorType.PubTask.toValue());
					newErr.setOperationType(OperationType.SUB);
					if (e.getCause() != null)
						newErr.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
					else
						newErr.setDetails(String.format("%s", e.getMessage()));
					newErr.setEventId(success.getEventId());
					newErr.setJuuid(success.getJuuid());
					newErr.setSenderId(success.getSenderId());
					newErr.setEndPointId(success.getSenderId());
					newErr.setSubscriberId(success.getSubscriberId());
					newErr.setPublisherId(success.getPublisherId());
					appConfig.getQueueLog().put(success);
					appConfig.getQueueLog().put(newErr);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private void collectError(List<ErrSubMsg> list, IContract msg, SubErrRouting subErrRouting, Exception e) {

		ErrSubMsg err = null;
		String logDatetime = DateConvertHelper.getCurDate();

		if (subErrRouting == null) {
			err = new ErrSubMsg();
			err.setErrorId(ErrorType.SubTask.toValue());
			err.setOperationType(OperationType.SUB);
			if (e.getCause() != null)
				err.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
			else
				err.setDetails(String.format("%s", e.getMessage()));
			err.setEventId(msg.getEventId());
			err.setJuuid(msg.getJuuid());
			err.setSenderId(msg.getSenderId());
			err.setEndPointId(msg.getSenderId());
			err.setPublisherId(msg.getPublisherId());
			err.setLogDatetime(logDatetime);

			list.add(err);
		} else {
			err = new ErrSubMsg();
			err.setErrorId(ErrorType.SubTask.toValue());
			err.setOperationType(OperationType.SUB);
			if (e.getCause() != null)
				err.setDetails(String.format("%s: %s", e.getMessage(), e.getCause()));
			else
				err.setDetails(String.format("%s", e.getMessage()));
			err.setEventId(msg.getEventId());
			err.setJuuid(msg.getJuuid());
			err.setSenderId(msg.getSenderId());
			err.setEndPointId(msg.getSenderId());
			err.setPublisherId(msg.getPublisherId());
			if (subErrRouting != null) {
				err.setResponseURI(subErrRouting.getSubscriberHandler());
				err.setSubscriberId(subErrRouting.getSubscriberId());
				err.setSubscriberHandler(subErrRouting.getSubscriberHandler());
				err.setStoreClass(subErrRouting.getSubscriberStoreClass());
				err.setResponseContentType(subErrRouting.getResponseContentType());
			}
			err.setLogDatetime(logDatetime);
			list.add(err);
		}
	}

	private void collectSuccess(List<SuccessSubMsg> list, IContract msg) {

		String logDatetime = DateConvertHelper.getCurDate();
		SuccessSubMsg success = new SuccessSubMsg();
		success.setEventId(msg.getEventId());
		success.setJuuid(msg.getJuuid());
		success.setSenderId(msg.getSenderId());
		success.setEndPointId(msg.getSenderId());
		success.setPublisherId(msg.getPublisherId());
		success.setLogDatetime(logDatetime);
		success.setOperationType(OperationType.SUB);
		list.add(success);
	}
}
