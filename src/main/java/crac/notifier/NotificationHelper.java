package crac.notifier;

import java.util.ArrayList;
import java.util.Random;

import crac.models.CracUser;
import crac.notifier.notifications.FriendRequest;

public class NotificationHelper {
	
	private NotificationHelper(){}

	public static void createFriendRequest(CracUser sender, CracUser receiver) {
		NotificationDistributor.getInstance().addNotification(receiver, new FriendRequest(sender.getId()));
	}

	public static void delelteNotification(String id) {
		NotificationDistributor.getInstance().deleteNotification(id);
	}

	public static ArrayList<Notification> getAllNotifications() {
		return unwrap(NotificationDistributor.getInstance().getWrappedNotifications());
	}
	
	public static Notification getNotificationByNotificationId(String notificationId){
		ArrayList<Notification> notifications = unwrap(NotificationDistributor.getInstance().getWrappedNotifications());
		for(Notification n : notifications){
			if(n.getNotificationId().equals(notificationId)){
				return n;
			}
		}
		
		return null;
		
	}

	public static ArrayList<Notification> getUserNotifications(CracUser user) {
		ArrayList<NotificationWrapper> list = new ArrayList<NotificationWrapper>();

		for (NotificationWrapper nw : NotificationDistributor.getInstance().getWrappedNotifications()) {
			if (nw.getTarget().getId() == user.getId()) {
				list.add(nw);
			}
		}

		return unwrap(list);

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


	private static ArrayList<Notification> unwrap(ArrayList<NotificationWrapper> wrapped) {
		ArrayList<Notification> unwrapped = new ArrayList<Notification>();
		for (NotificationWrapper wrappedElement : wrapped) {
			unwrapped.add(wrappedElement.getNotification());
		}
		return unwrapped;
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
