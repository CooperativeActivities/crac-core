package crac.module.matching.workers;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;
import crac.module.matching.superclass.Worker;

/**
 * Worker, that updates the values stored in the user-relationship based on a given evaluation
 * @author David Hondl
 *
 */
public class UserRelationEvolutionWorker extends Worker {

	private CracUser user;
	private Evaluation evaluation;

    public UserRelationEvolutionWorker() {

    }
    
    @Override
    public void injectParam(Object param) {
        this.evaluation = (Evaluation) param;
        this.user = evaluation.getUserTaskRel().getUser();
	}

    /**
     * The run-method uses the values of the evaluation, to update the user-relationship
     */
	@Override
	public Object run() {
		for (UserTaskRel utr : evaluation.getUserTaskRel().getTask().getUserRelationships()) {
			if (user.getId() != utr.getUser().getId()) {
				UserRelationship ur = super.getWf().getUserRelalationshipDAO().findByC1AndC2(utr.getUser(), user);
				if (ur == null) {
					ur = super.getWf().getUserRelalationshipDAO().findByC1AndC2(user, utr.getUser());
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

				super.getWf().getUserRelalationshipDAO().save(ur);
				System.out.println(ur.getLikeValue());
			}
		}
		return null;
	}
	

}
