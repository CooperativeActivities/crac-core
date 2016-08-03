package crac.notifier.notifications;

import java.util.HashMap;

import org.springframework.data.repository.CrudRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.CracUser;
import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.notifier.NotificationType;

public class FriendRequest extends Notification{
	
	private long senderId;
	
	public FriendRequest(Long senderId, Long targetId){
		super.setTargetId(targetId);
		super.setNotificationId(NotificationHelper.randomString(20));
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

	@Override
	public void accept(HashMap<String, CrudRepository> map) {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Friend-request accepted");
		
	}

	@Override
	public void deny() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Friend-request denied");
		
	}
	
}
