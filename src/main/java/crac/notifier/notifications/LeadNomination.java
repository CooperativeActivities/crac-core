package crac.notifier.notifications;

import java.util.HashMap;

import org.springframework.data.repository.CrudRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.daos.UserTaskRelDAO;
import crac.enums.TaskParticipationType;
import crac.models.Task;
import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.notifier.NotificationType;
import crac.relationmodels.UserTaskRel;

public class LeadNomination extends Notification{

	private long senderId;
	private long taskId;
	
	public LeadNomination(Long senderId, Long targetId, Long taskId){
		super.setTargetId(targetId);
		super.setNotificationId(NotificationHelper.randomString(20));
		this.senderId = senderId;
		super.setName("Lead Nomination");
		super.setType(NotificationType.REQUEST);
		this.taskId = taskId;
	}
	
	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public long getSenderId() {
		return senderId;
	}

	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}

	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString((LeadNomination)this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}

	@Override
	public void accept(HashMap<String, CrudRepository> map) {
		//TODO search for existing relationship
		TaskDAO taskDAO = (TaskDAO) map.get("taskDAO");
		UserTaskRelDAO userTaskRelDAO = (UserTaskRelDAO) map.get("userTaskRelDAO");
		CracUserDAO userDAO = (CracUserDAO) map.get("userDAO");
		
		Task task = taskDAO.findOne(taskId);
		UserTaskRel newRel = new UserTaskRel();
		newRel.setParticipationType(TaskParticipationType.LEADING);
		newRel.setTask(task);
		newRel.setUser(userDAO.findOne(super.getTargetId()));
		userTaskRelDAO.save(newRel);

		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Leader-Nomination accepted");
		
	}

	@Override
	public void deny() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Leader-Nomination denied");
		
	}

}
