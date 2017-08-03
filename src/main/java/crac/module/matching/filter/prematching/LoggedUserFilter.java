package crac.module.matching.filter.prematching;

import java.util.ArrayList;
import java.util.List;

import crac.enums.TaskParticipationType;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

/**
 * Filters a pool of tasks for the ones that the logged in user is not already related to
 * @author David
 *
 */
public class LoggedUserFilter extends ConcreteFilter {

	public LoggedUserFilter() {
		super("logged-user-filter");
	}

	@Override
	public void apply(FilterParameters fp) {
		
		CracUser u = fp.getUser();	
		List<Task> result = new ArrayList<>();
		
		for (Task task : fp.getTasksPool()) {				
				boolean isConnected = false;
				for (UserTaskRel utr : task.getUserRelationships()) {
					if (utr.getUser().getId() == u.getId() && utr.getParticipationType() == TaskParticipationType.PARTICIPATING) {
						isConnected = true;
					}
				}
				if (!isConnected) {
					result.add(task);
				}				
		}
		fp.setTasksPool(result);

		System.out.println("Applied: "+super.speakString());
	}

}
