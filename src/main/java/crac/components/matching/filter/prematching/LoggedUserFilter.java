package crac.components.matching.filter.prematching;

import java.util.ArrayList;
import java.util.List;

import crac.components.matching.CracPreMatchingFilter;
import crac.enums.TaskParticipationType;
import crac.models.db.entities.CracGroup;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;
import crac.models.utility.MatchingInformation;

public class LoggedUserFilter extends CracPreMatchingFilter {

	public LoggedUserFilter() {
		super("logged-user-filter");
	}

	@Override
	public List<Task> apply(MatchingInformation mi) {
		
		List<Task> result = new ArrayList<>();
		
		for (Task task : mi.getTasks()) {				
				boolean isConnected = false;
				for (UserTaskRel utr : task.getUserRelationships()) {
					if (utr.getUser().getId() == mi.getUser().getId() && utr.getParticipationType() == TaskParticipationType.PARTICIPATING) {
						isConnected = true;
					}
				}
				if (!isConnected) {
					result.add(task);
				}				
		}

		
		return result;
	}

}