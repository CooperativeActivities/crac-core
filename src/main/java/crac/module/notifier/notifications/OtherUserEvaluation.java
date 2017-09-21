package crac.module.notifier.notifications;

import crac.models.db.entities.Task.NotificationTask;
import crac.models.utility.NotificationConfiguration;
import crac.module.notifier.Notification;
import crac.module.notifier.NotificationType;
import lombok.Getter;
import lombok.Setter;

/**
 * An reminder to target user to evaluation another target user
 * @author David Hondl
 *
 */
public class OtherUserEvaluation extends Notification {

	@Getter
	@Setter
	private NotificationTask task;

	@Getter
	@Setter
	private Long evaluationId;

	@Getter
	@Setter
	private Long toEvaluateId;

	public OtherUserEvaluation() {
		super("Other User-Evaluation", NotificationType.MESSAGE);
	}

	@Override
	public String accept() {
		
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
	public void configure(NotificationConfiguration conf) {
		this.task = conf.get("task", NotificationTask.class);
		this.evaluationId = conf.get("evaluation", Long.class);
		this.toEvaluateId = conf.get("toEvaluate", Long.class);
	}

}
