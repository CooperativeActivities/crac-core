package crac.module.matching;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.module.factories.WorkerFactory;
import crac.module.matching.helpers.EvaluatedTask;
import crac.module.matching.helpers.EvaluatedUser;
import crac.module.matching.workers.TaskMatchingWorker;
import crac.module.matching.workers.UserCompetenceRelationEvolutionWorker;
import crac.module.matching.workers.UserMatchingWorker;
import crac.module.matching.workers.UserRelationEvolutionWorker;

@Service
public class Decider {
	
	@Autowired
	private WorkerFactory wf;
	
	public ArrayList<EvaluatedTask> findTasks(CracUser u){
		TaskMatchingWorker w = (TaskMatchingWorker) wf.createWorker(TaskMatchingWorker.class, u);
		ArrayList<EvaluatedTask> list = w.run();
		
		return list;
	}
	
	public ArrayList<EvaluatedUser> findUsers(Task t){
		UserMatchingWorker worker = (UserMatchingWorker) wf.createWorker(UserMatchingWorker.class, t);
		ArrayList<EvaluatedUser> list = worker.run();
		return list;
	}
	
	public void evaluate(Evaluation e){
		UserCompetenceRelationEvolutionWorker w1 = (UserCompetenceRelationEvolutionWorker) wf.createWorker(UserCompetenceRelationEvolutionWorker.class, e);
		UserRelationEvolutionWorker w2 = (UserRelationEvolutionWorker) wf.createWorker(UserRelationEvolutionWorker.class, e);
		w1.run();
		w2.run();
	}
	
}
