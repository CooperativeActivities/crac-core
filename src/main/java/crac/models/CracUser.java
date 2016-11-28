package crac.models;

import java.util.Date;
import java.util.HashSet;
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.relationmodels.UserCompetenceRel;
import crac.relationmodels.UserRelationship;
import crac.relationmodels.UserTaskRel;

/**
 * The cracUser-entity.
 */

@Entity
@Table(name = "users")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CracUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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
		
	/**
	 * defines a one to many relation with the evaluation-entity
	 */

	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private Set<Evaluation> evaluations;
	
	/**
	 * defines a one to many relation with the task-entity
	 */

	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
	private Set<Task> createdTasks;
	
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
	 * defines a many to many relation with the UserCompetenceRel-entity
	 */
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserCompetenceRel> competenceRelationships;
	
	/**
	 * defines two one-to-many relationships with the UserRelationship-entity
	 */

	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "c1", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserRelationship> userRelationshipsAs1;
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "c2", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserRelationship> userRelationshipsAs2;

	/**
	 * defines a one to many relation with the UserTaskRel-entity
	 */

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
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "mapping_role_user", joinColumns={@JoinColumn(name="user_id")}, inverseJoinColumns={@JoinColumn(name="role_id")})
	Set<Role> roles;
	
	/**
	 * constructors
	 */
	
	public CracUser() {
		this.competenceRelationships = new HashSet<UserCompetenceRel>();
	}
	
	//UTILITY----------------
	
	@JsonIgnore
	public boolean hasTaskPermissions(Task t){
		System.out.println("ADMIN: "+confirmRole("ADMIN"));
		System.out.println("LEADER: "+t.getAllLeaders().contains(this));
		return confirmRole("ADMIN") || t.getAllLeaders().contains(this);
	}
	
	@JsonIgnore
	public boolean confirmRole(String name){
		for(Role role : roles){
			if(role.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	@JsonIgnore
	public boolean confirmRole(Role role){
		return roles.contains(role);
	}
	
	//------------------------
	
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

	public Set<Evaluation> getEvaluations() {
		return evaluations;
	}

	public void setEvaluations(Set<Evaluation> evaluations) {
		this.evaluations = evaluations;
	}

	public Set<UserRelationship> getUserRelationshipsAs1() {
		return userRelationshipsAs1;
	}

	public void setUserRelationshipsAs1(Set<UserRelationship> userRelationshipsAs1) {
		this.userRelationshipsAs1 = userRelationshipsAs1;
	}

	public Set<UserRelationship> getUserRelationshipsAs2() {
		return userRelationshipsAs2;
	}

	public void setUserRelationshipsAs2(Set<UserRelationship> userRelationshipsAs2) {
		this.userRelationshipsAs2 = userRelationshipsAs2;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
}
