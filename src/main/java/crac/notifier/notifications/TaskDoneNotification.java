package crac.notifier.notifications;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.springframework.data.repository.CrudRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.daos.UserRelationshipDAO;
import crac.models.CracUser;
import crac.models.relation.UserRelationship;
import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.notifier.NotificationType;

public class TaskDoneNotification extends Notification{
	
	private Long taskId;
	
	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public TaskDoneNotification(Long taskId, Long targetId){
		super("Task is done", NotificationType.SUGGESTION, targetId);
		this.taskId = taskId;
	}
	
	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString((TaskDoneNotification)this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}

	@Override
	public String accept(HashMap<String, CrudRepository> map) {
		System.out.println("Task-completion accepted");
		return "accepted";
	}

	@Override
	public String deny() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Task-completion denied");
		return "denied";
		
	}
	
}
