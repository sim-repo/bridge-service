package com.simple.server;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.simple.server.config.AppConfig;
import com.simple.server.domain.IRec;
import com.simple.server.domain.contract.IContract;
import com.simple.server.domain.log.LogTimeoutPolicies;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	@Autowired
	private AppConfig appConfig;
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
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
	
}
