package crac.notifier.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.Task;
import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.notifier.NotificationType;
import crac.utility.DataAccess;

public class EvaluationNotification extends Notification {

	private long taskId;
	private long evaluationId;

	public EvaluationNotification(Long targetId, Long taskId, Long evaluationId) {
		super("Evaluation", NotificationType.MESSAGE, targetId);
		this.taskId = taskId;
		this.evaluationId = evaluationId;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	
	public long getEvaluationId() {
		return evaluationId;
	}

	public void setEvaluationIdy(long evaluationId) {
		this.evaluationId = evaluationId;
	}

	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString((EvaluationNotification) this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}

	@Override
	public String accept() {
		
		TaskDAO taskDAO = DataAccess.getRepo(TaskDAO.class);
		Task task = taskDAO.findOne(taskId);

		String message = "Evaluation accepted for: "+task.getName()+". Please fill out form.";
		
		System.out.println(message);
		return message;

	}

	@Override
	public String deny() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Evaluation denied");
		return "Self-Evaluation denied";

	}

}
