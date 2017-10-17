package crac.models.db.relation;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.entities.Competence;
import crac.models.db.entities.Role;
import lombok.Data;

@Data
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
	
	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(mappedBy = "mappedPermissionTypes")
	Set<Role> roles;

		
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "permissionType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Competence> permittedCompetences;

	public CompetencePermissionType() {
	}

}
