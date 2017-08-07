package crac.models.utility;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.module.matching.factories.CracFilterFactory;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;
import crac.module.matching.superclass.IndividualFilter;
import crac.module.utility.ElasticConnector;

@Component
@Scope("prototype")
public class TaskLookup {
	
	@Autowired
	private CracFilterFactory mf;
	
	@Autowired
	private ElasticConnector<Task> ect;
	
	@Autowired
	private TaskDAO taskDAO;

	public TaskLookup(){
		
	}
	
	public List<Task> lookUp(CracUser u, PersonalizedFilters pf){
		
		pf.convert(mf);
		
		String query = pf.getQuery();
		
		List<Task> pool = new ArrayList<>();
		
		if(query.equals("")){
			pool = taskDAO.selectSearchableTasks();
		}else{
			//pool = ect.queryForTasks(query);
			pool = taskDAO.selectNameContainingTasks(query);
		}
		
		FilterParameters fp = new FilterParameters();
		fp.setTasksPool(pool);
		fp.setUser(u);

		for(PersonalizedFilter p : pf.getFilters()){
			p.getCf().apply(fp);
		}
		
		return fp.getTasksPool();
		
	}
		
}
