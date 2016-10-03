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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.relationmodels.CompetencePermissionType;
import crac.relationmodels.CompetenceRelationship;
import crac.relationmodels.CompetenceTaskRel;
import crac.relationmodels.UserCompetenceRel;

/**
 * The competence-entity.
 */

@Entity
@Table(name = "competences")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Competence {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "competence_id")
	private long id;
		
	@NotNull
	private String name;
	
	@NotNull
	private String description;

	/**
	 * defines a many to many relation with the cracUser-entity
	 */
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "competence", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserCompetenceRel> userRelationships;

	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "permission_type_id")
	private CompetencePermissionType permissionType;

	/**
	 * defines a one to many relation with the cracUser-entity
	 */

	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "creator_id")
	private CracUser creator;
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "competence1", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<CompetenceRelationship> mappedCompetence1;

	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "competence2", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<CompetenceRelationship> mappedCompetence2;
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "competence", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<CompetenceTaskRel> competenceTaskRels;


	/**
	 * constructors
	 */

	public Competence(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public Competence() {
		this.name = "";
		this.description = "";
	}

	/**
	 * getters and setters
	 */

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

	public CracUser getCreator() {
		return creator;
	}

	public void setCreator(CracUser creator) {
		this.creator = creator;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<CompetenceRelationship> getMappedCompetence1() {
		return mappedCompetence1;
	}

	public void setMappedCompetence1(Set<CompetenceRelationship> mappedCompetence1) {
		this.mappedCompetence1 = mappedCompetence1;
	}

	public Set<CompetenceRelationship> getMappedCompetence2() {
		return mappedCompetence2;
	}

	public void setMappedCompetence2(Set<CompetenceRelationship> mappedCompetence2) {
		this.mappedCompetence2 = mappedCompetence2;
	}

	public Set<UserCompetenceRel> getUserRelationships() {
		return userRelationships;
	}

	public void setUserRelationships(Set<UserCompetenceRel> userRelationships) {
		this.userRelationships = userRelationships;
	}

	public CompetencePermissionType getPermissionType() {
		return permissionType;
	}

	public void setPermissionType(CompetencePermissionType permissionType) {
		this.permissionType = permissionType;
	}

	public Set<CompetenceTaskRel> getCompetenceTaskRels() {
		return competenceTaskRels;
	}

	public void setCompetenceTaskRels(Set<CompetenceTaskRel> competenceTaskRels) {
		this.competenceTaskRels = competenceTaskRels;
	}
	
}
