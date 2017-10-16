package crac.module.matching.workers;

import java.util.ArrayList;
import java.util.Collections;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.module.matching.configuration.MatchingConfiguration;
import crac.module.matching.helpers.CompetenceCollectionMatrix;
import crac.module.matching.helpers.EvaluatedUser;
import crac.module.matching.superclass.Worker;

public class UserMatchingWorker extends Worker {

	private Task task;

	@Override
	public void injectParam(Object param) {
		this.task = (Task) param;
	}

	public UserMatchingWorker() {
	}

	@Override
	public ArrayList<EvaluatedUser> run() {

		ArrayList<EvaluatedUser> users = new ArrayList<EvaluatedUser>();
		ArrayList<EvaluatedUser> remove = new ArrayList<EvaluatedUser>();

		CompetenceCollectionMatrix ccm;

		// load the filters for matrix matching
		MatchingConfiguration filters = (MatchingConfiguration) super.getWf().getMc().clone();

		for (CracUser u : super.getWf().getUserDAO().findAll()) {
			if (u.getCompetenceRelationships() != null) {
				if (u.getCompetenceRelationships().size() != 0) {
					ccm = new CompetenceCollectionMatrix(u, task, filters, super.getWf().getCs());
					ccm.print();
					EvaluatedUser eu = new EvaluatedUser(u, ccm.calcMatch());
					eu.setDoable(ccm.isDoable());
					users.add(eu);
				}
			}
		}

		if (users != null) {
			for (EvaluatedUser u : users) {
				if (!u.isDoable() || u.getAssessment() == 0) {
					remove.add(u);
				}
			}

			for (EvaluatedUser u : remove) {
				users.remove(u);
			}

			Collections.sort(users);
		}

		return users;
	}

}
