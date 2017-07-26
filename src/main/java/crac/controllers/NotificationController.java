package crac.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import crac.components.matching.factories.NotificationFactory;
import crac.components.notifier.Notification;
import crac.components.utility.JSONResponseHelper;
import crac.enums.ErrorCause;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.CracUser;

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

	@Autowired
	private NotificationFactory nf;

	/**
	 * Returns all notifications, which target the logged in user
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getNotifications() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		return JSONResponseHelper.createResponse(nf.getUserNotifications(user), true);
	}
	
	/**
	 * Returns all notifications in the system
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/admin/", "/admin" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getAllNotifications() {
		return JSONResponseHelper.createResponse(nf.getNotifications(), true);
	}

	/**
	 * Triggers the accept-method of the notification and deletes it
	 * @param notificationId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{notification_id}/accept", "/friend/{notification_id}/accept/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> acceptFriend(@PathVariable(value = "notification_id") String notificationId) {
		Notification n = nf.getNotificationById(notificationId);
		
		if(n != null){
			String message = n.accept();
			HashMap<String, Object> meta = new HashMap<>();
			meta.put("message", message);
			return JSONResponseHelper.createResponse(n, true, meta);
			//return JSonResponseHelper.successfullyAccepted(n, message);
		}else{
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
		Notification n = nf.getNotificationById(notificationId);
		
		if(n != null){
			String message = n.deny();
			HashMap<String, Object> meta = new HashMap<>();
			meta.put("message", message);
			return JSONResponseHelper.createResponse(n, true, meta);
			//return JSonResponseHelper.successfullyDenied(n, message);
		}else{
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}
	}
	
}
