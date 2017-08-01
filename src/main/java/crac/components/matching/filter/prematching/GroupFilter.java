package crac.components.matching.filter.prematching;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import crac.components.matching.CracPreMatchingFilter;
import crac.models.db.entities.CracGroup;
import crac.models.db.entities.Task;
import crac.models.utility.MatchingInformation;

public class GroupFilter extends CracPreMatchingFilter {

	public GroupFilter() {
		super("group-filter");
	}

	@Override
	public List<Task> apply(MatchingInformation mi) {

		List<Task> result = new ArrayList<>();

		for (Task t : mi.getTasks()) {
			Set<CracGroup> gr = t.getRestrictingGroups();
			if (gr != null) {
				if (gr.size() == 0) {
					result.add(t);
				} else {
					for (CracGroup g : t.getRestrictingGroups()) {
						if (g.getEnroledUsers().contains(mi.getUser())) {
							result.add(t);
							break;
						}
					}
				}
			} else {
				result.add(t);
			}
		}
		System.out.println("Applied: "+super.speakString());
		
		return result;
	}

}
