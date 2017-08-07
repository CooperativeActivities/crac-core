package crac.module.matching.factories;

import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.module.matching.interfaces.CracFilter;
import crac.module.matching.superclass.ConcreteFilter;

@Component
@Scope("prototype")
public class CracFilterFactory {
	
	public <T extends ConcreteFilter> ConcreteFilter createMatchingFilter(Class<T> type){
				
		return BeanUtils.instantiate(type);

	}

}
