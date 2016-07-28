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
		this.wrappedNotifications.add(new NotificationWrapper(target, notification, randomString(10)));
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
	
	public String randomString(final int length) {
	    Random r = new Random(); // perhaps make it a class variable so you don't make a new one every time
	    StringBuilder sb = new StringBuilder();
	    for(int i = 0; i < length; i++) {
	        int c = r.nextInt(9);
	        sb.append(c);
	    }
	    return sb.toString();
	}

}
