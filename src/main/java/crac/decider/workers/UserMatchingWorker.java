package crac.decider.workers;

import java.util.ArrayList;
import java.util.Collections;

import crac.daos.CracUserDAO;
import crac.decider.core.Worker;
import crac.models.CracUser;
import crac.models.Task;
import crac.models.storage.CompetenceCollectionMatrix;
import crac.models.storage.SearchFilter;
import crac.models.utility.EvaluatedUser;
import crac.notifier.NotificationHelper;

public class UserMatchingWorker extends Worker {

	private Task task;
	private CracUserDAO userDAO;

	public UserMatchingWorker(Task task, CracUserDAO userDAO) {
		this.task = task;
		this.userDAO = userDAO;
	}

	public ArrayList<EvaluatedUser> run() {

		ArrayList<EvaluatedUser> users = new ArrayList<EvaluatedUser>();
		ArrayList<EvaluatedUser> remove = new ArrayList<EvaluatedUser>();
		
		CompetenceCollectionMatrix ccm;

		for (CracUser u : userDAO.findAll()) {
			if (u.getCompetenceRelationships() != null) {
				if (u.getCompetenceRelationships().size() != 0) {
					ccm = new CompetenceCollectionMatrix(u, task);
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
			
			for(EvaluatedUser u : remove){
				users.remove(u);
			}

			Collections.sort(users);
		}

		return users;
	}
	
	public String getWorkerId(){
		return super.getWorkerId();
	}

}
