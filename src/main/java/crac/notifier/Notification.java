package crac.notifier;

import crac.models.CracUser;

public abstract class Notification {
	
	private String notificationId;
	
	private String targetId;
	
	private String name;
	
	private NotificationType type;
	
	public abstract void accept();
	
	public abstract void deny();
	
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

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	
}
