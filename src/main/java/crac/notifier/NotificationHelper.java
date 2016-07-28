package crac.notifier;

import java.util.ArrayList;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.models.CracUser;
import crac.notifier.notifications.FriendRequest;
import crac.utility.JSonResponseHelper;

public class NotificationHelper {
	
	public static void createFriendRequest(CracUser sender, CracUser receiver){
		NotificationDistributor n1 = NotificationDistributor.getInstance();
		n1.addNotification(receiver, new FriendRequest(sender));
	}
	
	public static void delelteNotification(String id){
		NotificationDistributor n1 = NotificationDistributor.getInstance();
		n1.deleteNotification(id);
	}
	
	public static ArrayList<NotificationWrapper> getAllNotifications(){
		NotificationDistributor n = NotificationDistributor.getInstance();
		return n.getWrappedNotifications();
	}
	
	public static ArrayList<NotificationWrapper> getUserNotifications(CracUser user){
		NotificationDistributor n = NotificationDistributor.getInstance();
		ArrayList<NotificationWrapper> list = new ArrayList<NotificationWrapper>();
		
		for(NotificationWrapper nw : n.getWrappedNotifications()){
			if(nw.getTarget() == user){
				list.add(nw);
			}
		}
		
		return list;
		
	}

}
