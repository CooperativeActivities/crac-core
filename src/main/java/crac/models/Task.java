package crac.models;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.daos.TaskDAO;
import crac.enums.TaskParticipationType;
import crac.enums.TaskRepetitionState;
import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.models.relation.CompetenceTaskRel;
import crac.models.relation.UserTaskRel;
import crac.models.utility.RepetitionDate;

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

	@NotNull
	private String description;

	@NotNull
	private String location;

	private Calendar startTime;

	private Calendar endTime;

	@NotNull
	private int urgency;

	@NotNull
	private int amountOfVolunteers;

	private String feedback;

	private TaskState taskState;

	private TaskRepetitionState taskRepetitionState;

	private boolean readyToPublish;

	private Calendar creationDate;

	/**
	 * Defines a different relationships to itself, providing the possibilities
	 * to add subtasks and nextTasks to tasks
	 */
	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	@JoinColumn(name = "super_task")
	private Task superTask;

	@OneToMany(mappedBy = "superTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Task> childTasks;

	/**
	 * defines a one to many relationship with the userRelationship-entity
	 */

	@JsonIdentityReference(alwaysAsId = true)
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserTaskRel> userRelationships;

	/**
	 * defines a one to many relation with the cracUser-entity
	 */
	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	@JoinColumn(name = "creator_id")
	private CracUser creator;

	/**
	 * defines a one to many relation with the repetitionDate-entity
	 */

	@ManyToOne
	@JsonIdentityReference(alwaysAsId = true)
	@JoinColumn(name = "repetitionDate_id")
	private RepetitionDate repetitionDate;

	/**
	 * defines a one to many relation with the attachment-entity
	 */
	@JsonIdentityReference(alwaysAsId = true)
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Attachment> attachments;

	/**
	 * defines a one to many relation with the attachment-entity
	 */
	@JsonIdentityReference(alwaysAsId = true)
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Comment> comments;

	@JsonIdentityReference(alwaysAsId = true)
	@OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
	private Set<Evaluation> mappedEvaluations;

	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<CompetenceTaskRel> mappedCompetences;

	/**
	 * constructors
	 */

	public Task() {
		this.taskState = TaskState.NOT_PUBLISHED;
		this.taskRepetitionState = TaskRepetitionState.ONCE;
		this.readyToPublish = false;
		this.creationDate = Calendar.getInstance();
		Calendar time = new GregorianCalendar();
		time.set(2030, 9, 10, 14, 30, 00);
		this.startTime = time;
		time.set(2031, 9, 10, 14, 30, 00);
		this.endTime = time;

	}

	public Task copy(Task superTask) {
		// t.setUserRelationships(userRelationships);
		// t.setAttachments(attachments);
		// t.setComments(comments);
		// t.setEndTime(endTime);
		// t.setFeedback(feedback);
		// t.setNextTask(t);
		// t.setPreviousTask(t);
		// t.setRepetitionTime(repetitionTime);
		// t.setStartTime(startTime);
		// t.setTaskRepetitionState(taskRepetitionState);
		// t.setTaskState(taskState);
		Task t = new Task();
		t.setAmountOfVolunteers(amountOfVolunteers);
		t.setCreator(creator);
		t.setDescription(description);
		t.setLocation(location);
		t.setName(name);

		Set<CompetenceTaskRel> competences = new HashSet<CompetenceTaskRel>();

		for (CompetenceTaskRel c : mappedCompetences) {
			competences.add(new CompetenceTaskRel(c.getCompetence(), c.getTask(), c.getNeededProficiencyLevel(),
					c.getImportanceLevel(), c.isMandatory()));
		}

		t.setMappedCompetences(competences);
		t.setSuperTask(superTask);
		t.setUrgency(urgency);
		t.setReadyToPublish(readyToPublish);

		Set<Task> copiedChildren = new HashSet<Task>();

		if (childTasks != null) {
			for (Task tc : childTasks) {
				copiedChildren.add(tc.copy(t));
			}
		}

		t.setChildTasks(copiedChildren);

		return t;
	}

	// UTILITY FUNCTIONS

	@JsonIgnore
	public Set<CracUser> getLeaders() {
		Set<CracUser> leaders = new HashSet<CracUser>();
		if (userRelationships != null) {
			for (UserTaskRel u : userRelationships) {
				if (u.getParticipationType() == TaskParticipationType.LEADING) {
					leaders.add(u.getUser());
				}
			}
		}
		if (leaders.isEmpty()) {
			if (superTask != null) {
				leaders = superTask.getLeaders();
			}
		}

		return leaders;
	}

	@JsonIgnore
	public int possibleNumberOfVolunteers() {
		int total = amountOfVolunteers;
		for (Task t : childTasks) {
			total -= t.getAmountOfVolunteers();
		}
		return total;
	}

	@JsonIgnore
	public Set<CracUser> getAllLeaders() {
		Set<CracUser> leaders = new HashSet<CracUser>();
		getAllLeadersIntern(leaders);
		return leaders;
	}

	@JsonIgnore
	private void getAllLeadersIntern(Set<CracUser> leaders) {

		if (userRelationships != null) {
			for (UserTaskRel u : userRelationships) {
				if (u.getParticipationType() == TaskParticipationType.LEADING) {
					leaders.add(u.getUser());
				}
			}
			if (superTask != null) {
				superTask.getAllLeadersIntern(leaders);
			}
		}

	}

	@JsonIgnore
	public void setTreeComplete(TaskDAO taskDAO) {
		taskState = TaskState.COMPLETED;
		taskDAO.save(this);
		if (childTasks != null) {
			if (!childTasks.isEmpty()) {
				for (Task t : childTasks) {
					t.setTreeComplete(taskDAO);
				}
			}
		}
	}

	@JsonIgnore
	public boolean readyToPublish() {
		if (!this.fieldsFilled()) {
			return false;
		}
		if (childTasks != null) {
			if (!childTasks.isEmpty()) {
				for (Task c : childTasks) {
					if (!c.isReadyToPublish()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@JsonIgnore
	public void readyToPublishTree(TaskDAO taskDAO) {
		if (this.fieldsFilled()) {
			this.readyToPublish = true;
			taskDAO.save(this);
			if (childTasks != null) {
				if (!childTasks.isEmpty()) {
					for (Task t : childTasks) {
						t.readyToPublishTree(taskDAO);
					}
				}
			}
		}
	}

	public boolean isSuperTask() {
		return this.getSuperTask() == null;
	}

	@JsonIgnore
	public boolean isLeaf() {
		if (childTasks == null) {
			return true;
		}

		if (childTasks.isEmpty()) {
			return true;
		}

		return false;
	}

	@JsonIgnore
	public boolean inConduction() {
		return this.getTaskState() == TaskState.STARTED || this.getTaskState() == TaskState.COMPLETED;
	}

	@JsonIgnore
	public boolean isJoinable() {
		return this.getTaskState() == TaskState.PUBLISHED || this.getTaskState() == TaskState.STARTED;
	}

	@JsonIgnore
	public boolean isFull() {
		if (userRelationships != null) {
			if (amountOfVolunteers == 0) {
				return false;
			}
			return amountOfVolunteers == userRelationships.size();
		} else {
			return true;
		}
	}

	@JsonIgnore
	public boolean fieldsFilled() {
		boolean filled = this.getAmountOfVolunteers() > 0 && !this.getDescription().equals("")
				&& this.getStartTime() != null && this.getEndTime() != null && !this.getLocation().equals("");
		if (isLeaf()) {
			if (this.getMappedCompetences() != null) {
				return filled && !this.getMappedCompetences().isEmpty();
			} else {
				return false;
			}
		}

		return filled;
	}

	@JsonIgnore
	private void publishTree() {
		this.setTaskState(TaskState.PUBLISHED);
		if (childTasks != null) {
			if (!childTasks.isEmpty()) {
				for (Task t : childTasks) {
					t.publishTree();
				}
			}
		}
	}

	// FUNCTIONS FOR STATECHANGE

	@JsonIgnore
	public int publish() {

		if (this.isSuperTask() && this.getTaskState() == TaskState.NOT_PUBLISHED && readyToPublish()) {
			if (fieldsFilled()) {
				publishTree();
				return 3;
			} else {
				return 2;
			}
		} else {
			return 1;
		}
	}

	@JsonIgnore
	public boolean checkStartAllowance() {
		return this.getStartTime().getTimeInMillis() < Calendar.getInstance().getTimeInMillis();
	}

	@JsonIgnore
	public int start(TaskDAO taskDAO) {

		if (this.getTaskState() == TaskState.PUBLISHED) {
			if (checkStartAllowance()) {
				this.setTaskState(TaskState.STARTED);
				taskDAO.save(this);
				return 3;
			} else {
				return 2;
			}
		} else {
			return 1;
		}

	}

	@JsonIgnore
	public int complete() {
		if (this.getTaskState() == TaskState.STARTED) {
			Set<UserTaskRel> ur = this.getUserRelationships();
			boolean usersDone = true;
			for (UserTaskRel u : ur) {
				if (!u.isCompleted()) {
					usersDone = false;
				}
			}
			if (usersDone) {
				this.setTaskState(TaskState.COMPLETED);
				return 3;
			} else {
				return 2;
			}
		} else {
			return 1;
		}
	}

	@JsonIgnore
	public int forceComplete(TaskDAO taskDAO, CracUser u) {
		if (this.getTaskState() == TaskState.STARTED) {
			this.setTreeComplete(taskDAO);
			return 3;
		} else {
			return 1;
		}
	}

	// -------------------------------------------------------

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

	public int getSignedUsers() {

		int num = 0;

		if (userRelationships != null) {
			for (UserTaskRel rel : userRelationships) {
				if (rel.getParticipationType() == TaskParticipationType.PARTICIPATING) {
					num++;
				}
			}
		}

		return num;

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

	public TaskRepetitionState getTaskRepetitionState() {
		return taskRepetitionState;
	}

	public void setTaskRepetitionState(TaskRepetitionState taskRepetitionState) {
		this.taskRepetitionState = taskRepetitionState;
	}

	public RepetitionDate getRepetitionDate() {
		return repetitionDate;
	}

	public void setRepetitionDate(RepetitionDate repetitionDate) {
		this.repetitionDate = repetitionDate;
	}

	public Set<Evaluation> getMappedEvaluations() {
		return mappedEvaluations;
	}

	public void setMappedEvaluations(Set<Evaluation> mappedEvaluations) {
		this.mappedEvaluations = mappedEvaluations;
	}

	public Set<CompetenceTaskRel> getMappedCompetences() {
		return mappedCompetences;
	}

	public void setMappedCompetences(Set<CompetenceTaskRel> mappedCompetences) {
		this.mappedCompetences = mappedCompetences;
	}

	public boolean isReadyToPublish() {
		return readyToPublish;
	}

	public void setReadyToPublish(boolean readyToPublish) {
		this.readyToPublish = readyToPublish;
	}

	public Calendar getCreationDate() {
		return creationDate;
	}

}
