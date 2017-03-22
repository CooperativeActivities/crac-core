package crac.notifier.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.relation.UserRelationship;
import crac.notifier.Notification;
import crac.notifier.NotificationHelper;
import crac.notifier.NotificationType;
import crac.utility.DataAccess;

public class FriendRequest extends Notification{
	
	private long senderId;
	
	public FriendRequest(Long senderId, Long targetId){
		super("Friend Request", NotificationType.REQUEST, targetId);
		this.senderId = senderId;
	}
	
	public long getSenderId() {
		return senderId;
	}

	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}

	@Override
	public String toJSon() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString((FriendRequest)this);
		} catch (JsonProcessingException e) {
			return e.toString();
		}
	}

	@Override
	public String accept() {
		
		UserRelationshipDAO userRelationshipDAO = DataAccess.getRepo(UserRelationshipDAO.class);
		CracUserDAO userDAO = DataAccess.getRepo(CracUserDAO.class);
		
		UserRelationship ur = new UserRelationship();
		
		ur.setC1(userDAO.findOne(senderId));
		ur.setC2(userDAO.findOne(getTargetId()));
		ur.setFriends(true);
		ur.setLikeValue(1.2);
		
		userRelationshipDAO.save(ur);
		
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Friend-request accepted");
		return "accepted";
	}

	@Override
	public String deny() {
		NotificationHelper.deleteNotification(this.getNotificationId());
		System.out.println("Friend-request denied");
		return "denied";
		
	}
	
}
