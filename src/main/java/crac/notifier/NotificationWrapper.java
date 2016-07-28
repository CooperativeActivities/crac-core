package crac.notifier;

import crac.models.CracUser;

public class NotificationWrapper {
	
	private String id;

	private CracUser target;
	
	private Notification notification;
		
	public NotificationWrapper(CracUser target, Notification notification, String id){
		this.target = target;
		this.notification = notification;
		this.id = id;
	}

	public CracUser getTarget() {
		return target;
	}

	public void setTarget(CracUser target) {
		this.target = target;
	}

	public Notification getNotification() {
		return notification;
	}

	public void setNotification(Notification notification) {
		this.notification = notification;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	

}
