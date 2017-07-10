package com.simple.server.dao;

import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;

import com.simple.server.config.MiscType;
import com.simple.server.domain.IRec;
import com.simple.server.domain.contract.IContract;

public interface IDao {
	
	Session currentSession() throws Exception;	
	JdbcTemplate currentJDBCTemplate() throws Exception;
	@Transactional
	void insert(List<IRec> list) throws Exception;
	void insert(IRec rec) throws Exception;
	void insertAsIs(IContract msg) throws Exception;
	void insertAsIs(List<IContract> list) throws Exception;	
	void insert(String sql) throws Exception;
	
	
	
	String readFlatJsonArray(String sql) throws Exception;
	String readComplexJsonArray(String sql) throws Exception;
	String getFlatJsonFirstObj(String sql) throws Exception;
	String readFlatXml(String sql) throws Exception;
	List<Map<String, Object>> getListMap(String sql) throws Exception;
	List<IRec> read(IRec rec) throws Exception;	
	<T extends IContract> List<T> readbyHQL(Class<T> clazz, String query, Map<String,String> params) throws Exception;
	<T extends IContract> List<T> readbyCriteria(Class<T> clazz, Map<String,Object> params, int topNum, Map<String,MiscType> orders) throws Exception;
	
	List<IRec> readAll(IRec rec) throws Exception;
	void deleteAsIs(IContract msg) throws Exception;
}
