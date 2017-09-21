package crac.module.matching.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import crac.enums.TaskState;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;
import crac.module.matching.configuration.MatchingConfiguration;
import crac.module.matching.configuration.UserFilterParameters;
import crac.module.matching.filter.matching.UserRelationFilter;
import crac.module.matching.helpers.CompetenceCollectionMatrix;
import crac.module.matching.helpers.EvaluatedTask;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.matching.superclass.ConcreteFilter;
import crac.module.matching.superclass.Worker;

public class TaskMatchingWorker extends Worker {

	private CracUser user;
	private UserFilterParameters up;

	public TaskMatchingWorker(CracUser u, UserFilterParameters up) {
		this.up = up;
		this.user = u;
		System.out.println("worker created");
	}

	@Override
	public ArrayList<EvaluatedTask> run() {

		ArrayList<EvaluatedTask> tasks = new ArrayList<EvaluatedTask>();
		CompetenceCollectionMatrix ccm;

		// load a filtered amount of tasks
		
		List<Task> taskSet = super.getWf().getTaskDAO().selectMatchableTasksSimple();
		
		//PreMatchingFilters
		
		FilterParameters fp = new FilterParameters();
		fp.setTasksPool(taskSet);
		fp.setUser(user);
		
		for(ConcreteFilter filter : super.getWf().getPmc().getFilters()){
			filter.apply(fp);
		}

		//MatchingFilters

		// load the filters for matrix matching and add user-filters
		FilterConfiguration filters = super.getWf().getMc().clone();
		addUserFilters(filters);

		for (Task t : taskSet) {
			ccm = new CompetenceCollectionMatrix(user, t, (MatchingConfiguration) filters, super.getWf().getCs());
			ccm.print();
			EvaluatedTask et = new EvaluatedTask(t, ccm.calcMatch());
			et.setDoable(ccm.isDoable());
			tasks.add(et);
		}
		
		//PostMatchingFilters
		
		fp = new FilterParameters();
		fp.setEvaluatedTasksPool(tasks);

		for(ConcreteFilter filter : super.getWf().getPomc().getFilters()){
			filter.apply(fp);
		}
		
		//------------------------

		if (tasks != null) {
			//tasks.sort((task1, task2) -> Double.compare(task1.getAssessment(), task2.getAssessment()));
			tasks.sort(Comparator.comparing(EvaluatedTask::getAssessment));
			//Collections.sort(tasks);
		}

		return tasks;
	}

	public void addUserFilters(FilterConfiguration m) {
		if (up.getFriends() == 1) {
			m.addFilter(new UserRelationFilter());
		}
	}

	public String getWorkerId() {
		return super.getWorkerId();
	}

	public ArrayList<Task> loadFilteredTasks() {

		ArrayList<Task> result = new ArrayList<>();

		List<Task> found = super.getWf().getTaskDAO().findByTaskStateNot(TaskState.NOT_PUBLISHED);
		System.out.println("found: " + found.size());
		
		for (Task task : found) {
			if (task.isJoinable()) {
				
				boolean isConnected = false;
				for (UserTaskRel utr : task.getUserRelationships()) {
					if (utr.getUser().getId() == user.getId()) {
						isConnected = true;
					}
				}
				if (!isConnected) {
					result.add(task);
				}
				
			}
		}
		System.out.println("returned: " + result.size());
		return result;
	}

	public void postModifyTasks(ArrayList<EvaluatedTask> tasks) {

		for (EvaluatedTask task : tasks) {
			Task t = task.getTask();

			if (t.getTaskState() == TaskState.STARTED) {

				double mval = 0.6;
				double newval = 0;

				double valAdjust = ((double) t.getAllParticipants().size() / (double) t.getMinAmountOfVolunteers());

				newval = task.getAssessment() * (1 + (1 - valAdjust) * mval);

				task.setAssessment((double) Math.round(newval * 100) / 100);

			}
		}

	}

}
