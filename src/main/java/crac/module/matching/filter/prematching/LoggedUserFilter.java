package crac.module.matching.filter.prematching;

import java.util.ArrayList;
import java.util.List;

import crac.enums.TaskParticipationType;
import crac.models.db.entities.CracGroup;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;
import crac.models.utility.MatchingInformation;
import crac.module.matching.superclass.CracPreMatchingFilter;

/**
 * Filters a pool of tasks for the ones that the logged in user is not already related to
 * @author David
 *
 */
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

		System.out.println("Applied: "+super.speakString());
		
		return result;
	}

}
