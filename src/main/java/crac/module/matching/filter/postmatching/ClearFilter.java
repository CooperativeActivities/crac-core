package crac.module.matching.filter.postmatching;

import java.util.ArrayList;

import crac.models.utility.EvaluatedTask;
import crac.module.matching.superclass.CracPostMatchingFilter;

public class ClearFilter extends CracPostMatchingFilter {

	public ClearFilter() {
		super("clear-filter");
	}

	@Override
	public ArrayList<EvaluatedTask> apply(ArrayList<EvaluatedTask> list) {
		ArrayList<EvaluatedTask> remove = new ArrayList<EvaluatedTask>();
		for (EvaluatedTask t : list) {
			if (!t.isDoable() || t.getAssessment() == 0) {
				remove.add(t);
			}
		}

		for (EvaluatedTask t : remove) {
			list.remove(t);
		}
		System.out.println("Applied: "+super.speakString());
		
		return list;
	}

}
