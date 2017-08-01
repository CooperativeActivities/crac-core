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

		return list;
	}

}
