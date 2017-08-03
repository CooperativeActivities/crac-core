package crac.models.utility;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.module.matching.factories.CracFilterFactory;
import crac.module.matching.helpers.MatchingInformation;
import crac.module.matching.superclass.CracPreMatchingFilter;
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
		
		MatchingInformation mi = new MatchingInformation(pool, u);

		for(CracPreMatchingFilter f : pf.getFiltersObj()){
			pool = f.apply(mi);
		}

		return pool;
		
	}
	
	private void createFilters(PersonalizedFilters pf){
		
		for(Class<CracPreMatchingFilter> s : pf.getFiltersClass()){
			pf.addFilter((CracPreMatchingFilter) mf.createMatchingFilter(s));
		}
		
	}
	
}
