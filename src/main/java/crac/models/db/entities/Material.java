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
import lombok.Getter;
import lombok.Setter;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name = "material")
public class Material {

	@Getter
	@Setter
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "material_id")
	private long id;

	@Getter
	@Setter
	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	@JoinColumn(name = "task")
	private Task task;

	@Getter
	@Setter
	@NotNull
	private long quantity;

	@Getter
	@Setter
	@NotNull
	private String name;

	@Getter
	@Setter
	@NotNull
	private String description;

	@Getter
	@Setter
	@OneToMany(mappedBy = "material", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserMaterialSubscription> subscribedUsers;

	public Material() {
		this.subscribedUsers = new HashSet<UserMaterialSubscription>();
	}
	
	public Material copy(Task t){
		Material m = new Material();
		m.setDescription(description);
		m.setName(name);
		m.setQuantity(quantity);
		m.setTask(t);
		return m;
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

	public Long getsubscribedQuantity() {
		Long currentq = 0l;
		for (UserMaterialSubscription subscription : subscribedUsers) {
			currentq += subscription.getQuantity();
		}
		return currentq;
	}


	public void addUserSubscription(UserMaterialSubscription subscription) {
		this.subscribedUsers.add(subscription);
	}
	
	public MaterialShort toShort(){
		MaterialShort m = new MaterialShort();
		m.setDescription(this.description);
		m.setName(this.name);
		m.setQuantity(this.quantity);
		return m;
	}

	public class MaterialShort{
		
		@Getter
		@Setter
		private long quantity;

		@Getter
		@Setter
		private String name;

		@Getter
		@Setter
		private String description;
		
		public MaterialShort(){
			
		}
	}
	
}
