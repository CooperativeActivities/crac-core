package crac.components.notifier;

import java.util.Calendar;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import crac.components.matching.factories.NotificationFactory;
import crac.models.utility.RandomUtility;

public abstract class Notification {
	
	private String notificationId;
	
	private Long senderId;

	private Long targetId;
	
	private Calendar creationTime;
	
	private String name;
	
	private NotificationType type;
	
	@JsonIgnore
	private NotificationFactory nf;
		
	public Notification(String name, NotificationType type){
		this.name = name;
		this.type = type;
		this.creationTime = Calendar.getInstance();
		this.notificationId = RandomUtility.randomString(20);
	}
	
	public void destroy(){
		nf.deleteNotificationById(this.getNotificationId());
	}
	
	public abstract String accept();
	
	public abstract String deny();
	
	public abstract void inject(HashMap<String, Long> ids);
	
	//public abstract String toJSon();

	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public Calendar getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Calendar creationTime) {
		this.creationTime = creationTime;
	}

	public Long getSenderId() {
		return senderId;
	}

	public void setSenderId(Long senderId) {
		this.senderId = senderId;
	}

	public NotificationFactory getNf() {
		return nf;
	}

	public void setNf(NotificationFactory nf) {
		this.nf = nf;
	}

}
