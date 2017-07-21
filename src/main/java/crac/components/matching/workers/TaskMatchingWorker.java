package crac.components.matching.workers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import crac.components.matching.CracPostMatchingFilter;
import crac.components.matching.CracPreMatchingFilter;
import crac.components.matching.Worker;
import crac.components.matching.configuration.MatchingConfiguration;
import crac.components.matching.configuration.PostMatchingConfiguration;
import crac.components.matching.configuration.PreMatchingConfiguration;
import crac.components.matching.configuration.UserFilterParameters;
import crac.components.matching.filter.matching.UserRelationFilter;
import crac.components.matching.interfaces.FilterConfiguration;
import crac.components.utility.DataAccess;
import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;
import crac.models.storage.CompetenceCollectionMatrix;
import crac.models.utility.EvaluatedTask;
import crac.models.utility.MatchingInformation;

public class TaskMatchingWorker extends Worker {

	private CracUser user;
	private TaskDAO taskDAO;
	private UserFilterParameters up;
	private PreMatchingConfiguration pmc;
	private MatchingConfiguration mc;
	private PostMatchingConfiguration pomc;

	public TaskMatchingWorker(CracUser u, UserFilterParameters up, PreMatchingConfiguration pmc, MatchingConfiguration mc, PostMatchingConfiguration pomc) {
		this.up = up;
		this.user = u;
		this.taskDAO = DataAccess.getRepo(TaskDAO.class);
		this.pmc = pmc;
		this.mc = mc;
		this.pomc = pomc;
		System.out.println("worker created");
	}

	public ArrayList<EvaluatedTask> run() {

		ArrayList<EvaluatedTask> tasks = new ArrayList<EvaluatedTask>();
		CompetenceCollectionMatrix ccm;

		// load a filtered amount of tasks
		
		List<Task> taskSet = taskDAO.selectMatchableTasks(TaskState.PUBLISHED, TaskState.STARTED, TaskType.WORKABLE, TaskType.SHIFT, user);
		
		//PreMatchingFilters
		
		MatchingInformation mi = new MatchingInformation(taskSet, user);

		for(CracPreMatchingFilter filter : pmc.getFilters()){
			taskSet = filter.apply(mi);
		}
		
		//MatchingFilters

		// load the filters for matrix matching and add user-filters
		FilterConfiguration filters = mc.clone();
		addUserFilters(filters);

		for (Task t : taskSet) {
			ccm = new CompetenceCollectionMatrix(user, t, (MatchingConfiguration) filters);
			ccm.print();
			EvaluatedTask et = new EvaluatedTask(t, ccm.calcMatch());
			et.setDoable(ccm.isDoable());
			tasks.add(et);
		}
		
		//PostMatchingFilters
		
		for(CracPostMatchingFilter filter : pomc.getFilters()){
			tasks = filter.apply(tasks);
		}
		
		//------------------------

		if (tasks != null) {
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
