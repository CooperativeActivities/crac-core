package crac.components.matching.workers;

import java.util.ArrayList;
import java.util.Collections;

import crac.components.matching.Worker;
import crac.components.matching.configuration.MatchingConfiguration;
import crac.components.matching.configuration.UserFilterParameters;
import crac.components.matching.filter.matching.UserRelationFilter;
import crac.components.utility.DataAccess;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.storage.CompetenceCollectionMatrix;
import crac.models.utility.EvaluatedUser;

public class UserMatchingWorker extends Worker {

	private Task task;
	private CracUserDAO userDAO;
	private UserFilterParameters up;
	private MatchingConfiguration mc;

	public UserMatchingWorker(Task task, UserFilterParameters up, MatchingConfiguration mc) {
		super();
		this.up = up;
		this.task = task;
		this.userDAO = DataAccess.getRepo(CracUserDAO.class);
		this.mc = mc;
	}

	@Override
	public ArrayList<EvaluatedUser> run() {

		ArrayList<EvaluatedUser> users = new ArrayList<EvaluatedUser>();
		ArrayList<EvaluatedUser> remove = new ArrayList<EvaluatedUser>();

		CompetenceCollectionMatrix ccm;

		// load the filters for matrix matching
		MatchingConfiguration filters = (MatchingConfiguration) mc.clone();

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

	public void addUserFilters(MatchingConfiguration m) {
		if (up.getFriends() == 1) {
			m.addFilter(new UserRelationFilter());
		}
	}

	public String getWorkerId() {
		return super.getWorkerId();
	}

}
