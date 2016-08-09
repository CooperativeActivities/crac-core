package crac.models;

import java.sql.Timestamp;
import java.util.Calendar;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.relationmodels.UserTaskRel;

/**
 * The task-entity.
 */
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "task_id")
	private long id;
	
	@NotNull
	private String name;

	private String description;

	private String location;
	
	private Calendar startTime;
	
	private Calendar endTime;
	
	private int urgency;
	
	private int amountOfVolunteers;
	
	private String feedback;
	
	private TaskState taskState;
	
	private TaskType taskType;

	/**
	 * Defines a different relationships to itself, providing the possibilities to add subtasks and nextTasks to tasks
	 */
	@ManyToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "super_task")
	private Task superTask;
	
	@OneToMany(mappedBy = "superTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Task> childTasks;
	
	@OneToOne
	@JsonIdentityReference(alwaysAsId=true)
	@JoinColumn(name = "previous_task")
	private Task previousTask;
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToOne(mappedBy = "previousTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Task nextTask;
	
	/**
	 * defines a many to many relation with the competence-entity
	 */
	@JsonIdentityReference(alwaysAsId=true)
	@ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinTable(name = "task_competences", joinColumns = { @JoinColumn(name = "task_id") }, inverseJoinColumns = {
			@JoinColumn(name = "competence_id") })
	private Set<Competence> neededCompetences;

	/**
	 * defines a one to many relationship with the userRelationship-entity
	 */
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserTaskRel> userRelationships;
	
	
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
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Attachment> attachments;

	/**
	 * defines a one to many relation with the attachment-entity
	 */
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Comment> comments;

	/**
	 * constructors
	 */

	public Task() {
		this.taskState = TaskState.NOT_PUBLISHED;
		this.taskType = TaskType.PARALLEL;
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

	public Calendar getStartTime() {
		return startTime;
	}


	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}


	public Calendar getEndTime() {
		return endTime;
	}


	public void setEndTime(Calendar endTime) {
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

	public Set<UserTaskRel> getUserRelationships() {
		return userRelationships;
	}

	public void setUserRelationships(Set<UserTaskRel> userRelationships) {
		this.userRelationships = userRelationships;
	}

	public TaskState getTaskState() {
		return taskState;
	}

	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}


	public Task getPreviousTask() {
		return previousTask;
	}


	public void setPreviousTask(Task previousTask) {
		this.previousTask = previousTask;
	}


	public Task getNextTask() {
		return nextTask;
	}


	public void setNextTask(Task nextTask) {
		this.nextTask = nextTask;
	}
	
}
