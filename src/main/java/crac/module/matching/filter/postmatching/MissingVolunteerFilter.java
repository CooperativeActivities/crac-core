package crac.module.matching.filter.postmatching;

import java.util.List;

import crac.enums.TaskState;
import crac.models.db.entities.Task;
import crac.module.matching.helpers.EvaluatedTask;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

public class MissingVolunteerFilter extends ConcreteFilter {

	public MissingVolunteerFilter() {
		super("missing-volunteer-filter");
	}

	@Override
	public void apply(FilterParameters fp) {
		List<EvaluatedTask> list = fp.getEvaluatedTasksPool();
		for (EvaluatedTask task : list) {
			Task t = task.getTask();

			if (t.getTaskState() == TaskState.STARTED) {

				double modifyVal = 0.6;
				double valAdjust = 1;

				if ((double) t.getMinAmountOfVolunteers() != 0) {
					valAdjust = ((double) t.getAllParticipants().size() / (double) t.getMinAmountOfVolunteers());
				}

				double newval = task.getAssessment() * (1 + (1 - valAdjust) * modifyVal);

				task.setAssessment((double) Math.round(newval * 100) / 100);

			}
		}
		System.out.println("Applied: " + super.speakString());
	}

}