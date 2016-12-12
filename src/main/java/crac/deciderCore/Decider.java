package crac.deciderCore;

import java.util.ArrayList;

import crac.daos.TaskDAO;
import crac.models.CracUser;
import crac.utilityModels.EvaluatedTask;

public class Decider {
	
	public ArrayList<EvaluatedTask> findTasks(CracUser u, TaskDAO taskDAO, DeciderParameters p){
		
		TaskMatchingWorker worker = new TaskMatchingWorker(u, taskDAO);
		
		ArrayList<EvaluatedTask> list = worker.run();
		
		return list;
	}

}
