package crac.models.utility;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.module.matching.factories.CracFilterFactory;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;
import crac.module.utility.ElasticConnector;

@Component
@Scope("prototype")
public class TaskLookup {
	
	@Autowired
	private CracFilterFactory mf;
	
	@Autowired
	private ElasticConnector<Task> ect;

	public TaskLookup(){
		
	}
	
	public List<Task> lookUp(CracUser u, PersonalizedFilters pf){
				
		createFilters(pf);
		
		List<Task> pool = ect.queryForTasks(pf.getQuery());
		
		FilterParameters fp = new FilterParameters();
		fp.setTasksPool(pool);
		fp.setUser(u);

		for(ConcreteFilter f : pf.getFiltersObj()){
			f.apply(fp);
		}

		return pool;
		
	}
	
	private void createFilters(PersonalizedFilters pf){
		
		for(Class<ConcreteFilter> s : pf.getFiltersClass()){
			pf.addFilter((ConcreteFilter) mf.createMatchingFilter(s));
		}
		
	}
	
}
