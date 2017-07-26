package crac.components.matching.workers;

import crac.components.matching.Worker;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;

public class UserCompetenceRelationEvolutionWorker extends Worker {

	private CracUser user;
	private Task task;
	private Evaluation evaluation;
	private UserCompetenceRelDAO userCompetenceRelDAO;

	public UserCompetenceRelationEvolutionWorker(Evaluation evaluation) {
		super();
		this.user = evaluation.getUserTaskRel().getUser();
		this.task = evaluation.getUserTaskRel().getTask();
		this.evaluation = evaluation;
		this.userCompetenceRelDAO = super.getWf().getUserCompetenceRelDAO();
	}

	@Override
	public Object run() {
		for (CompetenceTaskRel ctr : task.getMappedCompetences()) {
			UserCompetenceRel ucr = userCompetenceRelDAO.findByUserAndCompetence(user, ctr.getCompetence());

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
			userCompetenceRelDAO.save(ucr);
		}
		return null;
	}

}
