package crac.models.output;

import crac.models.db.relation.UserMaterialSubscription;

public class SubsrciptionDetails {

	private long id;
	
	private long userId;

	private String userName;
	
	private Long quantity;
	
	public SubsrciptionDetails(UserMaterialSubscription ums){
		this.id = ums.getId();
		this.userId = ums.getUser().getId();
		this.userName = ums.getUser().getName();
		this.quantity = ums.getQuantity();
	}

	public long getId() {
		return id;
	}

	public long getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public Long getQuantity() {
		return quantity;
	}
	
}
