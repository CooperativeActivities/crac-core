package crac.elastic;

import java.util.Date;
import java.util.Set;

import crac.models.Attachment;
import crac.models.Competence;
import crac.models.Group;
import crac.models.Project;
import crac.models.Role;
import crac.models.Task;

public class ElasticPerson {

	private long id;

	private String name;
	
	/*
	private String email;
	
	private String lastName;
	
	private String firstName;
	
	private Date birthDate;

	private String status;
	
	private String phone;
	
	private String address;
	
	private Role role;

	private Set<Task> createdTasks;

	private Set<Project> createdProjects;

	private Set<Competence> createdCompetences;

	private Set<Group> createdGroups;
	*/

	private Set<Competence> statedCompetences;
	
	private Set<Competence> relatedCompetences;
	
	private Set<Competence> likes;
	
	private Set<Competence> dislikes;
	
	/*

	private Set<Task> openTasks;
	
	private Set<Task> responsibleForTasks;

	private Set<Task> followingTasks;
	
	private Set<Group> groups;
	
	private Attachment userImage;
	
	*/

	public ElasticPerson(long id, String name, /*String email, String lastName, String firstName,
			Date birthDate, String status, String phone, String address, Role role, Set<Task> createdTasks,
			Set<Project> createdProjects, Set<Competence> createdCompetences, Set<Group> createdGroups,
			*/Set<Competence> statedCompetences, Set<Competence> likes, Set<Competence> dislikes/*, Set<Task> openTasks,
			Set<Task> responsibleForTasks, Set<Task> followingTasks, Set<Group> groups, Attachment userImage*/) {
		this.id = id;
		this.name = name;
		/*
		this.email = email;
		this.lastName = lastName;
		this.firstName = firstName;
		this.birthDate = birthDate;
		this.status = status;
		this.phone = phone;
		this.address = address;
		this.role = role;
		this.createdTasks = createdTasks;
		this.createdProjects = createdProjects;
		this.createdCompetences = createdCompetences;
		this.createdGroups = createdGroups;
		*/
		this.statedCompetences = statedCompetences;
		this.likes = likes;
		this.dislikes = dislikes;
		/*
		this.openTasks = openTasks;
		this.responsibleForTasks = responsibleForTasks;
		this.followingTasks = followingTasks;
		this.groups = groups;
		this.userImage = userImage;
		*/
	}

	public ElasticPerson() {
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
/*
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

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Set<Task> getCreatedTasks() {
		return createdTasks;
	}

	public void setCreatedTasks(Set<Task> createdTasks) {
		this.createdTasks = createdTasks;
	}

	public Set<Project> getCreatedProjects() {
		return createdProjects;
	}

	public void setCreatedProjects(Set<Project> createdProjects) {
		this.createdProjects = createdProjects;
	}

	public Set<Competence> getCreatedCompetences() {
		return createdCompetences;
	}

	public void setCreatedCompetences(Set<Competence> createdCompetences) {
		this.createdCompetences = createdCompetences;
	}

	public Set<Group> getCreatedGroups() {
		return createdGroups;
	}

	public void setCreatedGroups(Set<Group> createdGroups) {
		this.createdGroups = createdGroups;
	}
*/
	public Set<Competence> getStatedCompetences() {
		return statedCompetences;
	}

	public void setStatedCompetences(Set<Competence> statedCompetences) {
		this.statedCompetences = statedCompetences;
	}

	public Set<Competence> getRelatedCompetences() {
		return relatedCompetences;
	}

	public void setRelatedCompetences(Set<Competence> relatedCompetences) {
		this.relatedCompetences = relatedCompetences;
	}

	public Set<Competence> getLikes() {
		return likes;
	}

	public void setLikes(Set<Competence> likes) {
		this.likes = likes;
	}

	public Set<Competence> getDislikes() {
		return dislikes;
	}

	public void setDislikes(Set<Competence> dislikes) {
		this.dislikes = dislikes;
	}
/*
	public Set<Task> getOpenTasks() {
		return openTasks;
	}

	public void setOpenTasks(Set<Task> openTasks) {
		this.openTasks = openTasks;
	}

	public Set<Task> getResponsibleForTasks() {
		return responsibleForTasks;
	}

	public void setResponsibleForTasks(Set<Task> responsibleForTasks) {
		this.responsibleForTasks = responsibleForTasks;
	}

	public Set<Task> getFollowingTasks() {
		return followingTasks;
	}

	public void setFollowingTasks(Set<Task> followingTasks) {
		this.followingTasks = followingTasks;
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

	*/
}
