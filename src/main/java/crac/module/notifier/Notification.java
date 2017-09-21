package crac.module.notifier;

import java.util.Calendar;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import crac.models.db.entities.CracUser.NotificationUser;
import crac.models.utility.NotificationConfiguration;
import crac.module.notifier.factory.NotificationFactory;
import crac.module.utility.RandomUtility;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class for the data-type notification
 * Extending classes have to implement different necessary methods
 * @author David Hondl
 *
 */
public abstract class Notification {
	
	@Getter
	@Setter
	private String notificationId;
	
	@Getter
	@Setter
	private NotificationUser sender;

	@Getter
	@Setter
	private NotificationUser target;
	
	@Getter
	@Setter
	private Calendar creationTime;
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private NotificationType type;
	
	@JsonIgnore
	@Getter
	@Setter
	private NotificationFactory nf;
		
	public Notification(String name, NotificationType type){
		this.name = name;
		this.type = type;
		this.creationTime = Calendar.getInstance();
		this.notificationId = RandomUtility.randomString(20);
	}
	
	/**
	 * Called upon destruction of the notification
	 */
	public void destroy(){
		nf.deleteNotificationById(this.getNotificationId());
	}
	
	/**
	 * Called upon accepting the notification and its implications (child-classes are responsible for implementation)
	 * @return String
	 */
	public abstract String accept();
	
	/**
	 * Called upon denying the notification and its implications (child-classes are responsible for implementation)
	 * @return String
	 */
	public abstract String deny();
	
	/**
	 * Configures the arbitrary and class-dependent attributes of given child-class (implementation in child-class)
	 * @param conf
	 */
	public abstract void configure(NotificationConfiguration conf);

}
