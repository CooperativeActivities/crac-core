package crac.module.matching;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import crac.models.db.entities.Competence;
import crac.models.db.entities.CompetenceArea;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.module.factories.NLPWorkerFactory;
import crac.module.factories.WorkerFactory;
import crac.module.matching.helpers.EvaluatedTask;
import crac.module.matching.helpers.EvaluatedUser;
import crac.module.matching.workers.TaskCompetenceAreaMatchingWorker;
import crac.module.matching.workers.TaskCompetenceMatchingWorker;
import crac.module.matching.workers.TaskMatchingWorker;
import crac.module.matching.workers.UserCompetenceRelationEvolutionWorker;
import crac.module.matching.workers.UserMatchingWorker;
import crac.module.matching.workers.UserRelationEvolutionWorker;

/**
 * The decider-service serves as layer between the controllers and the instanced workers
 * @author David Hondl
 *
 */
@Service
public class Decider {
	
	@Autowired
	private WorkerFactory wf;
	/*
	@Autowired
	private NLPWorkerFactory nlpWf;
	*/
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
	/*
	public ArrayList<Competence> findCompetences(Task t){
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("task", t);
		param.put("rules", "crac/module/nlp/resources/competence_extraction_rules.txt");
		TaskCompetenceMatchingWorker w = (TaskCompetenceMatchingWorker) nlpWf.createWorker(TaskCompetenceMatchingWorker.class, param);
		ArrayList<Competence> list = w.run(); 
		return list; 
	}
	
	public ArrayList<CompetenceArea> findCompetenceAreas(Task t){
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("task", t);
		param.put("rules", "crac/module/nlp/resources/competence_extraction_rules.txt");
		TaskCompetenceAreaMatchingWorker w = (TaskCompetenceAreaMatchingWorker) nlpWf.createWorker(TaskCompetenceAreaMatchingWorker.class, param);
		ArrayList<CompetenceArea> list = w.run(); 
		return list;
	}*/
}
