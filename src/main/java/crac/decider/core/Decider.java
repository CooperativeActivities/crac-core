package crac.decider.core;

import java.util.ArrayList;

import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.decider.workers.TaskMatchingWorker;
import crac.decider.workers.UserMatchingWorker;
import crac.models.CracUser;
import crac.models.Task;
import crac.models.utility.EvaluatedTask;
import crac.models.utility.EvaluatedUser;

public class Decider {
	
	public ArrayList<EvaluatedTask> findTasks(CracUser u, TaskDAO taskDAO, DeciderParameters p){
		
		TaskMatchingWorker worker = new TaskMatchingWorker(u, taskDAO);
		
		ArrayList<EvaluatedTask> list = worker.run();
		
		return list;
	}
	
	public ArrayList<EvaluatedUser> findUsers(Task task, CracUserDAO userDAO, DeciderParameters p){
		UserMatchingWorker worker = new UserMatchingWorker(task, userDAO);
		ArrayList<EvaluatedUser> list = worker.run();
		return list;
	}

}
