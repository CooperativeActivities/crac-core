package crac.components.notifier.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.components.notifier.Notification;
import crac.components.notifier.NotificationHelper;
import crac.components.notifier.NotificationType;

public class FriendSuggestion extends Notification{
	
	private Long suggestedId;
	
	public FriendSuggestion(Long suggestedId, Long targetId){
		super("Friend Suggestion", NotificationType.SUGGESTION, targetId);
		this.suggestedId = suggestedId;
	}
	
	public long getSuggestedId() {
		return suggestedId;
	}

	public void setSuggestedId(long senderId) {
		this.suggestedId = senderId;
	}
/*
	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString((FriendSuggestion)this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}*/

	@Override
	public String accept() {
		//NotificationHelper.createFriendRequest(getTargetId(), suggestedId);
		NotificationHelper.createNotification(new FriendRequest(getTargetId(), suggestedId));
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Friend-suggestion accepted, Friend request sent.");
		return "accepted";
	}

	@Override
	public String deny() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Friend-suggestion denied");
		return "denied";
		
	}
	
}
