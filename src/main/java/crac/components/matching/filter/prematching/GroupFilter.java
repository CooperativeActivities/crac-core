package crac.components.matching.filter.prematching;

import java.util.ArrayList;
import java.util.List;

import crac.components.matching.CracFilter;
import crac.components.matching.CracPreMatchingFilter;
import crac.models.db.entities.Task;

public class GroupFilter extends CracPreMatchingFilter {

	public GroupFilter() {
		super("group-filter");
	}

	@Override
	public List<Task> apply(List<Task> tasks) {
		
		System.out.println("group-filter applied");
		
		return tasks;
	}

}
