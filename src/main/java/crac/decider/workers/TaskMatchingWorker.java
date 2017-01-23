package crac.decider.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import crac.daos.TaskDAO;
import crac.decider.core.Worker;
import crac.enums.TaskState;
import crac.models.CracUser;
import crac.models.Task;
import crac.models.storage.CompetenceCollectionMatrix;
import crac.models.storage.SearchFilter;
import crac.models.utility.EvaluatedTask;
import crac.notifier.NotificationHelper;

public class TaskMatchingWorker extends Worker {

	
	private CracUser user;
	private TaskDAO taskDAO;

	public TaskMatchingWorker(CracUser u, TaskDAO taskDAO) {
		super();
		this.user = u;
		this.taskDAO = taskDAO;
	}

	public ArrayList<EvaluatedTask> run() {
		
		ArrayList<EvaluatedTask> tasks = new ArrayList<EvaluatedTask>();
		ArrayList<EvaluatedTask> remove = new ArrayList<EvaluatedTask>();
		
		CompetenceCollectionMatrix ccm;
		
		ArrayList<Task> filteredTaskSet = loadFilteredTask();
		
		for (Task t : filteredTaskSet) {
			if (t.getMappedCompetences() != null) {
				if (t.getMappedCompetences().size() != 0) {
					ccm = new CompetenceCollectionMatrix(user, t);
					ccm.print();
					EvaluatedTask et = new EvaluatedTask(t, ccm.calcMatch());
					et.setDoable(ccm.isDoable());
					tasks.add(et);
				}
			}
		}

		
		if (tasks != null) {
			for (EvaluatedTask t : tasks) {
				if (!t.isDoable() || t.getAssessment() == 0) {
					remove.add(t);
				}
			}
			
			for(EvaluatedTask t : remove){
				tasks.remove(t);
			}

			Collections.sort(tasks);
		}

		return tasks;
	}
	
	public String getWorkerId(){
		return super.getWorkerId();
	}
	
	public ArrayList<Task> loadFilteredTask(){
		
		ArrayList<Task> result = new ArrayList<>();
		
		List<Task> found = taskDAO.findByTaskStateNot(TaskState.NOT_PUBLISHED);
		for(Task task : found){
			if(task.isLeaf()){
				result.add(task);
			}
		}
		return result;
	}

}
