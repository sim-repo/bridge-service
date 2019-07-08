package com.simple.server;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.simple.server.config.AppConfig;
import com.simple.server.domain.IRec;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.contract.PubErrRouting;
import com.simple.server.domain.contract.PubSuccessRouting;
import com.simple.server.domain.contract.SubRouting;
import com.simple.server.domain.log.LogTimeoutPolicies;
import com.simple.server.service.IService;
import com.simple.server.util.MyLogger;


@Controller
public class HomeController {
	
	@Autowired
	private AppConfig appConfig;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	
	/**
	 * <p> Утилита: получить закэшированный список политики таймаутов </p>
	 * <p> Вызов: http://msk10websvc2:8888/front/util/cache/allTimeouts </p>
	 * <p> Используйте для проверки значений таймаутов, используемых в GET- и POST-запросах  </p>
	 * <p> Внутренняя настроечная таблица: [timeout policies] </p>
	 * @return возвращает JSON</p>
	 * @author Иванов И.
	 * @version 1.0	 
	 */
	@RequestMapping(value = "util/cache/timeouts", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String jsonTimeoutsGet() {		
		StringBuilder ret = new StringBuilder();
		
		ret.append("front_sync_read_timeout:"+appConfig.timeoutPolicies.getFrontSyncReadTimeout()+"\n\n\n");
		ret.append("front_sync_connection_request_timeout:"+appConfig.timeoutPolicies.getFrontSyncConnectionRequestTimeout()+"\n\n\n");
		ret.append("front_sync_connection_timeout:"+appConfig.timeoutPolicies.getFrontSyncConnectionTimeout()+"\n\n\n");
		ret.append("back_async_read_timeout:"+appConfig.timeoutPolicies.getBackAsyncReadTimeout()+"\n\n\n");
		ret.append("back_async_connection_request_timeout:"+appConfig.timeoutPolicies.getBackAsyncConnectionRequestTimeout()+"\n\n\n");
		ret.append("back_async_connection_timeout:"+appConfig.timeoutPolicies.getBackAsyncConnectionTimeout()+"\n\n\n");
		
		return ret.toString();		
	}
	
	
	/**
	 * <p> Утилита: рефрешинг кэша из [timeout policies]</p>
	 * @author Иванов И.
	 * @version 1.0	 	 
	 */
	@RequestMapping(value = "/util/cache/timeouts/refresh", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String jsonRefresTimeoutsGet() {		
		StringBuilder ret = new StringBuilder();
		
		List<IContract> res3 = null;
		try {
			List<IRec> timeoutPolicies = appConfig.getLogService().readAll(appConfig.LOG_ENDPOINT_NAME, LogTimeoutPolicies.class);
			if(timeoutPolicies != null) {			
				for(IRec rec: timeoutPolicies) {
					appConfig.timeoutPolicies = (LogTimeoutPolicies)rec;						
				}
			}		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			ret.append(e.getMessage()+"\n\n\n");
		}			
						
		ret.append("front_sync_read_timeout:"+appConfig.timeoutPolicies.getFrontSyncReadTimeout()+"\n\n\n");
		ret.append("front_sync_connection_request_timeout:"+appConfig.timeoutPolicies.getFrontSyncConnectionRequestTimeout()+"\n\n\n");
		ret.append("front_sync_connection_timeout:"+appConfig.timeoutPolicies.getFrontSyncConnectionTimeout()+"\n\n\n");
		ret.append("back_async_read_timeout:"+appConfig.timeoutPolicies.getBackAsyncReadTimeout()+"\n\n\n");
		ret.append("back_async_connection_request_timeout:"+appConfig.timeoutPolicies.getBackAsyncConnectionRequestTimeout()+"\n\n\n");
		ret.append("back_async_connection_timeout:"+appConfig.timeoutPolicies.getBackAsyncConnectionTimeout()+"\n\n\n");
		
		return ret.toString();		
	}
	
	/**
	 * <p> Утилита: рефрешинг кэша из [routing sub]</p>
	 * @author Иванов И.
	 * @version 1.0	 	 
	 */
	@RequestMapping(value = "/util/cache/routes/refresh", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	public @ResponseBody String jsonRefresRoutingGet() {		
		StringBuilder ret = new StringBuilder();
		MyLogger.warnStartBlock(getClass(), "MANUAL REFRESH ROUTES: starting...");
		
		List<IContract> res3 = null;
		try {
			appConfig.clearTmpRoutes();
			
			IService service = appConfig.getServiceFactory().getService(appConfig.LOG_ENDPOINT_NAME);
			
			List<SubRouting> routes = service.<SubRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, SubRouting.class, null, 0, null);			 			
			List<PubSuccessRouting> successRoutes = service.<PubSuccessRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, PubSuccessRouting.class, null, 0, null);	
			List<PubErrRouting> errRoutes = service.<PubErrRouting>readbyCriteria(appConfig.LOG_ENDPOINT_NAME, PubErrRouting.class, null, 0, null);	
					
			
			if(routes != null) {
				MyLogger.warnSingleHeader(getClass(), "Sub Routes;"+routes.size());						
				for(IContract rec: routes) {
					SubRouting r = (SubRouting)rec;					
					appConfig.setRoutes(r.getSenderId()+r.getEventId(), r);
					MyLogger.warn(getClass(),r.getPublisherId()+";"+r.getEventId());	
				}
				
			}
			
			if(successRoutes != null) {				
				MyLogger.warnSingleHeader(getClass(), "Success Routes;"+successRoutes.size());
				for(IContract rec: successRoutes) {
					PubSuccessRouting r = (PubSuccessRouting)rec;
					MyLogger.warn(getClass(),r.getPublisherId()+";"+r.getEventId());	
					appConfig.setSuccessRoutes(r.getPublisherId()+r.getEventId(), r);					
				}					
			}
			
			if(errRoutes != null) {
				MyLogger.warnSingleHeader(getClass(), "Err Routes;"+errRoutes.size());							
				for(IContract rec: errRoutes) {
					PubErrRouting r = (PubErrRouting)rec;
					MyLogger.warn(getClass(),r.getPublisherId()+";"+r.getEventId());	
					appConfig.setErrRoutes(r.getPublisherId()+r.getEventId(), r);					
				}
			}
			
			if (appConfig.refreshAllRoutes() == false) {
				String s = "MANUAL REFRESH ROUTES: check database connections!!!";
				ret.append(s);
				MyLogger.error(getClass(), s);
				MyLogger.warnEndBlock(getClass(), "MANUAL REFRESH ROUTES: failed.");
			} else {
				ret.append("Routes: " + appConfig.getSizeRoutes() +"\n" + appConfig.logRoutes()+"\n\n\n");
				ret.append("Success Routes: " + appConfig.getSizeSuccessRoutes() +"\n" + appConfig.logSuccessRoutes()+"\n\n\n");
				ret.append("Error Routes: " + appConfig.getSizeErrRoutes() +"\n" + appConfig.logErrRoutes()+"\n\n\n");
				MyLogger.warnEndBlock(getClass(), "MANUAL REFRESH ROUTES: successful.");
			}
			
		} catch (Exception e) {
			MyLogger.error(getClass(), e);
			ret.append(e.getMessage());
		}									
		return ret.toString();		
	}
	
}
