package crac.models.output;

import java.util.HashSet;
import java.util.Set;

import crac.models.db.entities.Material;
import crac.models.db.relation.UserMaterialSubscription;
import crac.models.db.relation.UserMaterialSubscription.SubscriptionShort;
import lombok.Data;

/**
 * Helper class for output, that contains information about material-details
 * @author David Hondl
 *
 */
@Data
public class MaterialDetails {

	private long id;
	private long quantity;
	private String name;
	private String description;
	private long subscribedQuantity;
	private Set<SubscriptionShort> subscribedUsers;

	public MaterialDetails(Material t) {
		this.id = t.getId();
		this.quantity = t.getQuantity();
		this.name = t.getName();
		this.description = t.getDescription();
		this.subscribedQuantity = t.getsubscribedQuantity();
		addSubs(t.getSubscribedUsers());
	}

	private void addSubs(Set<UserMaterialSubscription> subs) {
		subscribedUsers = new HashSet<>();
		for (UserMaterialSubscription ums : subs) {
			subscribedUsers.add(ums.toShort());
		}
	}

}
