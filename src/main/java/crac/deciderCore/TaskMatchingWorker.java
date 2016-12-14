package crac.deciderCore;

import java.util.ArrayList;
import java.util.Collections;

import crac.competenceGraph.CompetenceCollectionMatrix;
import crac.daos.TaskDAO;
import crac.models.CracUser;
import crac.models.Task;
import crac.utilityModels.EvaluatedTask;

public class TaskMatchingWorker extends Worker {
	
	private CracUser user;
	private TaskDAO taskDAO;
	
	public TaskMatchingWorker(CracUser u, TaskDAO taskDAO){
		this.user = u;
		this.taskDAO = taskDAO;
	}

	public ArrayList<EvaluatedTask> run(){
		
		ArrayList<EvaluatedTask> tasks = new ArrayList<EvaluatedTask>();
		
		CompetenceCollectionMatrix ccm;
		
		for(Task t : taskDAO.findAll()){
			ccm = new CompetenceCollectionMatrix(user, t);
			ccm.print();
			tasks.add(new EvaluatedTask(t, ccm.calcMatch()));
		}
		
		for(EvaluatedTask t : tasks){
			if(!t.isDoable() || t.getAssessment() == 0){
				tasks.remove(t);
			}
		}
		
		Collections.sort(tasks);
		
		return tasks;
	}
	
}
