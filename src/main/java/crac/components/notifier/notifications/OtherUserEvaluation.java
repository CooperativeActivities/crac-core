package crac.components.notifier.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.components.notifier.Notification;
import crac.components.notifier.NotificationHelper;
import crac.components.notifier.NotificationType;
import crac.components.utility.DataAccess;
import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.Task;

public class OtherUserEvaluation extends Notification {

	private Long taskId;
	private Long evaluationId;
	private Long toEvaluateId;

	public OtherUserEvaluation(Long targetId, Long taskId, Long evaluationId, Long toEvaluateId) {
		super("Other User-Evaluation", NotificationType.MESSAGE, targetId);
		this.taskId = taskId;
		this.evaluationId = evaluationId;
		this.toEvaluateId = toEvaluateId;
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

	public void setEvaluationId(long evaluationId) {
		this.evaluationId = evaluationId;
	}
/*
	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString((OtherUserEvaluation) this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}*/

	@Override
	public String accept() {
		
		TaskDAO taskDAO = DataAccess.getRepo(TaskDAO.class);
		Task task = taskDAO.findOne(taskId);

		String message = "Self-Evaluation accepted for: "+task.getName()+". Please fill out form.";
		
		System.out.println(message);
		return message;

	}

	@Override
	public String deny() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Self-Evaluation denied");
		return "Self-Evaluation denied";

	}

}
