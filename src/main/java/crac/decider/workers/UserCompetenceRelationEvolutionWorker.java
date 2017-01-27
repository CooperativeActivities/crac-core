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
	
	public UserCompetenceRelationEvolutionWorker(Evaluation evaluation, UserCompetenceRelDAO userCompetenceRelDAO) {
		super();
		this.user = evaluation.getUser();
		this.task = evaluation.getTask();
		this.evaluation = evaluation;
		this.userCompetenceRelDAO = userCompetenceRelDAO;
	}

	public void run(){
		for(CompetenceTaskRel ctr : task.getMappedCompetences()){
			UserCompetenceRel ucr = userCompetenceRelDAO.findByUserAndCompetence(user, ctr.getCompetence());
			int likeValue = ucr.getLikeValue();
			int profValue = ucr.getProficiencyValue();
			likeValue += (evaluation.getLikeValTask() / 4) * 100 ;
			profValue += 10;
			if(likeValue > 100){
				likeValue = 100;
			}else if(likeValue < -100){
				likeValue = -100;
			}
			if(profValue > 100){
				profValue = 100;
			}else if(profValue < 0){
				profValue = 0;
			}
			
			ucr.setLikeValue(likeValue);
			ucr.setProficiencyValue(profValue);
			userCompetenceRelDAO.save(ucr);
		}
	}
	
}
