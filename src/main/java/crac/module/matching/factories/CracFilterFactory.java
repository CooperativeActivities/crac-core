package crac.module.matching.factories;

import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.module.matching.interfaces.CracFilter;

@Component
@Scope("prototype")
public class CracFilterFactory {
	
	public <T extends CracFilter> CracFilter createMatchingFilter(Class<T> type){
				
		return BeanUtils.instantiate(type);

	}

}
