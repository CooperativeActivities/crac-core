package crac.module.notifier.notifications;

import crac.enums.TaskParticipationType;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.entities.Task.NotificationTask;
import crac.models.db.relation.UserTaskRel;
import crac.models.utility.NotificationConfiguration;
import crac.module.notifier.Notification;
import crac.module.notifier.NotificationType;
import lombok.Getter;
import lombok.Setter;

/**
 * A notification that informs target user about his/her nomination to beeing a leader to target task; which the user becomes after accepting
 * @author David Hondl
 *
 */
public class LeadNomination extends Notification {

	@Getter
	@Setter
	private NotificationTask task;

	public LeadNomination() {
		super("Lead Nomination", NotificationType.REQUEST);
	}

	@Override
	public String accept() {
		TaskDAO taskDAO = super.getNf().getTaskDAO();
		UserTaskRelDAO userTaskRelDAO = super.getNf().getUserTaskRelDAO();
		CracUserDAO userDAO = super.getNf().getUserDAO();

		Task rtask = taskDAO.findOne(task.getId());
		CracUser user = userDAO.findOne(super.getTarget().getId());

		UserTaskRel utr = userTaskRelDAO.findByUserAndTaskAndParticipationTypeNot(user, rtask,
				TaskParticipationType.LEADING);
		
		String message = "";

		if (utr != null) {
			if (utr.getParticipationType() != TaskParticipationType.LEADING) {
				utr.setParticipationType(TaskParticipationType.LEADING);
				userTaskRelDAO.save(utr);
				message = "Found and Changed";
			}
			else{
				message = "Already Leading";
			}
		} else {
			UserTaskRel newRel = new UserTaskRel();
			newRel.setParticipationType(TaskParticipationType.LEADING);
			newRel.setTask(rtask);
			newRel.setUser(user);
			userTaskRelDAO.save(newRel);
			message = "New Relationship Created";
		}

		super.destroy();
		System.out.println("Leader-Nomination accepted, "+message);
		return message;

	}

	@Override
	public String deny() {
		super.destroy();
		System.out.println("Leader-Nomination denied");
		return "denied";

	}

	@Override
	public void configure(NotificationConfiguration conf) {
		this.task = conf.get("task", NotificationTask.class);
	}

}
