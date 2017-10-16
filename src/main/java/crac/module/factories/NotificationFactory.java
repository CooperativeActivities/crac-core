package crac.module.factories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.CracUser.NotificationUser;
import crac.models.utility.NotificationConfiguration;
import crac.module.notifier.Notification;
import lombok.Getter;
import lombok.Setter;

/**
 * A factory that creates arbitrary objects of the type Notification
 * @author David Hondl
 *
 */
@Component
@Scope("singleton")
public class NotificationFactory {

	@Autowired
	@Getter
	@Setter
	private TaskDAO taskDAO;

	@Autowired
	@Getter
	@Setter
	private CracUserDAO userDAO;

	@Autowired
	@Getter
	@Setter
	private UserRelationshipDAO userRelationshipDAO;

	@Autowired
	@Getter
	@Setter
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
	
	/**
	 * This overrides actual method, accepting different parameters
	 * @param type
	 * @param target
	 * @param sender
	 * @param conf
	 * @return
	 */
	public <T extends Notification> Notification createNotification(Class<T> type, CracUser target, CracUser sender, NotificationConfiguration conf) {
		return createNotification(type, target.generateNUser(), sender.generateNUser(), conf);
	}

	/**
	 * This overrides actual method, accepting different parameters; instead of the usual sender, the system gets set as sender
	 * @param type
	 * @param target
	 * @param conf
	 * @return
	 */
	public <T extends Notification> Notification createSystemNotification(Class<T> type, CracUser target, NotificationConfiguration conf) {
		return createNotification(type, target.generateNUser(), CracUser.sys(), conf);
	}

	/**
	 * This overrides actual method, accepting different parameters; instead of the usual sender, the system gets set as sender
	 * @param type
	 * @param target
	 * @param conf
	 * @return
	 */
	public <T extends Notification> Notification createSystemNotification(Class<T> type, NotificationUser target, NotificationConfiguration conf) {
		return createNotification(type, target, CracUser.sys(), conf);
	}
	
	/**
	 * This method takes in the type of the notification, its target and sender and its configuration and adds it to the global notification-list while returning a reference
	 * @param type
	 * @param target
	 * @param sender
	 * @param conf
	 * @return Notification
	 */
	public <T extends Notification> Notification createNotification(Class<T> type, NotificationUser target, NotificationUser sender,
			NotificationConfiguration conf) {

		Notification n = null;

		n = BeanUtils.instantiate(type);

		n.setNf(this);
		n.setSender(sender);
		n.setTarget(target);
		n.configure(conf);
		System.out.println("Notification " + n.getName() + " created!");

		addNotification(n);

		return n;
	}

	/**
	 * Add target notification or update it by its id
	 * @param notification
	 */
	public void addNotification(Notification notification) {
		this.notifications.put(notification.getNotificationId(), notification);
	}

	/**
	 * Delete target notification
	 * @param notificationId
	 */
	public void deleteNotificationById(String notificationId) {
		this.notifications.remove(notificationId);
	}

	/**
	 * Get target notification
	 * @param notificationId
	 * @return
	 */
	public Notification getNotificationById(String notificationId) {
		return this.notifications.get(notificationId);
	}

	/**
	 * Get all notifications of target user
	 * @param user
	 * @return List<Notification>
	 */
	public List<Notification> getUserNotifications(CracUser user) {		
		return this.notifications.entrySet()
				.stream()
				.map( x -> x.getValue() )
				.filter( x -> x.getTarget().equals(user.generateNUser()) )
				.collect(Collectors.toList());
	}

}
