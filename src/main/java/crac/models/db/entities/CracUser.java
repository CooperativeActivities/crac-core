package crac.models.db.entities;

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
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.persistence.JoinColumn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.db.relation.UserCompetenceRel;
import crac.models.db.relation.UserMaterialSubscription;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;

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
	@Column(name="last_name")
	private String lastName;
	
	@NotNull
	@Column(name="first_name")
	private String firstName;
	
	@Column(name="birth_date")
	private Date birthDate;

	private String status;
	
	@NotNull
	private String phone;
	
	private String address;
		
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
	private Set<CracGroup> createdGroups;

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
	private Set<CracGroup> groups;
	
	/**
	 * defines a one to one relation with the attachment-entity
	 */
	
	@OneToOne
    @JoinColumn(name = "user_image")
	private Attachment userImage;
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "mapping_role_user", joinColumns={@JoinColumn(name="user_id")}, inverseJoinColumns={@JoinColumn(name="role_id")})
	Set<Role> roles;
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserMaterialSubscription> subscribedMaterials;
	
	/**
	 * constructors
	 */
	
	public CracUser() {
		this.competenceRelationships = new HashSet<UserCompetenceRel>();
		this.roles = new HashSet<Role>(); 
	}
	
	@JsonIgnore
	public void update(CracUser u){
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

		if (u.getPassword() != null) {
			this.setPassword(bcryptEncoder.encode(u.getPassword()));
		}
		
		if (u.getName() != null) {
			this.setName(u.getName());
		}
		if (u.getFirstName() != null) {
			this.setFirstName(u.getFirstName());
		}
		if (u.getLastName() != null) {
			this.setLastName(u.getLastName());
		}
		if (u.getBirthDate() != null) {
			this.setBirthDate(u.getBirthDate());
		}
		if (u.getEmail() != null) {
			this.setEmail(u.getEmail());
		}
		if (u.getAddress() != null) {
			this.setAddress(u.getAddress());
		}
		if (u.getPhone() != null) {
			this.setPhone(u.getPhone());
		}
		if (u.getStatus() != null) {
			this.setStatus(u.getStatus());
		}
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

	public Set<CracGroup> getCreatedGroups() {
		return createdGroups;
	}

	public void setCreatedGroups(Set<CracGroup> createdGroups) {
		this.createdGroups = createdGroups;
	}
	
	public Set<CracGroup> getGroups() {
		return groups;
	}

	public void setGroups(Set<CracGroup> groups) {
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
	
	public void addRole(Role role){
		this.roles.add(role);
	}

	public Set<UserMaterialSubscription> getSubscribedMaterials() {
		return subscribedMaterials;
	}

	public void setSubscribedMaterials(Set<UserMaterialSubscription> subscribedMaterials) {
		this.subscribedMaterials = subscribedMaterials;
	}
	
}
