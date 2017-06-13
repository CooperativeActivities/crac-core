package crac.components.matching.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import crac.components.matching.Worker;
import crac.components.matching.configuration.FilterConfiguration;
import crac.components.matching.configuration.GlobalMatrixFilterConfig;
import crac.components.matching.configuration.UserFilterParameters;
import crac.components.matching.filter.UserRelationFilter;
import crac.components.utility.DataAccess;
import crac.enums.TaskState;
import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;
import crac.models.storage.CompetenceCollectionMatrix;
import crac.models.utility.EvaluatedTask;

public class TaskMatchingWorker extends Worker {

	private CracUser user;
	private TaskDAO taskDAO;
	private UserFilterParameters up;

	public TaskMatchingWorker(CracUser u, UserFilterParameters up) {
		super();
		this.up = up;
		this.user = u;
		this.taskDAO = DataAccess.getRepo(TaskDAO.class);
	}

	public ArrayList<EvaluatedTask> run() {

		ArrayList<EvaluatedTask> tasks = new ArrayList<EvaluatedTask>();
		ArrayList<EvaluatedTask> remove = new ArrayList<EvaluatedTask>();
		CompetenceCollectionMatrix ccm;

		// load a filtered amount of tasks
		ArrayList<Task> filteredTaskSet = loadFilteredTasks();

		// load the filters for matrix matching
		FilterConfiguration filters = GlobalMatrixFilterConfig.cloneConfiguration();

		// add user-filters to the global filters
		addUserFilters(filters);

		for (Task t : filteredTaskSet) {
			ccm = new CompetenceCollectionMatrix(user, t, filters);
			ccm.print();
			EvaluatedTask et = new EvaluatedTask(t, ccm.calcMatch());
			et.setDoable(ccm.isDoable());
			tasks.add(et);
		}

		if (tasks != null) {
			for (EvaluatedTask t : tasks) {
				System.out.println(t.getAssessment()+ " ASSASSMENT, "+t.getTask().getId()+" THE ID");
				if (!t.isDoable() || t.getAssessment() == 0) {
					remove.add(t);
				}
			}

			for (EvaluatedTask t : remove) {
				tasks.remove(t);
			}

			postModifyTasks(tasks);
			Collections.sort(tasks);
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

		List<Task> found = taskDAO.findByTaskStateNot(TaskState.NOT_PUBLISHED);
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
