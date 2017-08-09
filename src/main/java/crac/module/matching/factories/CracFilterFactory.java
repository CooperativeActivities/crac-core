package crac.module.matching.factories;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.GroupDAO;
import crac.module.matching.superclass.ConcreteFilter;
import lombok.Getter;
import lombok.Setter;

@Component
@Scope("prototype")
public class CracFilterFactory {
	
	@Autowired
	@Getter
	@Setter
	CracUserDAO userDAO;
	
	@Autowired
	@Getter
	@Setter
	GroupDAO groupDAO;

	public <T extends ConcreteFilter> ConcreteFilter createMatchingFilter(Class<T> type){
				
		ConcreteFilter f = BeanUtils.instantiate(type);
		f.setCff(this);
		
		return f;

	}

}
