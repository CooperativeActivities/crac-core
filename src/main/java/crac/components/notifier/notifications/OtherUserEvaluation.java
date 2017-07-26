package crac.components.notifier.notifications;

import java.util.HashMap;

import crac.components.notifier.Notification;
import crac.components.notifier.NotificationType;
import crac.components.utility.DataAccess;
import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.Task;

public class OtherUserEvaluation extends Notification {

	private Long taskId;
	private Long evaluationId;
	private Long toEvaluateId;

	public OtherUserEvaluation() {
		super("Other User-Evaluation", NotificationType.MESSAGE);
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getEvaluationId() {
		return evaluationId;
	}

	public void setEvaluationId(Long evaluationId) {
		this.evaluationId = evaluationId;
	}

	public Long getToEvaluateId() {
		return toEvaluateId;
	}

	public void setToEvaluateId(Long toEvaluateId) {
		this.toEvaluateId = toEvaluateId;
	}

	@Override
	public String accept() {
		
		TaskDAO taskDAO = DataAccess.getRepo(TaskDAO.class);
		Task task = taskDAO.findOne(taskId);

		String message = "Self-Evaluation accepted for: "+task.getName()+". Please fill out form.";
		super.destroy();
		System.out.println(message);
		return message;

	}

	@Override
	public String deny() {
		super.destroy();
		System.out.println("Self-Evaluation denied");
		return "Self-Evaluation denied";

	}

	@Override
	public void inject(HashMap<String, Long> ids) {
		this.taskId = ids.get("task");
		this.evaluationId = ids.get("evaluation");
		this.toEvaluateId = ids.get("toEvaluate");
	}

}
