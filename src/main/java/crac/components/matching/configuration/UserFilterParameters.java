package crac.components.matching.configuration;

public class UserFilterParameters {
	
	public UserFilterParameters(){
		friends = 0;
	}
	
	private int friends;
	
	public boolean isSet(){
		if(friends == 1){
			return true;
		}else{
			return false;
		}
	}
		
	public int getFriends() {
		return friends;
	}
	
	public void setFriends(int friends) {
		this.friends = friends;
	}

}
