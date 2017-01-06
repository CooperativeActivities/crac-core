package crac.models.storage;

public class SearchFilter {
	
	public SearchFilter(){
		proficiency = 1;
		like = 1;
		friends = 1;
		importance = 1;
	}
	
	private int proficiency;
	private int like;
	private int friends;
	private int importance;
	
	public boolean isSet(){
		if(proficiency == 1 || like == 1 || friends == 1 || importance == 1){
			return true;
		}else{
			return false;
		}
	}
	
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

	public int getImportance() {
		return importance;
	}

	public void setImportance(int importance) {
		this.importance = importance;
	}

}
