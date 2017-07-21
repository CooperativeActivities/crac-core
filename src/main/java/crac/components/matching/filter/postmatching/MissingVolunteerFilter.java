package crac.components.matching.filter.postmatching;

import java.util.ArrayList;
import java.util.List;

import crac.components.matching.CracPostMatchingFilter;
import crac.components.matching.CracPreMatchingFilter;
import crac.enums.TaskState;
import crac.models.db.entities.CracGroup;
import crac.models.db.entities.Task;
import crac.models.utility.EvaluatedTask;
import crac.models.utility.MatchingInformation;

public class MissingVolunteerFilter extends CracPostMatchingFilter {

	public MissingVolunteerFilter() {
		super("missing-volunteer-filter");
	}

	@Override
	public ArrayList<EvaluatedTask> apply(ArrayList<EvaluatedTask> list) {
		for (EvaluatedTask task : list) {
			Task t = task.getTask();

			if (t.getTaskState() == TaskState.STARTED) {

				double mval = 0.6;
				double newval = 0;

				double valAdjust = ((double) t.getAllParticipants().size() / (double) t.getMinAmountOfVolunteers());

				newval = task.getAssessment() * (1 + (1 - valAdjust) * mval);

				task.setAssessment((double) Math.round(newval * 100) / 100);

			}
		}
		return list;
	}

}
