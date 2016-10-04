package crac.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.daos.UserRelationshipDAO;
import crac.daos.UserTaskRelDAO;
import crac.models.CracUser;
import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.utility.JSonResponseHelper;

@RestController
@RequestMapping("/notification")

public class NotificationController {
	
	@Autowired
	private CracUserDAO userDAO;
	
	@Autowired
	private TaskDAO taskDAO;
	
	@Autowired
	private UserTaskRelDAO userTaskRelDAO;
	
	@Autowired
	private UserRelationshipDAO userRelationshipDAO;

	/**
	 * Returns all notifications, which target the logged in user
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getNotifications() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());
		return ResponseEntity.ok().body(NotificationHelper.notificationsToString(NotificationHelper.getUserNotifications(user)));
	}
	
	/**
	 * Returns all notifications in the system
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/admin/", "/admin" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getAllNotifications() {
	return ResponseEntity.ok().body(NotificationHelper.notificationsToString(NotificationHelper.getAllNotifications()));
	}

	/**
	 * Triggers the accept-method of the notification and deletes it
	 * @param notificationId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{notification_id}/accept", "/friend/{notification_id}/accept/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> acceptFriend(@PathVariable(value = "notification_id") String notificationId) {
		Notification n = NotificationHelper.getNotificationById(notificationId);
		
		if(n != null){
			HashMap<String, CrudRepository> map = new HashMap<String, CrudRepository>();
			map.put("taskDAO", taskDAO);
			map.put("userTaskRelDAO", userTaskRelDAO);
			map.put("userDAO", userDAO);
			map.put("userRelationshipDAO", userRelationshipDAO);
			String message = n.accept(map);
			return JSonResponseHelper.successfullyAccepted(n, message);
		}else{
			return JSonResponseHelper.noSuchNotification();
		}
		

	}
	
	/**
	 * Triggers the deny-method of the notification and deletes it
	 * @param notificationId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{notification_id}/deny", "/{notification_id}/deny/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> denyFriend(@PathVariable(value = "notification_id") String notificationId) {
		Notification n = NotificationHelper.getNotificationById(notificationId);
		
		if(n != null){
			String message = n.deny();
			return JSonResponseHelper.successfullyDenied(n, message);
		}else{
			return JSonResponseHelper.noSuchNotification();
		}
	}
	
}
