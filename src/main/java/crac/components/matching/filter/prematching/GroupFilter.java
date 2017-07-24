package crac.components.matching.filter.prematching;

import java.util.ArrayList;
import java.util.List;

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
		
		for(Task t : mi.getTasks()){
			for(CracGroup g : t.getRestrictingGroups()){
				if(g.getEnroledUsers().contains(mi.getUser())){
					result.add(t);
					break;
				}
			}
		}
		
		return result;
	}

}
