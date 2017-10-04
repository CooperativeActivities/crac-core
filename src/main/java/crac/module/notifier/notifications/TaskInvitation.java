package crac.module.notifier.notifications;

import crac.enums.TaskParticipationType;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Task.NotificationTask;
import crac.models.db.relation.UserTaskRel;
import crac.models.utility.NotificationConfiguration;
import crac.module.notifier.Notification;
import crac.module.notifier.NotificationType;
import lombok.Getter;
import lombok.Setter;

/**
 * Invites target user to target task; after accepting he/she becomes a participant
 * @author David Hondl
 *
 */
public class TaskInvitation extends Notification{
		
	@Getter
	@Setter
	private NotificationTask task;
	
	public TaskInvitation(){
		super("Task Invitation", NotificationType.SUGGESTION);
	}
	
	@Override
	public String accept() {
		
		UserTaskRelDAO userTaskRelDAO = super.getNf().getUserTaskRelDAO();
		CracUserDAO userDAO = super.getNf().getUserDAO();
		TaskDAO taskDAO = super.getNf().getTaskDAO();

		UserTaskRel utl = new UserTaskRel();
		
		utl.setTask(taskDAO.findOne(task.getId()));
		utl.setUser(userDAO.findOne(getTarget().getId()));
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
	public void configure(NotificationConfiguration conf) {
		this.task = conf.get("task", NotificationTask.class);
	}
	
}
