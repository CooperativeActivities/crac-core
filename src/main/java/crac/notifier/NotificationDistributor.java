package crac.notifier;

import java.util.ArrayList;
import java.util.Random;

import crac.models.CracUser;

public class NotificationDistributor {
	
	private ArrayList<Notification> notifications = new ArrayList<Notification>();

	public ArrayList<Notification> getNotifications() {
		return notifications;
	}

	public void addNotification(Notification notification) {
		this.notifications.add(notification);
	}
	
	public void deleteNotificationById(String notificationId){
		Notification toRemove = null;
		for(Notification note : notifications){
			if(notificationId.equals(note.getNotificationId())){
				toRemove = note;
			}
		}
		notifications.remove(toRemove);
	}

	private static NotificationDistributor instance = new NotificationDistributor();

	private NotificationDistributor() {
	}

	public static NotificationDistributor getInstance() {
		return instance;
	}
	

}
