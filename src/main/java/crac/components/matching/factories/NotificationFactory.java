package crac.components.matching.factories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.components.notifier.Notification;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.CracUser;

@Component
@Scope("singleton")
public class NotificationFactory {

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private UserRelationshipDAO userRelationshipDAO;

	@Autowired
	private UserTaskRelDAO userTaskRelDAO;

	private HashMap<String, Notification> notifications;

	private NotificationFactory() {
		notifications = new HashMap<String, Notification>();
	}

	public ArrayList<Notification> getNotifications() {
		ArrayList<Notification> list = new ArrayList<Notification>();

		for (Entry<String, Notification> entry : this.notifications.entrySet()) {
			list.add(entry.getValue());
		}

		return list;
	}

	public <T extends Notification> Notification createNotification(Class<T> type, long targetId, long senderId,
			HashMap<String, Long> ids) {

		Notification n = null;

		n = BeanUtils.instantiate(type);

		n.setNf(this);
		n.setSenderId(senderId);
		n.setTargetId(targetId);
		n.inject(ids);
		System.out.println("Notification " + n.getName() + " created!");

		addNotification(n);

		return n;
	}

	public void addNotification(Notification notification) {
		this.notifications.put(notification.getNotificationId(), notification);
	}

	public void deleteNotificationById(String notificationId) {
		this.notifications.remove(notificationId);
	}

	public Notification getNotificationById(String notificationId) {
		return this.notifications.get(notificationId);
	}

	public ArrayList<Notification> getUserNotifications(CracUser user) {
		ArrayList<Notification> list = new ArrayList<Notification>();

		for (Entry<String, Notification> entry : this.notifications.entrySet()) {
			Notification n = entry.getValue();
			if (n.getTargetId() == user.getId()) {
				list.add(n);
			}
		}

		return list;

	}

	public TaskDAO getTaskDAO() {
		return taskDAO;
	}

	public void setTaskDAO(TaskDAO taskDAO) {
		this.taskDAO = taskDAO;
	}

	public CracUserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(CracUserDAO userDAO) {
		this.userDAO = userDAO;
	}

	public UserRelationshipDAO getUserRelationshipDAO() {
		return userRelationshipDAO;
	}

	public void setUserRelationshipDAO(UserRelationshipDAO userRelationshipDAO) {
		this.userRelationshipDAO = userRelationshipDAO;
	}

	public void setNotifications(HashMap<String, Notification> notifications) {
		this.notifications = notifications;
	}

	public UserTaskRelDAO getUserTaskRelDAO() {
		return userTaskRelDAO;
	}

	public void setUserTaskRelDAO(UserTaskRelDAO userTaskRelDAO) {
		this.userTaskRelDAO = userTaskRelDAO;
	}

}
