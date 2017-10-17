package crac.module.notifier;

import java.util.Calendar;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import crac.models.db.entities.CracUser.NotificationUser;
import crac.models.utility.NotificationConfiguration;
import crac.module.factories.NotificationFactory;
import crac.module.utility.CracUtility;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class for the data-type notification
 * Extending classes have to implement different necessary methods
 * @author David Hondl
 *
 */

@Data
public abstract class Notification {
	
	private String notificationId;
	private NotificationUser sender;
	private NotificationUser target;
	private Calendar creationTime;
	private String name;
	private NotificationType type;
	
	@JsonIgnore
	private NotificationFactory nf;
		
	public Notification(String name, NotificationType type){
		this.name = name;
		this.type = type;
		this.creationTime = Calendar.getInstance();
		this.notificationId = CracUtility.randomString(20);
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
