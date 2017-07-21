package crac.components.matching.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.components.matching.configuration.MatchingConfiguration;
import crac.components.matching.configuration.PostMatchingConfiguration;
import crac.components.matching.configuration.PreMatchingConfiguration;
import crac.components.matching.configuration.UserFilterParameters;
import crac.components.matching.workers.TaskMatchingWorker;
import crac.components.matching.workers.UserMatchingWorker;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;

@Component
@Scope("prototype")
public class WorkerFactory {
	
	@Autowired
	private PreMatchingConfiguration pmc;
	
	@Autowired
	private MatchingConfiguration mc;

	@Autowired
	private PostMatchingConfiguration pomc;

	public TaskMatchingWorker createTmWorker(CracUser u, UserFilterParameters up){
		System.out.println("factory called");
		return new TaskMatchingWorker(u, up, pmc, mc, pomc);
	}

	public UserMatchingWorker createUmWorker(Task t, UserFilterParameters up){
		System.out.println("factory called");
		return new UserMatchingWorker(t, up, mc);
	}
}
