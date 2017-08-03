package crac.module.matching;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.module.matching.configuration.UserFilterParameters;
import crac.module.matching.factories.WorkerFactory;
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
	
	public ArrayList<EvaluatedTask> findTasks(CracUser u, UserFilterParameters up){
				
		HashMap<String, Object> params = new HashMap<>();
		params.put("user", u);
		params.put("userFilterParameters", up);
		TaskMatchingWorker w = (TaskMatchingWorker) wf.createWorker(TaskMatchingWorker.class, params);
		ArrayList<EvaluatedTask> list = w.run();
		
		return list;
	}
	
	public ArrayList<EvaluatedUser> findUsers(Task task, UserFilterParameters up){
		HashMap<String, Object> params = new HashMap<>();
		params.put("task", task);
		params.put("userFilterParameters", up);
		UserMatchingWorker worker = (UserMatchingWorker) wf.createWorker(UserMatchingWorker.class, params);
		ArrayList<EvaluatedUser> list = worker.run();
		return list;
	}
	
	public void evaluate(Evaluation evaluation){
		HashMap<String, Object> params = new HashMap<>();
		params.put("evaluation", evaluation);
		UserCompetenceRelationEvolutionWorker w1 = (UserCompetenceRelationEvolutionWorker) wf.createWorker(UserCompetenceRelationEvolutionWorker.class, params);
		UserRelationEvolutionWorker w2 = (UserRelationEvolutionWorker) wf.createWorker(UserRelationEvolutionWorker.class, params);
		w1.run();
		w2.run();
	}
	
}