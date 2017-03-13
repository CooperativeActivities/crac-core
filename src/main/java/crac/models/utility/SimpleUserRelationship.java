package crac.models.utility;

import crac.models.db.entities.CracUser;

public class SimpleUserRelationship {
	
	private CracUser relatedUser;
	
	private double likeValue;
	
	private boolean friends;

	public SimpleUserRelationship(CracUser relatedUser, double likeValue, boolean friends) {
		this.relatedUser = relatedUser;
		this.likeValue = likeValue;
		this.friends = friends;
	}

	public CracUser getRelatedUser() {
		return relatedUser;
	}

	public void setRelatedUser(CracUser relatedUser) {
		this.relatedUser = relatedUser;
	}

	public double getLikeValue() {
		return likeValue;
	}

	public void setLikeValue(double likeValue) {
		this.likeValue = likeValue;
	}

	public boolean isFriends() {
		return friends;
	}

	public void setFriends(boolean friends) {
		this.friends = friends;
	}

}
