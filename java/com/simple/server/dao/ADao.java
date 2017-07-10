package com.simple.server.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.server.config.AppConfig;
import com.simple.server.config.ContentType;
import com.simple.server.config.MiscType;
import com.simple.server.domain.IRec;
import com.simple.server.domain.UniRequest;
import com.simple.server.domain.UniResult;
import com.simple.server.domain.contract.IContract;
import com.simple.server.statistics.time.Timing;
import com.simple.server.util.ObjectConverter;

public abstract class ADao implements IDao{

	@Autowired
	protected AppConfig appConfig;

	@Override
	public void insert(List<IRec> list) throws Exception {
		int count=0;
		for(IRec rec: list){
			try{							
				Thread.currentThread().sleep(Timing.TIME_MID_SLEEP);
				currentSession().save(rec);	
			}catch(SQLException e){
				e.printStackTrace();
			}			
			if (++count % 50 == 0 ) {
				currentSession().flush();
				currentSession().clear();
			}
		}	
	}
	
	@Override
	public void insert(IRec rec) throws Exception {
		currentSession().save(rec);	
	}
	
	@Override
	public void insertAsIs(IContract msg) throws Exception {
		currentSession().saveOrUpdate(msg);	
	}

	@Override
	public void insertAsIs(List<IContract> list) throws Exception {
		int count=0;
		for(IContract msg: list){
			try{	
				Thread.currentThread().sleep(Timing.TIME_MIN_SLEEP);
				currentSession().save(msg);	
			}catch(SQLException e){
				e.printStackTrace();
			}			
			if (++count % 50 == 0 ) {
				currentSession().flush();
				currentSession().clear();
			}
		}	
	}
	
	@Override
	public void insert(String sql) throws Exception {
		Query query = currentSession().createQuery(sql);
		query.executeUpdate();		
	}
	
	@Override
	public void deleteAsIs(IContract msg) throws Exception {
		currentSession().delete(msg);	
	}
	
	@Override
	public String readFlatJsonArray(String sql) throws Exception {
		List<Map<String,Object>> list =  currentJDBCTemplate().queryForList(sql);			
		return ObjectConverter.listMapToJson(list);
	}
	
	@Override
	public String getFlatJsonFirstObj(String sql) throws Exception{		
		List<Map<String,Object>> list =  currentJDBCTemplate().queryForList(sql);			
		return listMapToJsonFirstObj(list);
	}

	
	@Override
	public List<Map<String, Object>> getListMap(String sql) throws Exception {		
		List<Map<String,Object>> list =  currentJDBCTemplate().queryForList(sql);										
		return list;				
	}

	@Override
	public String readComplexJsonArray(String sql) throws Exception {
		List<Map<String,Object>> list =  currentJDBCTemplate().queryForList(sql);				
		StringBuilder result = new StringBuilder();			
		for(Map<String, Object> map: list){		
			for(Map.Entry<String, Object> pair: map.entrySet()){				
				result.append(pair.getValue());			
			}		
		}			
		JSONObject jObject = XML.toJSONObject(result.toString());
	    ObjectMapper mapper = new ObjectMapper();	   
	    Object json = mapper.readValue(jObject.toString(), Object.class);
	    String output = mapper.writeValueAsString(json);
		return output;
	}

	@Override
	public String readFlatXml(String sql) throws Exception {
		List<Map<String,Object>> list =  currentJDBCTemplate().queryForList(sql);
		StringBuilder result = new StringBuilder();		
				
		for(Map<String, Object> map: list){		
			for(Map.Entry<String, Object> pair: map.entrySet()){				
				result.append(pair.getValue());
			}		
		}			
		JSONObject jObject = XML.toJSONObject(result.toString());
	    ObjectMapper mapper = new ObjectMapper();	   
	    Object json = mapper.readValue(jObject.toString(), Object.class);
	    String output = mapper.writeValueAsString(json);
		return output;
	}
	
	
	
	protected abstract List<IRec> readbyPK(IRec rec) throws Exception;	
		
	@Override
	public List<IRec> readAll(IRec rec) throws Exception{
		Criteria criteria = currentSession().createCriteria(rec.getClass());
		return criteria.list();
	}
	
	@Override
	public List<IRec> read(IRec rec) throws Exception{
		List<IRec> res = new ArrayList<IRec> ();
		if(rec instanceof UniRequest){
			UniRequest r = (UniRequest)rec;
			UniResult u = new UniResult();
			u.setResponseContentType(r.getResponseContentType());
			u.setResponseURI(r.getResponseURI());
			if(ContentType.JsonPlainText.equals(r.getResponseContentType())){				 
				u.setResult(readFlatJsonArray(r.getQuery()));				
			}
			else if(ContentType.XmlPlainText.equals(r.getResponseContentType())){				
				u.setResult(readFlatXml(r.getQuery()));				
			}
			res.add(u);			
		}
		else{
			res.addAll(readbyPK(rec));
		}
		return res;
	}

	@Override
	public <T extends IContract> List<T> readbyHQL(Class<T> clazz, String query, Map<String,String> params) throws Exception {		
		Query q = currentSession().createQuery(query);
		for(Map.Entry<String,String> pair: params.entrySet()){						
			q.setParameter(pair.getKey(), pair.getValue());
		}				
		List<T> res = q.list();
		return res;
	}
	
	
	@Override
	public synchronized <T extends IContract> List<T> readbyCriteria(Class<T> clazz, Map<String,Object> params, int topNum, Map<String,MiscType> orders) throws Exception{		
		Criteria criteria = currentSession().createCriteria(clazz);	
		if(params != null)
			for(Map.Entry<String,Object> pair: params.entrySet()){						
				criteria.add(Restrictions.eq(pair.getKey(), pair.getValue()));			
			}		
		if(topNum != 0){
			criteria.setMaxResults(topNum);
		}
		if(orders != null && orders.size() != 0){
			for(Map.Entry<String, MiscType> pair: orders.entrySet()){
				String fld = pair.getKey();								
				if(pair.getValue().equals(MiscType.asc))
					criteria.addOrder(Order.asc(fld));
				else
					if(pair.getValue().equals(MiscType.desc))
						criteria.addOrder(Order.desc(fld));
			}
		}
						
		List<T> res = criteria.list();			
		StringBuilder er = new StringBuilder();
		System.out.println("bridge::ado.readbyCriteria"+res.size());
		if (res.size()==0){
			res = criteria.list();	
		}			
		return res;
	}
	
	
	public String listMapToJsonFirstObj(List<Map<String, Object>> list){       
	   
		JSONObject json_obj=new JSONObject();
	    for (Map<String, Object> map : list) {
	        
	        for (Map.Entry<String, Object> entry : map.entrySet()) {
	            String key = entry.getKey();
	            Object value = entry.getValue();
	            try {
	                json_obj.put(key,value);
	            } catch (JSONException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }                           
	        }
	        return json_obj.toString();
	    }
	    return null;
	}

}
