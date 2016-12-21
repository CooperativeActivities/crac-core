package crac.models.storage;

public class SearchFilter {
	
	public SearchFilter(){
		
	}
	
	private int proficiency;
	private int like;
	private int friends;
	
	public int getProficiency() {
		return proficiency;
	}
	
	public void setProficiency(int proficiency) {
		this.proficiency = proficiency;
	}
	
	public int getLike() {
		return like;
	}
	
	public void setLike(int like) {
		this.like = like;
	}
	
	public int getFriends() {
		return friends;
	}
	
	public void setFriends(int friends) {
		this.friends = friends;
	}

}
