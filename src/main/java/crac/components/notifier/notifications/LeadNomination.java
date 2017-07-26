package crac.components.notifier.notifications;

import java.util.HashMap;

import crac.components.notifier.Notification;
import crac.components.notifier.NotificationType;
import crac.enums.TaskParticipationType;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;

public class LeadNomination extends Notification {

	private long taskId;

	public LeadNomination() {
		super("Lead Nomination", NotificationType.REQUEST);
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	@Override
	public String accept() {
		TaskDAO taskDAO = super.getNf().getTaskDAO();
		UserTaskRelDAO userTaskRelDAO = super.getNf().getUserTaskRelDAO();
		CracUserDAO userDAO = super.getNf().getUserDAO();

		Task task = taskDAO.findOne(taskId);
		CracUser user = userDAO.findOne(super.getTargetId());

		UserTaskRel utr = userTaskRelDAO.findByUserAndTaskAndParticipationTypeNot(user, task,
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
			newRel.setTask(task);
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
	public void inject(HashMap<String, Long> ids) {
		this.taskId = ids.get("task");
	}

}
