package crac.notifier.notifications;

import java.util.HashMap;

import org.springframework.data.repository.CrudRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.TaskDAO;
import crac.models.Task;
import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.notifier.NotificationType;

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

	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString((OtherUserEvaluation) this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}

	@Override
	public String accept(HashMap<String, CrudRepository> map) {
		
		TaskDAO taskDAO = (TaskDAO) map.get("taskDAO");
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
