package crac.components.notifier.notifications;

import java.util.HashMap;

import crac.components.notifier.Notification;
import crac.components.notifier.NotificationType;
import crac.components.utility.DataAccess;
import crac.enums.TaskParticipationType;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.relation.UserTaskRel;

public class TaskInvitation extends Notification{
		
	private long taskId;
	
	public TaskInvitation(){
		super("Task Invitation", NotificationType.SUGGESTION);
	}
	
	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	@Override
	public String accept() {
		
		UserTaskRelDAO userTaskRelDAO = DataAccess.getRepo(UserTaskRelDAO.class);
		CracUserDAO userDAO = DataAccess.getRepo(CracUserDAO.class);
		TaskDAO taskDAO = DataAccess.getRepo(TaskDAO.class);

		UserTaskRel utl = new UserTaskRel();
		
		utl.setTask(taskDAO.findOne(taskId));
		utl.setUser(userDAO.findOne(getTargetId()));
		utl.setParticipationType(TaskParticipationType.PARTICIPATING);
		
		userTaskRelDAO.save(utl);
		
		super.destroy();
		System.out.println("Task-invitation accepted");
		return "accepted";
	}

	@Override
	public String deny() {
		super.destroy();
		System.out.println("Friend-request denied");
		return "denied";
		
	}

	@Override
	public void inject(HashMap<String, Long> ids) {
		this.taskId = ids.get("task");
	}
	
}
