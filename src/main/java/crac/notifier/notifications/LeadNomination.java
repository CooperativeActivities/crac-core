package crac.notifier.notifications;

import java.util.HashMap;

import org.springframework.data.repository.CrudRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.daos.UserTaskRelDAO;
import crac.enums.TaskParticipationType;
import crac.models.CracUser;
import crac.models.Task;
import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.notifier.NotificationType;
import crac.relationmodels.UserTaskRel;

public class LeadNomination extends Notification {

	private long senderId;
	private long taskId;

	public LeadNomination(Long senderId, Long targetId, Long taskId) {
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
			return mapper.writeValueAsString((LeadNomination) this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}

	@Override
	public String accept(HashMap<String, CrudRepository> map) {
		TaskDAO taskDAO = (TaskDAO) map.get("taskDAO");
		UserTaskRelDAO userTaskRelDAO = (UserTaskRelDAO) map.get("userTaskRelDAO");
		CracUserDAO userDAO = (CracUserDAO) map.get("userDAO");

		Task task = taskDAO.findOne(taskId);
		CracUser user = userDAO.findOne(super.getTargetId());

		UserTaskRel utr = userTaskRelDAO.findByUserAndTask(user, task);
		
		String message = "";

		if (utr != null) {
			if (utr.getParticipationType() != TaskParticipationType.LEADING) {
				utr.setParticipationType(TaskParticipationType.LEADING);
				userTaskRelDAO.save(utr);
				message = "Found and Changed";
			}
			else{
				message = "Already Leading";
			}
		} else {
			UserTaskRel newRel = new UserTaskRel();
			newRel.setParticipationType(TaskParticipationType.LEADING);
			newRel.setTask(task);
			newRel.setUser(user);
			userTaskRelDAO.save(newRel);
			message = "New Relationship Created";
		}

		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Leader-Nomination accepted, "+message);
		return message;

	}

	@Override
	public String deny() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Leader-Nomination denied");
		return "denied";

	}

}
