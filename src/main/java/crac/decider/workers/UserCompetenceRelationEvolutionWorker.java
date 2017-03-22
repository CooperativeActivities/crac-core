package crac.decider.workers;

import crac.decider.core.Worker;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import crac.utility.DataAccess;

public class UserCompetenceRelationEvolutionWorker extends Worker {

	private CracUser user;
	private Task task;
	private Evaluation evaluation;
	private UserCompetenceRelDAO userCompetenceRelDAO;
	
	public UserCompetenceRelationEvolutionWorker(Evaluation evaluation) {
		super();
		this.user = evaluation.getUser();
		this.task = evaluation.getTask();
		this.evaluation = evaluation;
		this.userCompetenceRelDAO = DataAccess.getRepo(UserCompetenceRelDAO.class);
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
