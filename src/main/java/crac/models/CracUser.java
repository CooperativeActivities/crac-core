package crac.models;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.persistence.JoinColumn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * The cracUser-entity.
 */

@Entity
@Table(name = "users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CracUser {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id")
	private long id;

	@NotNull
	private String name;
	
	@NotNull
	private String email;
	
	@NotNull
	private String password;
	
	@NotNull
	private String lastName;
	
	@NotNull
	private String firstName;
	
	private Date birthDate;

	private String status;
	
	@NotNull
	private String phone;
	
	private String address;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private Role role;

	/**
	 * defines a one to many relation with the task-entity
	 */

	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
	private Set<Task> createdTasks;

	/**
	 * defines a one to many relation with the task-entity
	 */

	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
	private Set<Project> createdProjects;

	
	/**
	 * defines a one to many relation with the competence-entity
	 */

	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
	private Set<Competence> createdCompetences;

	/**
	 * defines a one to many relation with the group-entity
	 */

	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
	private Set<Group> createdGroups;

	/**
	 * defines a many to many relation with the competence-entity
	 */
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserCompetenceRel> competenceRelationships;



	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserTaskRel> taskRelationships;
	
	/**
	 * defines a many to many relation with the group-entity
	 */
	
	@ManyToMany(mappedBy = "enroledUsers", fetch = FetchType.LAZY)
	@JsonIdentityReference(alwaysAsId=true)
	private Set<Group> groups;
	
	/**
	 * defines a one to one relation with the attachment-entity
	 */
	
	@OneToOne
    @JoinColumn(name = "user_image")
	private Attachment userImage;

	/**
	 * constructors
	 */
	
	public CracUser(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public CracUser() {
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<Task> getCreatedTasks() {
		return createdTasks;
	}

	public void setCreatedTasks(Set<Task> createdTasks) {
		this.createdTasks = createdTasks;
	}

	public Set<Competence> getCreatedCompetences() {
		return createdCompetences;
	}

	public void setCreatedCompetences(Set<Competence> createdCompetences) {
		this.createdCompetences = createdCompetences;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public Set<Group> getCreatedGroups() {
		return createdGroups;
	}

	public void setCreatedGroups(Set<Group> createdGroups) {
		this.createdGroups = createdGroups;
	}
	
	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	public Attachment getUserImage() {
		return userImage;
	}

	public void setUserImage(Attachment userImage) {
		this.userImage = userImage;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Set<Project> getCreatedProjects() {
		return createdProjects;
	}

	public void setCreatedProjects(Set<Project> createdProjects) {
		this.createdProjects = createdProjects;
	}

	public Set<UserCompetenceRel> getCompetenceRelationships() {
		return competenceRelationships;
	}

	public void setCompetenceRelationships(Set<UserCompetenceRel> competenceRelationships) {
		this.competenceRelationships = competenceRelationships;
	}

	public Set<UserTaskRel> getTaskRelationships() {
		return taskRelationships;
	}

	public void setTaskRelationships(Set<UserTaskRel> taskRelationships) {
		this.taskRelationships = taskRelationships;
	}

}
