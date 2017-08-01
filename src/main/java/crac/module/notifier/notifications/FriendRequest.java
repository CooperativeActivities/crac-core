package crac.module.notifier.notifications;

import java.util.HashMap;

import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.relation.UserRelationship;
import crac.module.notifier.Notification;
import crac.module.notifier.NotificationType;

public class FriendRequest extends Notification{
		
	public FriendRequest(){
		super("Friend Request", NotificationType.REQUEST);
	}
	

	@Override
	public String accept() {
		
		UserRelationshipDAO userRelationshipDAO = super.getNf().getUserRelationshipDAO();
		CracUserDAO userDAO = super.getNf().getUserDAO();
		
		UserRelationship ur = new UserRelationship();
		
		ur.setC1(userDAO.findOne(super.getSenderId()));
		ur.setC2(userDAO.findOne(getTargetId()));
		ur.setFriends(true);
		ur.setLikeValue(1.2);
		
		userRelationshipDAO.save(ur);
		
		super.destroy();
		System.out.println("Friend-request accepted");
		return "accepted";
	}

	@Override
	public String deny() {
		super.destroy();
		System.out.println("Friend-request denied");
		return "denied";
		
	}


	@Override
	public void inject(HashMap<String, Long> ids) {
	}
	
}
