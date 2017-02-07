package crac.models.output;

import crac.models.CracUser;

public class UserFriendDetails {

	private long id;

	private String name;

	private boolean friend;
	
	private boolean self;
	
	public UserFriendDetails(CracUser u, boolean friend) {
		this.id = u.getId();
		this.name = u.getName();
		this.friend = friend;
		this.self = false;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFriend() {
		return friend;
	}

	public void setFriend(boolean friend) {
		this.friend = friend;
	}

	public boolean isSelf() {
		return self;
	}

	public void setSelf(boolean self) {
		this.self = self;
	}
	
}
