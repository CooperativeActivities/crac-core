package crac.deciderCore;

import java.util.ArrayList;

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
		
		CompetenceCollectionMatrix ccm;
		
		for(Task t : taskDAO.findAll()){
			ccm = new CompetenceCollectionMatrix(user, t);
			ccm.print();
		}
		
		return new ArrayList<EvaluatedTask>();
	}
	
}
