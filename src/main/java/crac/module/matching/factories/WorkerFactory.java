package crac.module.matching.factories;

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

@Component
@Scope("prototype")
public class WorkerFactory {

	@Autowired
	private PreMatchingConfiguration pmc;

	@Autowired
	private MatchingConfiguration mc;

	@Autowired
	private PostMatchingConfiguration pomc;

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private UserRelationshipDAO userRelalationshipDAO;

	@Autowired
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

	public TaskDAO getTaskDAO() {
		return taskDAO;
	}

	public void setTaskDAO(TaskDAO taskDAO) {
		this.taskDAO = taskDAO;
	}

	public PreMatchingConfiguration getPmc() {
		return pmc;
	}

	public void setPmc(PreMatchingConfiguration pmc) {
		this.pmc = pmc;
	}

	public MatchingConfiguration getMc() {
		return mc;
	}

	public void setMc(MatchingConfiguration mc) {
		this.mc = mc;
	}

	public PostMatchingConfiguration getPomc() {
		return pomc;
	}

	public void setPomc(PostMatchingConfiguration pomc) {
		this.pomc = pomc;
	}

	public UserCompetenceRelDAO getUserCompetenceRelDAO() {
		return userCompetenceRelDAO;
	}

	public void setUserCompetenceRelDAO(UserCompetenceRelDAO userCompetenceRelDAO) {
		this.userCompetenceRelDAO = userCompetenceRelDAO;
	}

	public CracUserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(CracUserDAO userDAO) {
		this.userDAO = userDAO;
	}

	public UserRelationshipDAO getUserRelalationshipDAO() {
		return userRelalationshipDAO;
	}

	public void setUserRelalationshipDAO(UserRelationshipDAO userRelalationshipDAO) {
		this.userRelalationshipDAO = userRelalationshipDAO;
	}

	public CompetenceStorage getCs() {
		return cs;
	}

	public void setCs(CompetenceStorage cs) {
		this.cs = cs;
	}

}
