
package com.simple.server.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;

import com.simple.server.config.AppConfig;
import com.simple.server.config.ContentType;
import com.simple.server.util.HttpNotFoundException;
import com.simple.server.util.ObjectConverter;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@Service("httpImpl")
@Scope("singleton")
public class HttpImpl implements IHttp {

	@Autowired
	private AppConfig appConfig;
	
	
	private static final Logger logger = LogManager.getLogger(HttpImpl.class);
	
	@Override
	public void sendHttp(Object msg, String url, ContentType contentType, Boolean useAuth, String msgId) throws Exception {
		try {
			prepareAndSend(msg, url, contentType, useAuth, msgId);
		} catch (RestClientException ex) {
			if (ex.getCause() == null) {
				throw new Exception(String.format("HttpImpl(UniMinMsg) url: < %s >, content-type: < %s >, < %s >", url,
						contentType, ex.getMessage()));
			} else {
				throw new Exception(String.format("HttpImpl(UniMinMsg) url: <%s>, content-type: < %s >, < %s >, < %s >",
						url, contentType, ex.getMessage(), ex.getCause()));
			}
		}
	}

	public void prepareAndSend(Object msg, String url, ContentType contentType, Boolean useAuth, String msgId) throws Exception {
		String converted = null;
		String sContentType = null;
		if (ContentType.XmlPlainText.equals(contentType)) {
			converted = ObjectConverter.objectToXml(msg, false);
			sContentType = "text/plain;charset=utf-8";
		} else if (ContentType.ApplicationJson.equals(contentType)) {
			converted = ObjectConverter.objectToJson(msg);
			sContentType = "application/json;charset=utf-8";
		} else if (ContentType.ApplicationXml.equals(contentType)) {
			converted = ObjectConverter.objectToXml(msg, false);
			sContentType = "application/xml;charset=utf-8";
		} else {
			converted = ObjectConverter.objectToJson(msg);
			sContentType = "text/plain;charset=utf-8";
		}
		post(converted, url, sContentType, contentType,  useAuth, msgId);
	}
	

	public void post(String body, String url, String sContentType, ContentType contentType, Boolean useAuth, String msgId) throws Exception {
		
		logger.debug(String.format("[HttpImpl] [PRE] %s %s , thread id: %s , thread name:  %s", url, msgId, Thread.currentThread().getId(), Thread.currentThread().getName()));
		if (useAuth) {
			postNTLM(body, url, sContentType, msgId);
		} else {
			ResponseEntity<String> response = null;
			URI uri = new URI(url);			
												
			HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		     httpRequestFactory.setConnectionRequestTimeout(appConfig.timeoutPolicies.getBackAsyncConnectionRequestTimeout());
			 httpRequestFactory.setConnectTimeout(appConfig.timeoutPolicies.getBackAsyncConnectionTimeout());
			 httpRequestFactory.setReadTimeout(appConfig.timeoutPolicies.getBackAsyncReadTimeout());
			
		    RestTemplate restTemplate = new RestTemplate(httpRequestFactory);		        
			HttpEntity<String> entity = null;
			entity = new HttpEntity<String>(body, createHeaders(contentType));
			try{
				response = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
				checkHttpResonseStatusCode(url, response.getStatusCode().value());				
			}catch(RestClientException e){
				logger.debug(String.format("[HttpImpl] [ERR] %s %s %s, thread id: %s , thread name:  %s", url, msgId, e.getMessage(), Thread.currentThread().getId(), Thread.currentThread().getName()));
				throw new HttpNotFoundException(String.format("HttpImpl, url: < %s >, %s",e.getMessage(), url));
			}			
		}
		logger.debug(String.format("[HttpImpl] [OK] %s %s , thread id: %s , thread name:  %s", url, msgId, Thread.currentThread().getId(), Thread.currentThread().getName()));
	}

	
	@SuppressWarnings("deprecation")
	public void postNTLM(String body, String url, String contentType, String msgId) throws Exception {

		
		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams, appConfig.timeoutPolicies.getBackAsyncConnectionTimeout());
		HttpConnectionParams.setSoTimeout(my_httpParams, appConfig.timeoutPolicies.getBackAsyncReadTimeout());
	
		DefaultHttpClient httpclient = new DefaultHttpClient(my_httpParams);
		try {
			httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
				public void process(final HttpRequest request, final HttpContext context)
						throws HttpException, IOException {
					request.addHeader("Accept-Encoding", "gzip, deflate");
					request.addHeader("Accept", "*/*");
					request.addHeader("Accept-Language", " ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3");
					request.addHeader("Content-Type", contentType);					
				}
			});

			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(url);

			httpPost.addHeader("Content-Type", contentType);
			
			
			StringEntity entity = new StringEntity(body,  "utf-8");

			httpPost.setEntity(entity);
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(AuthScope.ANY,
					new NTCredentials(AppConfig.ACC, AppConfig.PSW, AppConfig.WORKSTATION, AppConfig.DOMEN));

			List<String> authtypes = new ArrayList<String>();
			authtypes.add(AuthPolicy.NTLM);
			httpclient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authtypes);

			localContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);			

			HttpResponse response = httpclient.execute(httpPost, localContext);
			
			checkHttpResonseStatusCode(url, response.getStatusLine().getStatusCode());
						
			if (response.getEntity() != null) {
				response.getEntity().consumeContent();
			}
		} catch (HttpNotFoundException e) {
			logger.debug(String.format("[HttpImpl] [ERR] NTLM %s %s %s, thread id: %s , thread name:  %s", url, msgId, e.getMessage(), Thread.currentThread().getId(), Thread.currentThread().getName()));
			throw new HttpNotFoundException(String.format("HttpImpl NTLM: %s",e.getMessage()));
		} catch (Exception e) {
			logger.debug(String.format("[HttpImpl] [ERR] NTLM %s %s %s, thread id: %s , thread name:  %s", url, msgId, e.getMessage(), Thread.currentThread().getId(), Thread.currentThread().getName()));
			throw new Exception(String.format("HttpImpl NTLM: %s",e.getMessage()));
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
	}

	
	private void checkHttpResonseStatusCode(String url, int statusCode) throws HttpNotFoundException{
		if (statusCode < 200 || statusCode > 300) 
			throw new HttpNotFoundException(String.format("HTTP Error, url: < %s >, status code: %s", url, statusCode));
	}
	
	
	public static HttpHeaders createHeaders(ContentType contentType) {
		return new HttpHeaders() {
			{
				if (ContentType.XmlPlainText.equals(contentType) || ContentType.ApplicationXml.equals(contentType)) {
					setContentType(new MediaType("application", "xml", Charset.forName("UTF-8")));
				} else if (ContentType.ApplicationJson.equals(contentType)) {
					setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));					
				}				
			}
		};
	}
	
	/*
	public static HttpHeaders createHeaders(String username, String password, ContentType contentType) {
		return new HttpHeaders() {
			{
				String auth = username + ":" + password;
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);
				set("Authorization", authHeader);
				setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
			}
		};
	}
	*/
}



