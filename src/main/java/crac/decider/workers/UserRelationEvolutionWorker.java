package crac.decider.workers;

import crac.daos.TaskDAO;
import crac.daos.UserRelationshipDAO;
import crac.daos.UserTaskRelDAO;
import crac.decider.core.UserFilterParameters;
import crac.decider.core.Worker;
import crac.models.CracUser;
import crac.models.Evaluation;
import crac.models.Task;
import crac.models.relation.UserRelationship;
import crac.models.relation.UserTaskRel;

public class UserRelationEvolutionWorker extends Worker {

	private CracUser user;
	private Evaluation evaluation;
	private UserRelationshipDAO userRelationshipDAO;

	public UserRelationEvolutionWorker(Evaluation evaluation, UserRelationshipDAO userRelationshipDAO) {
		super();
		this.user = evaluation.getUser();
		this.evaluation = evaluation;
		this.userRelationshipDAO = userRelationshipDAO;
	}

	public void run() {
		for (UserTaskRel utr : evaluation.getTask().getUserRelationships()) {
			if (user.getId() != utr.getUser().getId()) {
				UserRelationship ur = userRelationshipDAO.findByC1AndC2(utr.getUser(), user);
				if (ur == null) {
					ur = userRelationshipDAO.findByC1AndC2(user, utr.getUser());
					if (ur == null) {
						ur = new UserRelationship();
						ur.setC1(user);
						ur.setC2(utr.getUser());
						ur.setLikeValue(0);
						ur.setFriends(false);
					}
				}

				double like = ur.getLikeValue();

				double updated = like;
				updated += evaluation.getLikeValOthers() / 4;

				if (updated > 1) {
					updated = 1;
				} else if (updated < -1) {
					updated = -1;
				}

				ur.setLikeValue(updated);

				userRelationshipDAO.save(ur);
				System.out.println(ur.getLikeValue());
			}
		}
	}

}
