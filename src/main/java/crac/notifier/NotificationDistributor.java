package crac.notifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import crac.models.db.entities.CracUser;

public class NotificationDistributor {
	
	private HashMap<String, Notification> notifications = new HashMap<String, Notification>();

	public HashMap<String, Notification> getNotifications() {
		return notifications;
	}

	public void addNotification(Notification notification) {
		this.notifications.put(notification.getNotificationId(), notification);
	}
	
	public void deleteNotificationById(String notificationId){
		this.notifications.remove(notificationId);
	}
	
	public Notification getNotificationById(String notificationId){
		return this.notifications.get(notificationId);
	}

	private static NotificationDistributor instance = new NotificationDistributor();

	private NotificationDistributor() {
	}

	public static NotificationDistributor getInstance() {
		return instance;
	}
	

}
