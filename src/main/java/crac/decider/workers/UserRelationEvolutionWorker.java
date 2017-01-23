package crac.decider.workers;

import crac.daos.TaskDAO;
import crac.daos.UserRelationshipDAO;
import crac.daos.UserTaskRelDAO;
import crac.decider.core.Worker;
import crac.models.CracUser;
import crac.models.Evaluation;
import crac.models.Task;
import crac.models.relation.UserRelationship;
import crac.models.relation.UserTaskRel;
import crac.models.storage.SearchFilter;

public class UserRelationEvolutionWorker extends Worker {

	private CracUser user;
	private Evaluation evaluation;
	private UserRelationshipDAO userRelationshipDAO;
	
	public UserRelationEvolutionWorker(CracUser user, Evaluation evaluation,
			UserRelationshipDAO userRelationshipDAO) {
		super();
		this.user = user;
		this.evaluation = evaluation;
		this.userRelationshipDAO = userRelationshipDAO;
	}

	public void run(){
		for(UserTaskRel utr : evaluation.getTask().getUserRelationships()){
			UserRelationship ur = userRelationshipDAO.findByC1AndC2(utr.getUser(), user);
			if(ur == null){
				ur = new UserRelationship();
				ur.setC1(user);
				ur.setC2(utr.getUser());
				ur.setLikeValue(0);
				ur.setFriends(false);
			}
			
			double like = ur.getLikeValue();
			//TODO calc new user relation
			//like = Formulars.calcUserRelation(like, evaluation.getLikeValOthers());
			ur.setLikeValue(like);	

			userRelationshipDAO.save(ur);
		}

	}
	
}
