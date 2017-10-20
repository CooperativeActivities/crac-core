package crac.models.db.entities;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.relation.CompetencePermissionType;
import lombok.Getter;
import lombok.Setter;

/**
 * The role entity
 * @author David Hondl
 *
 */
@Entity
@Table(name = "role")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Role {
	
	@Getter
	@Setter
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private long id;

	@Getter
	@Setter
	@NotNull
	private String name;
		
	@Getter
	@Setter
	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "mapping_role_permissiontype", joinColumns={@JoinColumn(name="role_id")}, inverseJoinColumns={@JoinColumn(name="permissiontype_id")})
	Set<CompetencePermissionType> mappedPermissionTypes;
	
	@Getter
	@Setter
	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(mappedBy = "roles")
	Set<CracUser> mappedUser;

	public Role() {
	}

	public RoleShort toShort(){
		RoleShort r = new RoleShort();
		r.setId(this.id);
		r.setName(this.name);
		return r;
	}

	public class RoleShort {
		
		@Getter
		@Setter
		private long id;

		@Getter
		@Setter
		private String name;

		public RoleShort(){
		}

	}

}
