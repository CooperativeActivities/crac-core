package crac.module.matching;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.module.matching.configuration.UserFilterParameters;
import crac.module.matching.factories.NLPWorkerFactory;
import crac.module.matching.factories.WorkerFactory;
import crac.module.matching.helpers.EvaluatedTask;
import crac.module.matching.helpers.EvaluatedUser;
import crac.module.matching.workers.TaskCompetenceMatchingWorker;
import crac.module.matching.workers.TaskMatchingWorker;
import crac.module.matching.workers.UserCompetenceRelationEvolutionWorker;
import crac.module.matching.workers.UserMatchingWorker;
import crac.module.matching.workers.UserRelationEvolutionWorker;
import crac.module.storage.CompetenceStorage;

@Service
public class Decider {
	
	@Autowired
	private WorkerFactory wf;
	
	@Autowired
	private NLPWorkerFactory nlpWf;
	
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
	
	public ArrayList<Competence> findCompetences(Task task){
		HashMap<String, Object> params = new HashMap<>();
		params.put("task", task);
		TaskCompetenceMatchingWorker w = (TaskCompetenceMatchingWorker) nlpWf.createWorker(TaskCompetenceMatchingWorker.class, params);
		ArrayList<Competence> list = w.run(); 
		return list; 
	}
	
}
