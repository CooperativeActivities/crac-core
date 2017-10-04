package crac.module.notifier.notifications;

import crac.models.utility.NotificationConfiguration;
import crac.module.notifier.Notification;
import crac.module.notifier.NotificationType;

/**
 * An notification that suggests a possible FriendRequest to target user
 * @author David Hondl
 *
 */
public class FriendSuggestion extends Notification{
	
	public FriendSuggestion(){
		super("Friend Suggestion", NotificationType.SUGGESTION);
	}
	
	@Override
	public String accept() {
		super.getNf().createNotification(FriendRequest.class, getTarget(), getSender(), null);
		super.destroy();
		System.out.println("Friend-suggestion accepted, Friend request sent.");
		return "accepted";
	}

	@Override
	public String deny() {
		super.destroy();
		System.out.println("Friend-suggestion denied");
		return "denied";
		
	}

	@Override
	public void configure(NotificationConfiguration conf) {
	}
	
}
