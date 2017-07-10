package com.simple.server.dao.log;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.simple.server.dao.ADao;
import com.simple.server.domain.IRec;


@Repository("logDao") 
public class LogDaoImpl extends ADao implements ILogDao{
	
	@Override
	public Session currentSession() throws Exception {
		return appConfig.getLogSessionFactory().getCurrentSession();
	}

	@Override
	public JdbcTemplate currentJDBCTemplate() throws Exception {
		return appConfig.getLogJdbcTemplate();
	}	

	@Override
	public List<IRec> readbyPK(IRec rec) throws Exception {
		IRec r = (IRec) currentSession().load(rec.getClass(), rec);		
		return Arrays.asList(r);
	}	

}
