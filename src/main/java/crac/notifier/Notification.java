package crac.notifier;

public abstract class Notification {
	
	private String notificationId;
	
	private String name;
	
	private NotificationType type;
	
	public abstract void accept();
	
	public abstract void deny();

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
	
	public abstract String toJSon();
	
	

}
