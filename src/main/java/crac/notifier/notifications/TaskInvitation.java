package crac.notifier.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.enums.TaskParticipationType;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.relation.UserTaskRel;
import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.notifier.NotificationType;
import crac.utility.DataAccess;

public class TaskInvitation extends Notification{
	
	private long senderId;
	
	private long taskId;
	
	public TaskInvitation(Long senderId, Long targetId, Long taskId){
		super("Task Invitation", NotificationType.SUGGESTION, targetId);
		this.senderId = senderId;
		this.taskId = taskId;
	}
	
	public long getSenderId() {
		return senderId;
	}

	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString((TaskInvitation)this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
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
		
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Task-invitation accepted");
		return "accepted";
	}

	@Override
	public String deny() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Friend-request denied");
		return "denied";
		
	}
	
}
