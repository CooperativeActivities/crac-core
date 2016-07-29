package crac.notifier;

import java.util.ArrayList;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.CracUser;
import crac.notifier.notifications.FriendRequest;
import crac.utility.JSonResponseHelper;

public class NotificationHelper {

	public static void createFriendRequest(CracUser sender, CracUser receiver) {
		NotificationDistributor n1 = NotificationDistributor.getInstance();
		n1.addNotification(receiver, new FriendRequest(sender.getId()));
	}

	public static void delelteNotification(String id) {
		NotificationDistributor n1 = NotificationDistributor.getInstance();
		n1.deleteNotification(id);
	}

	public static ArrayList<Notification> getAllNotifications() {
		NotificationDistributor n = NotificationDistributor.getInstance();
		return unwrap(n.getWrappedNotifications());
	}

	public static ArrayList<Notification> getUserNotifications(CracUser user) {
		NotificationDistributor n = NotificationDistributor.getInstance();
		ArrayList<NotificationWrapper> list = new ArrayList<NotificationWrapper>();

		for (NotificationWrapper nw : n.getWrappedNotifications()) {
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

	private static ArrayList<Notification> unwrap(ArrayList<NotificationWrapper> wrapped) {
		ArrayList<Notification> unwrapped = new ArrayList<Notification>();
		for (NotificationWrapper wrappedElement : wrapped) {
			unwrapped.add(wrappedElement.getNotification());
		}
		return unwrapped;
	}

}
