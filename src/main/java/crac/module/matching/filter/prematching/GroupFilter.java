package crac.module.matching.filter.prematching;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import crac.models.db.entities.CracGroup;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

public class GroupFilter extends ConcreteFilter {

	public GroupFilter() {
		super("group-filter");
	}

	@Override
	public void apply(FilterParameters fp) {

		CracUser u = fp.getUser();	
		List<Task> result = new ArrayList<>();

		for (Task t : fp.getTasksPool()) {
			Set<CracGroup> gr = t.getRestrictingGroups();
			if (gr != null) {
				if (gr.size() == 0) {
					result.add(t);
				} else {
					for (CracGroup g : t.getRestrictingGroups()) {
						if (g.getEnroledUsers().contains(u)) {
							result.add(t);
							break;
						}
					}
				}
			} else {
				result.add(t);
			}
		}
		fp.setTasksPool(result);
		System.out.println("Applied: "+super.speakString());
	}

}
