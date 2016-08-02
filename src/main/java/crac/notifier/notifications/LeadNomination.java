package crac.notifier.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.notifier.NotificationType;

public class LeadNomination extends Notification{

	private long senderId;
	
	public LeadNomination(Long senderId){
		super.setNotificationId(NotificationHelper.randomString(20));
		this.senderId = senderId;
		super.setName("Lead Nomination");
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
			return mapper.writeValueAsString((LeadNomination)this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}

	@Override
	public void accept() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Leader-Nomination accepted");
		
	}

	@Override
	public void deny() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Leader-Nomination denied");
		
	}

}
