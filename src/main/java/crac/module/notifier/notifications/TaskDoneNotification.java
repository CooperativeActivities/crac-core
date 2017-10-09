package crac.module.notifier.notifications;

import crac.models.db.entities.Task.TaskShort;
import crac.models.utility.NotificationConfiguration;
import crac.module.notifier.Notification;
import crac.module.notifier.NotificationType;
import lombok.Getter;
import lombok.Setter;

/**
 * Notifies the leader(s) of target task about all participants having finished same task
 * @author David Hondl
 *
 */
public class TaskDoneNotification extends Notification{
	
	@Getter
	@Setter
	private TaskShort task;
	
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
	public void configure(NotificationConfiguration conf) {
		this.task = conf.get("task", TaskShort.class);
	}
	
}
