package crac.module.factories;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.module.matching.configuration.MatchingConfiguration;
import crac.module.matching.configuration.PostMatchingConfiguration;
import crac.module.matching.configuration.PreMatchingConfiguration;
import crac.module.matching.configuration.UserFilterParameters;
import crac.module.matching.superclass.Worker;
import crac.module.matching.workers.TaskMatchingWorker;
import crac.module.matching.workers.UserCompetenceRelationEvolutionWorker;
import crac.module.matching.workers.UserMatchingWorker;
import crac.module.matching.workers.UserRelationEvolutionWorker;
import crac.module.notifier.Notification;
import crac.module.storage.CompetenceStorage;
import lombok.Getter;
import lombok.Setter;

@Component
@Scope("prototype")
public class WorkerFactory {

	@Autowired
	@Getter
	@Setter
	private PreMatchingConfiguration pmc;

	@Autowired
	@Getter
	@Setter
	private MatchingConfiguration mc;

	@Autowired
	@Getter
	@Setter
	private PostMatchingConfiguration pomc;

	@Autowired
	@Getter
	@Setter
	private TaskDAO taskDAO;

	@Autowired
	@Getter
	@Setter
	private UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	@Getter
	@Setter
	private CracUserDAO userDAO;

	@Autowired
	@Getter
	@Setter
	private UserRelationshipDAO userRelalationshipDAO;

	@Autowired
	@Getter
	@Setter
	private CompetenceStorage cs;

	public <T extends Worker> Worker createWorker(Class<T> type, HashMap<String, Object> params) {
		Worker w = null;
		if (type == TaskMatchingWorker.class) {
			w = new TaskMatchingWorker((CracUser) params.get("user"),
					(UserFilterParameters) params.get("userFilterParameters"));
		}else if(type == UserMatchingWorker.class){
			w = new UserMatchingWorker((Task) params.get("task"),
					(UserFilterParameters) params.get("userFilterParameters"));
		}else if(type == UserRelationEvolutionWorker.class){
			w = new UserRelationEvolutionWorker((Evaluation) params.get("evaluation"));
		}else if(type == UserCompetenceRelationEvolutionWorker.class){
			w = new UserCompetenceRelationEvolutionWorker((Evaluation) params.get("evaluation"));
		}else{
			return null;
		}
		w.setWf(this);
		return w;
	}


}
