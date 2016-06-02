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

/**
 * The competence-entity.
 */

@Entity
@Table(name = "competences")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Competence {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "competence_id")
	private long id;
	
	/**
	 * Defines a one to many relationship to itself, to provide the possibility to add parent-child relationships to competences
	 */
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "parent_competence")
	private Competence parentCompetence;
	
	@OneToMany(mappedBy = "parentCompetence", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Competence> childCompetences;
	
	
	/**
	 * defines a many to many relation with the task-entity
	 */

	@ManyToMany(mappedBy = "neededCompetences", fetch = FetchType.LAZY)
	@JsonIdentityReference(alwaysAsId=true)
	private Set<Task> connectedTasks;

	/**
	 * defines a many to many relation with the cracUser-entity
	 */

	@ManyToMany(mappedBy = "competences", fetch = FetchType.LAZY)
	@JsonIdentityReference(alwaysAsId=true)
	private Set<CracUser> users;
	
	@ManyToMany(mappedBy = "likes", fetch = FetchType.LAZY)
	@JsonIdentityReference(alwaysAsId=true)
	private Set<CracUser> likedBy;

	@ManyToMany(mappedBy = "dislikes", fetch = FetchType.LAZY)
	@JsonIdentityReference(alwaysAsId=true)
	private Set<CracUser> dislikedBy;


	@NotNull
	private String name;
	
	@NotNull
	private String description;

	/**
	 * defines a one to many relation with the cracUser-entity
	 */

	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "creator_id")
	private CracUser creator;

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

	public Set<CracUser> getUsers() {
		return users;
	}

	public void setUsers(Set<CracUser> users) {
		this.users = users;
	}

	public CracUser getCreator() {
		return creator;
	}

	public void setCreator(CracUser creator) {
		this.creator = creator;
	}

	public Set<Task> getConnectedTasks() {
		return connectedTasks;
	}

	public void setConnectedTasks(Set<Task> connectedTasks) {
		this.connectedTasks = connectedTasks;
	}

	public Competence getParentCompetence() {
		return parentCompetence;
	}

	public void setParentCompetence(Competence parentCompetence) {
		this.parentCompetence = parentCompetence;
	}

	public Set<Competence> getChildCompetences() {
		return childCompetences;
	}

	public void setChildCompetences(Set<Competence> childCompetences) {
		this.childCompetences = childCompetences;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
