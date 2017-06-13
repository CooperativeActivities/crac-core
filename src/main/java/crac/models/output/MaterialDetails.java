package crac.models.output;

import java.util.HashSet;
import java.util.Set;

import crac.models.db.entities.Material;
import crac.models.db.relation.UserMaterialSubscription;

public class MaterialDetails {

	private long id;

	private long quantity;

	private String name;

	private String description;

	private long subscribedQuantity;

	private Set<SubsrciptionDetails> subscribedUsers;

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
			subscribedUsers.add(new SubsrciptionDetails(ums));
		}
	}

	public long getId() {
		return id;
	}

	public long getQuantity() {
		return quantity;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Set<SubsrciptionDetails> getSubscribedUsers() {
		return subscribedUsers;
	}

	public long getSubscribedQuantity() {
		return subscribedQuantity;
	}

}
