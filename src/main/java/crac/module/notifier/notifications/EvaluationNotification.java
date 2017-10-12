package crac.module.notifier.notifications;

import crac.models.db.entities.Task.TaskShort;
import crac.models.utility.NotificationConfiguration;
import crac.module.notifier.Notification;
import crac.module.notifier.NotificationType;
import lombok.Getter;
import lombok.Setter;

/**
 * Notification which messages target user, that there is an evaluation available
 * @author David Hondl
 *
 */
public class EvaluationNotification extends Notification {

	@Getter
	@Setter
	private TaskShort task;

	@Getter
	@Setter
	private Long evaluationid;

	public EvaluationNotification() {
		super("Evaluation", NotificationType.MESSAGE);
	}

	@Override
	public String accept() {
		
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
	public void configure(NotificationConfiguration conf) {
		this.task = conf.get("task", TaskShort.class);
		this.evaluationid = conf.get("evaluationid", Long.class);	
	}

}
