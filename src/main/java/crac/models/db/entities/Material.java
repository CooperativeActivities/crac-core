package crac.models.db.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.relation.UserMaterialSubscription;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name = "material")
public class Material {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "material_id")
	private long id;

	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	@JoinColumn(name = "task")
	private Task task;

	@NotNull
	private long quantity;

	@NotNull
	private String name;

	@NotNull
	private String description;

	@OneToMany(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserMaterialSubscription> subscribedUsers;

	public Material() {
		this.subscribedUsers = new HashSet<UserMaterialSubscription>();
	}

	public String subscribable(Long additionalq) {

		if (additionalq <= 0) {
			return "QUANTITY_TOO_SMALL";
		}

		if (getsubscribedQuantity() + additionalq <= quantity) {
			return "OK";
		} else {
			return "QUANTITY_TOO_HIGH";
		}

	}
	
	public void update(Material m){
		if (m.getName() != null) {
			this.setName(m.getName());
		}
		if (m.getDescription() != null) {
			this.setDescription(m.getDescription());
		}
		if (m.getQuantity() > 0) {
			this.setQuantity(m.getQuantity());
		}
	}

	public String subscribable(Long additionalq, UserMaterialSubscription um) {
		if (additionalq <= 0) {
			return "QUANTITY_TOO_SMALL";
		}

		if ((getsubscribedQuantity() - um.getQuantity()) + additionalq <= quantity) {
			return "OK";
		} else {
			return "QUANTITY_TOO_HIGH";
		}
		
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getQuantity() {
		return quantity;
	}

	public Long getsubscribedQuantity() {
		Long currentq = 0l;
		for (UserMaterialSubscription subscription : subscribedUsers) {
			currentq += subscription.getQuantity();
		}
		return currentq;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<UserMaterialSubscription> getSubscribedUsers() {
		return subscribedUsers;
	}

	public void addUserSubscription(UserMaterialSubscription subscription) {
		this.subscribedUsers.add(subscription);
	}

	public void setSubscribedUsers(Set<UserMaterialSubscription> subscribedUsers) {
		this.subscribedUsers = subscribedUsers;
	}

}
