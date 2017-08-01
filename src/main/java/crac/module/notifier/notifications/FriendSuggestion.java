package crac.module.notifier.notifications;

import java.util.HashMap;

import crac.module.notifier.Notification;
import crac.module.notifier.NotificationType;

public class FriendSuggestion extends Notification{
	
	
	public FriendSuggestion(){
		super("Friend Suggestion", NotificationType.SUGGESTION);
	}
	
	@Override
	public String accept() {
		//NotificationHelper.createFriendRequest(getTargetId(), suggestedId);
		super.getNf().createNotification(FriendRequest.class, getTargetId(), getSenderId(), null);
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
	public void inject(HashMap<String, Long> ids) {
		// TODO Auto-generated method stub
		
	}
	
}
