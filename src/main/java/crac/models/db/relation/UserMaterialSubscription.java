package crac.models.db.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.CracUser.UserShort;
import crac.models.db.entities.Material;
import crac.models.db.entities.Material.MaterialShort;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_material_subscription")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserMaterialSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	@Getter
	@Setter
	private long id;
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "user_id")
	@Getter
	@Setter
	private CracUser user;
	
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "material_id")
	@Getter
	@Setter
	private Material material;
	
	@Getter
	@Setter
	private Long quantity;
	
	@Getter
	@Setter
	private boolean fullfilled;
	
	public UserMaterialSubscription(){
		
	}
	
	public SubscriptionShort toShort(){
		SubscriptionShort s = new SubscriptionShort();
		s.setFullfilled(this.fullfilled);
		s.setUser(this.user.toShort());
		s.setMaterial(this.material.toShort());
		s.setQuantity(this.quantity);
		return s;
	}
	
	public UserMaterialSubscription(CracUser user, Material material, Long quantity) {
		this.user = user;
		this.material = material;
		this.quantity = quantity;
	}
	
	public class SubscriptionShort{
		
		@Getter
		@Setter
		private UserShort user;
		
		@Getter
		@Setter
		private MaterialShort material;
		
		@Getter
		@Setter
		private Long quantity;
		
		@Getter
		@Setter
		private boolean fullfilled;
		
		public SubscriptionShort(){
			
		}
		
	}
	
}


