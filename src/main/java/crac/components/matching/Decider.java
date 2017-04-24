package crac.components.matching;

import java.util.ArrayList;

import crac.components.matching.configuration.UserFilterParameters;
import crac.components.matching.workers.TaskMatchingWorker;
import crac.components.matching.workers.UserCompetenceRelationEvolutionWorker;
import crac.components.matching.workers.UserMatchingWorker;
import crac.components.matching.workers.UserRelationEvolutionWorker;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.utility.EvaluatedTask;
import crac.models.utility.EvaluatedUser;

public class Decider {
	
	public ArrayList<EvaluatedTask> findTasks(CracUser u, UserFilterParameters up){
		
		TaskMatchingWorker worker = new TaskMatchingWorker(u, up);
		ArrayList<EvaluatedTask> list = worker.run();
		
		return list;
	}
	
	public ArrayList<EvaluatedUser> findUsers(Task task, UserFilterParameters up){
		UserMatchingWorker worker = new UserMatchingWorker(task, up);
		ArrayList<EvaluatedUser> list = worker.run();
		return list;
	}
	
	public void evaluateUsers(Evaluation e){
		UserRelationEvolutionWorker w = new UserRelationEvolutionWorker(e);
		w.run();
	}
	
	public void evaluateTask(Evaluation e){
		UserCompetenceRelationEvolutionWorker w = new UserCompetenceRelationEvolutionWorker(e);
		w.run();
	}

}
