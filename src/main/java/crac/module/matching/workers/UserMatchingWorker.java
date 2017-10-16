package crac.module.matching.workers;

import java.util.ArrayList;
import java.util.Collections;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.module.matching.configuration.MatchingConfiguration;
import crac.module.matching.configuration.UserFilterParameters;
import crac.module.matching.filter.matching.UserRelationFilter;
import crac.module.matching.helpers.CompetenceCollectionMatrix;
import crac.module.matching.helpers.EvaluatedUser;
import crac.module.matching.superclass.Worker;

public class UserMatchingWorker extends Worker {

	private Task task;
	private UserFilterParameters up;

	public UserMatchingWorker(Task task, UserFilterParameters up) {
		this.up = up;
		this.task = task;
	}

	@Override
	public ArrayList<EvaluatedUser> run() {

		ArrayList<EvaluatedUser> users = new ArrayList<EvaluatedUser>();
		ArrayList<EvaluatedUser> remove = new ArrayList<EvaluatedUser>();

		CompetenceCollectionMatrix ccm;

		// load the filters for matrix matching
		MatchingConfiguration filters = (MatchingConfiguration) super.getWf().getMc().clone();

		// add user-filters to the global filters
		addUserFilters(filters);

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

	public void addUserFilters(MatchingConfiguration m) {
		if (up.getFriends() == 1) {
			m.addFilter(new UserRelationFilter());
		}
	}

	public String getWorkerId() {
		return super.getWorkerId();
	}

}
