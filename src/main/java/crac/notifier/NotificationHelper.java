package crac.notifier;

import java.util.ArrayList;
import java.util.Random;

import crac.models.CracUser;
import crac.notifier.notifications.FriendRequest;

public class NotificationHelper {
	
	private NotificationHelper(){}

	public static void createFriendRequest(CracUser sender, CracUser target) {
		NotificationDistributor.getInstance().addNotification(new FriendRequest(sender.getId(), target.getId()+""));
	}
	
	public static void deleteNotification(String id) {
		NotificationDistributor.getInstance().deleteNotificationById(id);
	}

	public static ArrayList<Notification> getAllNotifications() {
		return NotificationDistributor.getInstance().getNotifications();
	}
	
	public static Notification getNotificationByNotificationId(String notificationId){
		ArrayList<Notification> notifications = NotificationDistributor.getInstance().getNotifications();
		for(Notification n : notifications){
			if(n.getNotificationId().equals(notificationId)){
				return n;
			}
		}
		
		return null;
		
	}

	public static ArrayList<Notification> getUserNotifications(CracUser user) {
		ArrayList<Notification> list = new ArrayList<Notification>();

		for (Notification nw : NotificationDistributor.getInstance().getNotifications()) {
			if (nw.getTargetId().equals(user.getId()+"")) {
				list.add(nw);
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
		System.out.println(s);

		return "[" + s + "]";
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
