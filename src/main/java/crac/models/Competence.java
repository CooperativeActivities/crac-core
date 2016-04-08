package crac.models;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
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
	@Autowired
	@Column(name = "competence_id")
	private long id;

	/**
	 * defines a many to many relation with the task-entity
	 */

	@ManyToMany(mappedBy = "neededCompetences")
	private Set<Task> connectedTasks;

	/**
	 * defines a many to many relation with the cracUser-entity
	 */

	@ManyToMany(mappedBy = "competences")
	private Set<CracUser> users;

	@NotNull
	@Autowired
	private String name;

	/**
	 * defines a one to many relation with the cracUser-entity
	 */

	@Autowired
	@ManyToOne
	@JoinColumn(name = "creator_id")
	private CracUser creator;

	/**
	 * constructors
	 */

	public Competence(String name) {
		this.name = name;
	}

	public Competence() {
		this.name = "default";
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

}
