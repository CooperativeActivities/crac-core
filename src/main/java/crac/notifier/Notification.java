package crac.notifier;

import java.util.Calendar;
import java.util.HashMap;

import org.springframework.data.repository.CrudRepository;

import crac.models.CracUser;

public abstract class Notification {
	
	private String notificationId;
	
	private Long targetId;
	
	private Calendar creationTime;
	
	private String name;
	
	private NotificationType type;
	
	public Notification(String name, NotificationType type, Long targetId){
		this.name = name;
		this.type = type;
		this.creationTime = Calendar.getInstance();
		this.notificationId = NotificationHelper.randomString(20);
		this.targetId = targetId;

	}
	
	public abstract String accept(HashMap<String, CrudRepository> map);
	
	public abstract String deny();
	
	public abstract String toJSon();

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

	
}
