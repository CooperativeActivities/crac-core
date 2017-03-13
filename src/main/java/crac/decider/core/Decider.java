package crac.decider.core;

import java.util.ArrayList;

import crac.decider.workers.TaskMatchingWorker;
import crac.decider.workers.UserCompetenceRelationEvolutionWorker;
import crac.decider.workers.UserMatchingWorker;
import crac.decider.workers.UserRelationEvolutionWorker;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.utility.EvaluatedTask;
import crac.models.utility.EvaluatedUser;

public class Decider {
	
	public ArrayList<EvaluatedTask> findTasks(CracUser u, UserFilterParameters up, TaskDAO taskDAO){
		
		TaskMatchingWorker worker = new TaskMatchingWorker(u, taskDAO, up);
		ArrayList<EvaluatedTask> list = worker.run();
		
		return list;
	}
	
	public ArrayList<EvaluatedUser> findUsers(Task task, UserFilterParameters up, CracUserDAO userDAO){
		UserMatchingWorker worker = new UserMatchingWorker(task, userDAO, up);
		ArrayList<EvaluatedUser> list = worker.run();
		return list;
	}
	
	public void evaluateUsers(Evaluation e, UserRelationshipDAO userRelationshipDAO){
		UserRelationEvolutionWorker w = new UserRelationEvolutionWorker(e, userRelationshipDAO);
		w.run();
	}
	
	public void evaluateTask(Evaluation e, UserCompetenceRelDAO userCompetenceRelDAO){
		UserCompetenceRelationEvolutionWorker w = new UserCompetenceRelationEvolutionWorker(e, userCompetenceRelDAO);
		w.run();
	}

}
