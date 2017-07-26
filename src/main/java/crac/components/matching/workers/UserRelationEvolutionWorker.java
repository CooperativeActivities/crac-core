package crac.components.matching.workers;

import crac.components.matching.Worker;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;

public class UserRelationEvolutionWorker extends Worker {

	private CracUser user;
	private Evaluation evaluation;
	private UserRelationshipDAO userRelationshipDAO;

	public UserRelationEvolutionWorker(Evaluation evaluation) {
		super();
		this.user = evaluation.getUserTaskRel().getUser();
		this.evaluation = evaluation;
		this.userRelationshipDAO = super.getWf().getUserRelalationshipDAO();
	}

	@Override
	public Object run() {
		for (UserTaskRel utr : evaluation.getUserTaskRel().getTask().getUserRelationships()) {
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

				ur.setLikeValue((double) Math.round(updated * 100) / 100);

				userRelationshipDAO.save(ur);
				System.out.println(ur.getLikeValue());
			}
		}
		return null;
	}
	

}
