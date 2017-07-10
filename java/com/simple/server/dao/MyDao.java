package com.simple.server.dao;

import java.util.List;

import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.simple.server.domain.IRec;


@Repository("myDao")
public class MyDao extends ADao{

	@Override
	public Session currentSession() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JdbcTemplate currentJDBCTemplate() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<IRec> readbyPK(IRec rec) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
