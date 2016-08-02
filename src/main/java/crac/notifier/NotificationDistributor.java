package crac.notifier;

import java.util.ArrayList;
import java.util.Random;

import crac.models.CracUser;

public class NotificationDistributor {
	
	private ArrayList<NotificationWrapper> wrappedNotifications = new ArrayList<NotificationWrapper>();

	public ArrayList<NotificationWrapper> getWrappedNotifications() {
		return wrappedNotifications;
	}

	public void addNotification(CracUser target, Notification notification) {
		this.wrappedNotifications.add(new NotificationWrapper(target, notification, NotificationHelper.randomString(10)));
	}
	
	public void deleteNotification(String id){
		for(NotificationWrapper note : wrappedNotifications){
			if(id == note.getId()){
				wrappedNotifications.remove(note);
			}
		}
	}

	private static NotificationDistributor instance = new NotificationDistributor();

	private NotificationDistributor() {
	}

	public static NotificationDistributor getInstance() {
		return instance;
	}
	

}
