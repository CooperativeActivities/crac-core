package crac.notifier;

public abstract class Notification {
	
	private String name;
	
	private NotificationType type;

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
