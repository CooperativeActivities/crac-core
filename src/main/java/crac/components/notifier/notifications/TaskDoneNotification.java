package crac.components.notifier.notifications;

import java.util.HashMap;

import crac.components.notifier.Notification;
import crac.components.notifier.NotificationType;

public class TaskDoneNotification extends Notification{
	
	private Long taskId;
	
	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public TaskDoneNotification(){
		super("Task is done", NotificationType.SUGGESTION);
	}

	@Override
	public String accept() {
		super.destroy();
		System.out.println("Task-completion accepted");
		return "accepted";
	}

	@Override
	public String deny() {
		super.destroy();
		System.out.println("Task-completion denied");
		return "denied";
		
	}

	@Override
	public void inject(HashMap<String, Long> ids) {
		this.taskId = ids.get("task");
	}
	
}
