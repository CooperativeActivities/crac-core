package crac.models;

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

import crac.relationmodels.CompetencePermissionType;

@Entity
@Table(name = "role")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Role {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private long id;

	@NotNull
	private String name;
		
	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "mapping_role_permissiontype", joinColumns={@JoinColumn(name="role_id")}, inverseJoinColumns={@JoinColumn(name="permissiontype_id")})
	Set<CompetencePermissionType> mappedPermissionTypes;
	
	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(mappedBy = "roles")
	Set<CracUser> mappedUser;

	public Role() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<CompetencePermissionType> getMappedPermissionTypes() {
		return mappedPermissionTypes;
	}

	public void setMappedPermissionTypes(Set<CompetencePermissionType> mappedPermissionTypes) {
		this.mappedPermissionTypes = mappedPermissionTypes;
	}

	public Set<CracUser> getMappedUser() {
		return mappedUser;
	}

	public void setMappedUser(Set<CracUser> mappedUser) {
		this.mappedUser = mappedUser;
	}


}
