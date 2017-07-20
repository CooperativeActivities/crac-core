package crac.components.matching;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.components.matching.configuration.PreMatchingConfiguration;
import crac.components.matching.configuration.UserFilterParameters;
import crac.components.matching.workers.TaskMatchingWorker;
import crac.models.db.entities.CracUser;

@Component
@Scope("prototype")
public class WorkerFactory {
	
	@Autowired
	private PreMatchingConfiguration pmc;
	
	public TaskMatchingWorker createTmWorker(CracUser u, UserFilterParameters up){
		System.out.println("factory called");
		return new TaskMatchingWorker(u, up, pmc);
	}

}
