package crac.utility;

import java.util.Calendar;
import java.util.Set;

import crac.daos.TaskDAO;
import crac.enums.TaskState;
import crac.models.CracUser;
import crac.models.Task;
import crac.models.relation.UserTaskRel;

//0 is undefined -> unknown bug, skipped all the code
//1 means that a very important rule for changing the state is not met
//2 means that a minor rule for changing the state is not met
//3 means that everything is ok and the state has changed

public class TaskStateHandler {
/*
	public static int publish(Task t) {

		if (t.getTaskState() == TaskState.NOT_PUBLISHED && t.readyToPublish()) {
			if (t.getAmountOfVolunteers() > 0 && !t.getDescription().equals("") && t.getStartTime() != null
					&& t.getEndTime() != null && !t.getMappedCompetences().isEmpty() && !t.getLocation().equals("")) {
				t.setTaskState(TaskState.PUBLISHED);
				return 3;
			} else {
				return 2;
			}
		} else {
			return 1;
		}
	}
	
	public static boolean checkStartAllowance(Task t){
		return t.getStartTime().getTimeInMillis() < Calendar.getInstance().getTimeInMillis();
	}

	public static int start(Task t) {

		if (t.getTaskState() == TaskState.PUBLISHED) {
			if(checkStartAllowance(t)){
				t.setTaskState(TaskState.STARTED);
				return 3;
			}else{
				return 2;
			}
		} else {
			return 1;
		}

	}

	public static int complete(Task t) {
		if (t.getTaskState() == TaskState.STARTED) {
			Set<UserTaskRel> ur = t.getUserRelationships();
			boolean usersDone = true;
			for (UserTaskRel u : ur) {
				if (!u.isCompleted()) {
					usersDone = false;
				}
			}
			if (usersDone) {
				t.setTaskState(TaskState.COMPLETED);
				return 3;
			} else {
				return 2;
			}
		} else {
			return 1;
		}
	}

	public static int forceComplete(TaskDAO taskDAO, Task t, CracUser u) {
		if (t.getTaskState() == TaskState.STARTED) {
			if (t.getAllLeaders().contains(u)) {
				t.setTreeComplete(taskDAO);
				return 3;
			} else {
				return 2;
			}
		} else {
			return 1;
		}
	}
*/
}
