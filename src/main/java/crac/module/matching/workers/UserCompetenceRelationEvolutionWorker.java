package crac.module.matching.workers;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import crac.module.matching.superclass.Worker;

/**
 * Worker, that updates the values stored in the user-competence-relationship based on a given evaluation
 * @author David Hondl
 *
 */
public class UserCompetenceRelationEvolutionWorker extends Worker {

	private CracUser user;
	private Task task;
	private Evaluation evaluation;

    public UserCompetenceRelationEvolutionWorker() {
    }
    
    @Override
    public void injectParam(Object param) {
        this.evaluation = (Evaluation) evaluation;
        this.user = evaluation.getUserTaskRel().getUser();
        this.task = evaluation.getUserTaskRel().getTask();
    }

    /**
     * The run-method uses the values of the evaluation, to update the user-competence-relationship
     */
	@Override
	public Object run() {
		for (CompetenceTaskRel ctr : task.getMappedCompetences()) {
			UserCompetenceRel ucr = super.getWf().getUserCompetenceRelDAO().findByUserAndCompetence(user, ctr.getCompetence());

			if (ucr == null) {
				ucr = new UserCompetenceRel();
				ucr.setCompetence(ctr.getCompetence());
				ucr.setUser(user);
				ucr.setLikeValue(0);
				ucr.setProficiencyValue(0);
			}

			int likeValue = ucr.getLikeValue();
			int profValue = ucr.getProficiencyValue();
			likeValue += (evaluation.getLikeValTask() / 4) * 100;
			profValue += 10;
			if (likeValue > 100) {
				likeValue = 100;
			} else if (likeValue < -100) {
				likeValue = -100;
			}
			if (profValue > 100) {
				profValue = 100;
			} else if (profValue < 0) {
				profValue = 0;
			}

			ucr.setLikeValue(likeValue);
			ucr.setProficiencyValue(profValue);
			super.getWf().getUserCompetenceRelDAO().save(ucr);
		}
		return null;
	}

}
