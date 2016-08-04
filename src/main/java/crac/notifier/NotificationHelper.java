package crac.notifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import crac.models.CracUser;
import crac.models.Task;
import crac.notifier.notifications.FriendRequest;
import crac.notifier.notifications.LeadNomination;

public class NotificationHelper {
	
	private NotificationHelper(){}

	public static void createFriendRequest(CracUser sender, CracUser target) {
		NotificationDistributor.getInstance().addNotification(new FriendRequest(sender.getId(), target.getId()));
	}
	
	public static void createLeadNomination(CracUser sender, CracUser target, Task task) {
		NotificationDistributor.getInstance().addNotification(new LeadNomination(sender.getId(), target.getId(), task.getId()));
	}

	
	public static void deleteNotification(String id) {
		NotificationDistributor.getInstance().deleteNotificationById(id);
	}

	public static HashMap<String, Notification> getAllNotifications() {
		return NotificationDistributor.getInstance().getNotifications();
	}
	
	public static Notification getNotificationById(String notificationId){
		Notification notification = NotificationDistributor.getInstance().getNotificationById(notificationId);
		
		return notification;
		
	}

	public static ArrayList<Notification> getUserNotifications(CracUser user) {
		ArrayList<Notification> list = new ArrayList<Notification>();

		for (Entry<String, Notification> entry : NotificationDistributor.getInstance().getNotifications().entrySet()) {
			Notification n = entry.getValue();
			if (n.getTargetId() == user.getId()) {
				list.add(n);
			}
		}

		return list;

	}

	public static String notificationsToString(ArrayList<Notification> notifications) {

		String s = "";

		for (Notification n : notifications) {
			s += n.toJSon() + ",";
		}
		
		s = s.replaceAll(",$", "");

		return "[" + s + "]";
	}
	
	public static String notificationsToString(HashMap<String, Notification> map) {
		ArrayList<Notification> notifications = new ArrayList<Notification>();
		
		for (Entry<String, Notification> entry : map.entrySet()) {
			notifications.add(entry.getValue());
		}
		
		return notificationsToString(notifications);
	}

	
	public static String notificationsToString(Notification n) {
		return "[" + n.toJSon() + "]";
	}
	
	public static String randomString(final int length) {
	    Random r = new Random(); // perhaps make it a class variable so you don't make a new one every time
	    StringBuilder sb = new StringBuilder();
	    for(int i = 0; i < length; i++) {
	        int c = r.nextInt(9);
	        sb.append(c);
	    }
	    return sb.toString();
	}


}
