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
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.relation.CompetencePermissionType;
import crac.models.db.relation.CompetenceRelationship;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;

/**
 * The competence-entity.
 */

@Entity
@Table(name = "competences")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Competence {

	@Id
	@Column(name = "competence_id")
	private long id;
		
	@NotNull
	private String name;
	
	@NotNull
	@Type(type="text")
	private String description;
	
	//Defines the KOMET-ID this competence is from
	@Column(name = "komet_id")
	private int kometId;
	
	//Defines the ID of the competence in the KOMET DB
	@Column(name = "source_id")
	private int sourceId;
	
	/**
	 * defines a many to many relation with the cracUser-entity
	 */
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "competence", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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
	@OneToMany(mappedBy = "competence1", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<CompetenceRelationship> mappedCompetence1;

	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "competence2", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<CompetenceRelationship> mappedCompetence2;
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "competence", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<CompetenceTaskRel> competenceTaskRels;

	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "mapping_competencearea_competence", joinColumns={@JoinColumn(name="competence_id")}, inverseJoinColumns={@JoinColumn(name="competenceArea_id")})
	Set<CompetenceArea> competenceAreas;
	
	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "mapping_question_competence", joinColumns={@JoinColumn(name="competence_id")}, inverseJoinColumns={@JoinColumn(name="question_id")})
	Set<Question> mappedQuestions;
	


	/**
	 * constructors
	 */

	public Competence(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public Competence() {
		this.name = "default";
		this.description = "default";
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

	public Set<CompetenceArea> getCompetenceAreas() {
		return competenceAreas;
	}

	public void setCompetenceAreas(Set<CompetenceArea> competenceAreas) {
		this.competenceAreas = competenceAreas;
	}

	public int getKometId() {
		return kometId;
	}

	public void setKometId(int kometId) {
		this.kometId = kometId;
	}

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public Set<Question> getMappedQuestions() {
		return mappedQuestions;
	}

	public void setMappedQuestions(Set<Question> mappedQuestions) {
		this.mappedQuestions = mappedQuestions;
	}
	
}
