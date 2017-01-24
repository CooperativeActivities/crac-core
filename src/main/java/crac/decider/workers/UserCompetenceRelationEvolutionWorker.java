package crac.decider.workers;

import crac.daos.TaskDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.daos.UserRelationshipDAO;
import crac.daos.UserTaskRelDAO;
import crac.decider.core.UserFilterParameters;
import crac.decider.core.Worker;
import crac.models.CracUser;
import crac.models.Evaluation;
import crac.models.Task;
import crac.models.relation.CompetenceTaskRel;
import crac.models.relation.UserCompetenceRel;
import crac.models.relation.UserTaskRel;

public class UserCompetenceRelationEvolutionWorker extends Worker {

	private CracUser user;
	private Task task;
	private Evaluation evaluation;
	private UserCompetenceRelDAO userCompetenceRelDAO;
	
	public UserCompetenceRelationEvolutionWorker(CracUser user, Task task, Evaluation evaluation, UserCompetenceRelDAO userCompetenceRelDAO) {
		super();
		this.user = user;
		this.task = task;
		this.evaluation = evaluation;
		this.userCompetenceRelDAO = userCompetenceRelDAO;
	}

	public void run(){
		for(CompetenceTaskRel ctr : task.getMappedCompetences()){
			UserCompetenceRel ucr = userCompetenceRelDAO.findByUserAndCompetence(user, ctr.getCompetence());
			double likeValue = ucr.getLikeValue();
			double profValue = ucr.getProficiencyValue();
			likeValue = likeValue * (1 + (((1 - likeValue / 2) * evaluation.getLikeValTask()) * 0.7));
			profValue = profValue * (1 + (((1 - profValue / 2) * 0.7) * 0.7));
			userCompetenceRelDAO.save(ucr);
		}
	}
	
}
