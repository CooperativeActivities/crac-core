package crac.notifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import crac.models.CracUser;
import crac.models.Evaluation;
import crac.models.Task;
import crac.notifier.notifications.EvaluationNotification;
import crac.notifier.notifications.FriendRequest;
import crac.notifier.notifications.FriendSuggestion;
import crac.notifier.notifications.LeadNomination;
import crac.notifier.notifications.TaskDoneNotification;
import crac.notifier.notifications.TaskInvitation;

public class NotificationHelper {
	
	private NotificationHelper(){}
	
	public static void createTaskDone(Long taskId, Long targetUserId) {
		NotificationDistributor.getInstance().addNotification(new TaskDoneNotification(taskId, targetUserId));
	}

	public static void createFriendRequest(Long sendingUserId, Long targetUserId) {
		NotificationDistributor.getInstance().addNotification(new FriendRequest(sendingUserId, targetUserId));
	}
	
	public static void createFriendSuggestion(Long suggestedUserId, Long targetUserId) {
		NotificationDistributor.getInstance().addNotification(new FriendSuggestion(suggestedUserId, targetUserId));
	}
	
	public static void createTaskInvitation(Long sendingUserId, Long targetUserId, Long taskId) {
		NotificationDistributor.getInstance().addNotification(new TaskInvitation(sendingUserId, targetUserId, taskId));
	}

	
	public static void createLeadNomination(Long sendingUserId, Long targetUserId, Long taskId) {
		NotificationDistributor.getInstance().addNotification(new LeadNomination(sendingUserId, targetUserId, taskId));
	}

	public static EvaluationNotification createEvaluation(Long targetUserId, Long taskId, Long evaluationId) {
		EvaluationNotification e = new EvaluationNotification(targetUserId, taskId, evaluationId);
		NotificationDistributor.getInstance().addNotification(e);
		return e;
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
