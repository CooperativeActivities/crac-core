package crac.decider.workers;

import java.util.ArrayList;
import java.util.Collections;

import crac.daos.CracUserDAO;
import crac.decider.core.MatrixFilterConfiguration;
import crac.decider.core.UserFilterParameters;
import crac.decider.core.Worker;
import crac.decider.filter.UserRelationFilter;
import crac.decider.workers.config.GlobalMatrixConfig;
import crac.models.CracUser;
import crac.models.Task;
import crac.models.storage.CompetenceCollectionMatrix;
import crac.models.utility.EvaluatedUser;
import crac.notifier.NotificationHelper;

public class UserMatchingWorker extends Worker {

	private Task task;
	private CracUserDAO userDAO;
	private UserFilterParameters up;

	public UserMatchingWorker(Task task, CracUserDAO userDAO, UserFilterParameters up) {
		super();
		this.up = up;
		this.task = task;
		this.userDAO = userDAO;
	}

	public ArrayList<EvaluatedUser> run() {

		ArrayList<EvaluatedUser> users = new ArrayList<EvaluatedUser>();
		ArrayList<EvaluatedUser> remove = new ArrayList<EvaluatedUser>();

		CompetenceCollectionMatrix ccm;

		// load the filters for matrix matching
		MatrixFilterConfiguration filters = GlobalMatrixConfig.cloneConfiguration();

		// add user-filters to the global filters
		addUserFilters(filters);

		for (CracUser u : userDAO.findAll()) {
			if (u.getCompetenceRelationships() != null) {
				if (u.getCompetenceRelationships().size() != 0) {
					ccm = new CompetenceCollectionMatrix(u, task, filters);
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

	public void addUserFilters(MatrixFilterConfiguration m) {
		if (up.getFriends() == 1) {
			m.addFilter(new UserRelationFilter());
		}
	}

	public String getWorkerId() {
		return super.getWorkerId();
	}

}
