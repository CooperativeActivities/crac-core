package crac.relationmodels;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.enums.Role;
import crac.models.Competence;

@Entity
@Table(name = "competence_permission_type")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CompetencePermissionType {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "type_id")
	private long id;

	@NotNull
	private String name;
	
	private String description;
	
	private boolean self;
	
	private Role neededRole;
		
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "permissionType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Competence> permittedCompetences;

	public CompetencePermissionType() {
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isSelf() {
		return self;
	}

	public void setSelf(boolean self) {
		this.self = self;
	}


	public Role getNeededRole() {
		return neededRole;
	}

	public void setNeededRole(Role neededRole) {
		this.neededRole = neededRole;
	}

	public Set<Competence> getPermittedCompetences() {
		return permittedCompetences;
	}

	public void setPermittedCompetences(Set<Competence> permittedCompetences) {
		this.permittedCompetences = permittedCompetences;
	}

	
}
