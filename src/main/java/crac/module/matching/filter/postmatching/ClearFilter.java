package crac.module.matching.filter.postmatching;

import java.util.ArrayList;
import java.util.List;

import crac.module.matching.helpers.EvaluatedTask;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

/**
 * This filter removes tasks from the task-pool based on the (competence-matching-) assessment of each task
 * @author David Hondl
 *
 */
public class ClearFilter extends ConcreteFilter {

	public ClearFilter() {
		super("clear-filter");
	}

	@Override
	public void apply(FilterParameters fp) {
		List<EvaluatedTask> list = fp.getEvaluatedTasksPool();
		List<EvaluatedTask> remove = new ArrayList<EvaluatedTask>();
		for (EvaluatedTask t : list) {
			if (!t.isDoable() || t.getAssessment() == 0) {
				remove.add(t);
			}
		}

		for (EvaluatedTask t : remove) {
			list.remove(t);
		}
		System.out.println("Applied: " + super.speakString());
	}

}
