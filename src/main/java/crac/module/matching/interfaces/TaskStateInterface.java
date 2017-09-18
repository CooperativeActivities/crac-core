package crac.module.matching.interfaces;

import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.Task;

public interface TaskStateInterface {
	
	public boolean isJoinable();
	
	public boolean isExtendable();
	
	public boolean inConduction();
	
	public void nextTaskState(Task t, TaskDAO taskDAO);
		
}
