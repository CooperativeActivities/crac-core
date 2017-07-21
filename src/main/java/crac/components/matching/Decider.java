package crac.components.matching;

import java.util.ArrayList;

import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import crac.components.matching.configuration.UserFilterParameters;
import crac.components.matching.factories.WorkerFactory;
import crac.components.matching.workers.TaskMatchingWorker;
import crac.components.matching.workers.UserCompetenceRelationEvolutionWorker;
import crac.components.matching.workers.UserMatchingWorker;
import crac.components.matching.workers.UserRelationEvolutionWorker;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.utility.EvaluatedTask;
import crac.models.utility.EvaluatedUser;

@Service
public class Decider {
	
	@Autowired
	private WorkerFactory wf;
	
	public ArrayList<EvaluatedTask> findTasks(CracUser u, UserFilterParameters up){
				
		TaskMatchingWorker worker = wf.createTmWorker(u, up);
		ArrayList<EvaluatedTask> list = worker.run();
		
		return list;
	}
	
	public ArrayList<EvaluatedUser> findUsers(Task task, UserFilterParameters up){
		UserMatchingWorker worker = wf.createUmWorker(task, up);
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
