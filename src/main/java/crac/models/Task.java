package crac.models;

import java.sql.Timestamp;
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
 * The task-entity.
 */
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "task_id")
	private long id;

	/**
	 * Defines a one to many relationship to itself, to provide the possibility to add subtasks to tasks
	 */
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "super_task")
	private Task superTask;
	
	@OneToMany(mappedBy = "superTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Task> childTasks;

	
	/**
	 * defines a many to many relation with the competence-entity
	 */
	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "task_competences", joinColumns = { @JoinColumn(name = "task_id") }, inverseJoinColumns = {
			@JoinColumn(name = "competence_id") })
	private Set<Competence> neededCompetences;

	/**
	 * defines a many to many relation with the cracUser-entity
	 */
	@ManyToMany(mappedBy = "openTasks", fetch = FetchType.LAZY)
	@JsonIdentityReference(alwaysAsId=true)
	private Set<CracUser> signedUsers;
	
	/**
	 * defines a many to many relation with the cracUser-entity
	 */
	@ManyToMany(mappedBy = "responsibleForTasks", fetch = FetchType.LAZY)
	@JsonIdentityReference(alwaysAsId=true)
	private Set<CracUser> responsibleUsers;
	
	/**
	 * defines a many to many relation with the cracUser-entity
	 */
	@ManyToMany(mappedBy = "followingTasks", fetch = FetchType.LAZY)
	@JsonIdentityReference(alwaysAsId=true)
	private Set<CracUser> followingUsers;

	@NotNull
	private String name;

	@NotNull
	private String description;

	@NotNull
	private String location;
	
	@NotNull
	private Timestamp startTime;
	
	@Autowired
	@NotNull
	private Timestamp endTime;
	
	@NotNull
	private int urgency;
	
	@NotNull
	private int amountOfVolunteers;
	
	private String feedback;
	
	/**
	 * defines a one to many relation with the cracUser-entity
	 */
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "creator_id")
	private CracUser creator;

	/**
	 * defines a one to many relation with the attachment-entity
	 */
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Attachment> attachments;

	/**
	 * defines a one to many relation with the attachment-entity
	 */
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Comment> comments;

	/**
	 * constructors
	 */

	public Task() {
		java.util.Date date= new java.util.Date();
		this.name = "default";
		this.description = "default";
		this.location = "default";
		this.startTime = new Timestamp(date.getTime());
		this.endTime = new Timestamp(date.getTime());
		this.urgency = 0;
		this.amountOfVolunteers = 0;
	}
	
	public Task(String name, String description) {
		java.util.Date date= new java.util.Date();
		this.name = name;
		this.description = description;
		this.location = "default";
		this.startTime = new Timestamp(date.getTime());
		this.endTime = new Timestamp(date.getTime());
		this.urgency = 0;
		this.amountOfVolunteers = 0;
	}
	
	public Task(String feedback) {
		this.feedback = feedback;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CracUser getCreator() {
		return creator;
	}

	public void setCreator(CracUser creator) {
		this.creator = creator;
	}

	public Set<Competence> getNeededCompetences() {
		return neededCompetences;
	}

	public void setNeededCompetences(Set<Competence> neededCompetences) {
		this.neededCompetences = neededCompetences;
	}

	public Task getSuperTask() {
		return superTask;
	}

	public void setSuperTask(Task superTask) {
		this.superTask = superTask;
	}

	public Set<Task> getChildTasks() {
		return childTasks;
	}

	public void setChildTasks(Set<Task> childTasks) {
		this.childTasks = childTasks;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public int getUrgency() {
		return urgency;
	}

	public void setUrgency(int urgency) {
		this.urgency = urgency;
	}

	public int getAmountOfVolunteers() {
		return amountOfVolunteers;
	}

	public void setAmountOfVolunteers(int amountOfVolunteers) {
		this.amountOfVolunteers = amountOfVolunteers;
	}

	public String getFeedback() {
		return feedback;
	}

	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}

	public Set<CracUser> getSignedUsers() {
		return signedUsers;
	}

	public void setSignedUsers(Set<CracUser> signedUsers) {
		this.signedUsers = signedUsers;
	}

	public Set<CracUser> getResponsibleUsers() {
		return responsibleUsers;
	}

	public void setResponsibleUsers(Set<CracUser> responsibleUsers) {
		this.responsibleUsers = responsibleUsers;
	}

	public Set<CracUser> getFollowingUsers() {
		return followingUsers;
	}

	public void setFollowingUsers(Set<CracUser> followingUsers) {
		this.followingUsers = followingUsers;
	}

	public Set<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(Set<Attachment> attachments) {
		this.attachments = attachments;
	}

	public Set<Comment> getComments() {
		return comments;
	}

	public void setComments(Set<Comment> comments) {
		this.comments = comments;
	}
	
}
