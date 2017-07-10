package com.simple.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import com.simple.server.config.AppConfig;
import com.simple.server.config.MiscType;
import com.simple.server.dao.IDao;
import com.simple.server.domain.IRec;
import com.simple.server.domain.contract.IContract;
import com.simple.server.util.ObjectConverter;

public abstract class AService implements IService {
	@Autowired
	AppConfig appConfig;

	@Override
	public AppConfig getAppConfig() throws Exception {
		return appConfig;
	}
		
	@Override
	public IDao getDao() throws Exception {		
		return getAppConfig().getMyDao();
	}


	@Override	
	public void qInsert(IContract msg) throws Exception {
		if (msg.getIsDirectInsert())
			insertAsIs(msg);
		else {			
			IRec rec = getAppConfig().getContractRecFactory().newRec(msg);
			getAppConfig().getEndpointCfg().getQueue(msg.getEndPointId().toValue()).add(rec);										
		}
	}
	

	@Override
	public void qInsert(String sql) throws Exception {
		getDao().insert(sql);
	}

	@Override
	public void qInsert(List<IContract> list) throws Exception {	
		List<IRec> recs = getAppConfig().getContractRecFactory().newRecList(list); 
		// getDao().insert(recs); temporary commented
		this.getDaoQueue().addAll(recs);
	}

	
	
	@Override	
	public void insert(IContract msg) throws Exception {
		if (msg.getIsDirectInsert())
			insertAsIs(msg);
		else {			
			IRec rec = getAppConfig().getContractRecFactory().newRec(msg);								
			getDao().insert(rec);			
		}
	}
	

	@Override
	public void insert(String sql) throws Exception {
		getDao().insert(sql);
	}

	@Override
	public void insert(List<IContract> list) throws Exception {	
		List<IRec> recs = getAppConfig().getContractRecFactory().newRecList(list); 
		getDao().insert(recs);		
	}

	@Override
	public void insertAsIs(IContract msg) throws Exception {
		getDao().insertAsIs(msg);
	}

	@Override
	public void insertAsIs(List<IContract> list) throws Exception {
		getDao().insertAsIs(list);
	}

	@Override
	public void deleteAsIs(IContract msg) throws Exception {
		getDao().deleteAsIs(msg);
	}

	@Override
	public String readFlatJson(String sql) throws Exception {
		String res = getDao().readFlatJsonArray(sql);
		return res;
	}

	@Override
	public String readComlexJson(String sql) throws Exception {
		String res = getDao().readComplexJsonArray(sql);
		return res;
	}

	@Override
	public String getFlatJsonFirstObj(String sql) throws Exception {
		String res = getDao().getFlatJsonFirstObj(sql);
		return res;
	}

	@Override
	public String readFlatXml(String sql) throws Exception {
		String res = getDao().readFlatXml(sql);
		return res;
	}

	@Override
	public List<IContract> readAll(IContract msg) throws Exception {
		IRec rec = getAppConfig().getContractRecFactory().newRec(msg);
		List<IRec> list = getDao().readAll(rec);
		if (list.size() == 0)
			return null;
		List<IContract> res = new ArrayList<IContract>();
		for (IRec element : list) {
			IContract contract = getAppConfig().getContractRecFactory().newContract(element);
			res.add(contract);
		}
		return res;
	}

	@Override
	public <T extends IContract> List<T> readbyHQL(String sql, Class<T> clazz, Map<String, String> params)
			throws Exception {
		List<T> res = getDao().<T>readbyHQL(clazz, sql, params);
		return res;
	}

	@Override
	public synchronized <T extends IContract> List<T> readbyCriteria(Class<T> clazz, Map<String, Object> params,
			int topNum, Map<String, MiscType> orders) throws Exception {
		List<T> res = getDao().<T>readbyCriteria(clazz, params, topNum, orders);
		return res;
	}

	@Override
	public List<IContract> read(IContract msg) throws Exception {
		IRec rec = getAppConfig().getContractRecFactory().newRec(msg);
		List<IRec> list = getDao().read(rec);
		if (list.size() == 0)
			return null;
		List<IContract> res = new ArrayList<IContract>();
		for (IRec element : list) {
			IContract contract = getAppConfig().getContractRecFactory().newContract(element);
			res.add(contract);
		}
		return res;
	}

	@Override
	public <T extends IContract> List<T> read(String sql, Class<T> clazz) throws Exception {
		String json = getDao().readFlatJsonArray(sql);
		T t = clazz.newInstance();
		List<T> res = ObjectConverter.jsonToObjects(json, clazz);
		return res;
	}

	@Override
	public List<Map<String, Object>> getListMap(String sql) throws Exception {
		return getDao().getListMap(sql);
	}

}
