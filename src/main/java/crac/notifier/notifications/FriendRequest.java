package crac.notifier.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.CracUser;
import crac.notifier.Notification;
import crac.notifier.NotificationType;

public class FriendRequest extends Notification{
	
	private CracUser sender;
	
	public FriendRequest(CracUser sender){
		this.sender = sender;
		super.setName("Friend Request");
		super.setType(NotificationType.REQUEST);
	}

	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}
	
}
