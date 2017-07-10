package com.simple.server.dao.nav;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.simple.server.dao.ADao;
import com.simple.server.domain.IRec;
import com.simple.server.domain.nav.INavRec;

@Repository("navDao")
public class NavDaoImpl extends ADao implements INavDao{
	
	@Override
	public Session currentSession() throws Exception {
		return appConfig.getNavSessionFactory().getCurrentSession();	
	}

	@Override
	public JdbcTemplate currentJDBCTemplate() throws Exception {
		return appConfig.getNavJdbcTemplate();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<IRec> readbyPK(IRec rec) throws Exception {
		if(rec==null)
			throw new IllegalArgumentException("NavDaoImpl readbyPK: param cannot be null");
		if(!(rec instanceof INavRec))
			throw new IllegalArgumentException("NavDaoImpl readbyPK: param must be instance of INavRec");
		
		INavRec navRec = (INavRec)rec;		
		List<IRec> list = currentSession().createCriteria(rec.getClass())
			    .add( Restrictions.like("JUUID", navRec.getJuuid()) )			    
			    .list();
		return list;
	}
}
