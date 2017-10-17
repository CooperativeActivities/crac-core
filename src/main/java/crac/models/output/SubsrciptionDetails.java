package crac.models.output;

import crac.models.db.relation.UserMaterialSubscription;
import lombok.Data;

@Data
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
	
}
