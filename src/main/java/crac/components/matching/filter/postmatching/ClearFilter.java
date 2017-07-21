package crac.components.matching.filter.postmatching;

import java.util.ArrayList;

import crac.components.matching.CracPostMatchingFilter;
import crac.models.utility.EvaluatedTask;

public class ClearFilter extends CracPostMatchingFilter {

	public ClearFilter() {
		super("clear-filter");
	}

	@Override
	public ArrayList<EvaluatedTask> apply(ArrayList<EvaluatedTask> list) {
		ArrayList<EvaluatedTask> remove = new ArrayList<EvaluatedTask>();
		for (EvaluatedTask t : list) {
			System.out.println(t.getAssessment() + " ASSASSMENT, " + t.getTask().getId() + " THE ID");
			if (!t.isDoable() || t.getAssessment() == 0) {
				remove.add(t);
			}
		}

		for (EvaluatedTask t : remove) {
			list.remove(t);
		}
		return list;
	}

}
