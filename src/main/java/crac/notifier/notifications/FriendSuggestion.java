package crac.notifier.notifications;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.springframework.data.repository.CrudRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.db.entities.CracUser;
import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.notifier.NotificationType;

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

	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString((FriendSuggestion)this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}

	@Override
	public String accept(HashMap<String, CrudRepository> map) {
		NotificationHelper.createFriendRequest(getTargetId(), suggestedId);
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
