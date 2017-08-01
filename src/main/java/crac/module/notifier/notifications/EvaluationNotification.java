package crac.module.notifier.notifications;

import java.util.HashMap;

import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.Task;
import crac.module.notifier.Notification;
import crac.module.notifier.NotificationType;

public class EvaluationNotification extends Notification {

	private long taskId;
	private long evaluationId;

	public EvaluationNotification() {
		super("Evaluation", NotificationType.MESSAGE);
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
	public String accept() {
		
		TaskDAO taskDAO = super.getNf().getTaskDAO();
		Task task = taskDAO.findOne(taskId);

		String message = "Evaluation accepted for: "+task.getName()+". Please fill out form.";
		super.destroy();

		System.out.println(message);
		return message;

	}

	@Override
	public String deny() {
		super.destroy();
		System.out.println("Evaluation denied");
		return "Self-Evaluation denied";

	}

	@Override
	public void inject(HashMap<String, Long> ids) {
		this.taskId = ids.get("task");
		this.evaluationId = ids.get("evaluation");	
	}

}
