package crac.notifier.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.CracUser;
import crac.notifier.Notification;
import crac.notifier.NotificationType;

public class FriendRequest extends Notification{
	
	private long senderId;
	
	public FriendRequest(Long senderId){
		this.senderId = senderId;
		super.setName("Friend Request");
		super.setType(NotificationType.REQUEST);
	}
	
	public long getSenderId() {
		return senderId;
	}

	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}

	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString((FriendRequest)this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}
	
}
