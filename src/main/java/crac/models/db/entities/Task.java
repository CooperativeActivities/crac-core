package crac.models.db.entities;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
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

import crac.components.utility.DataAccess;
import crac.enums.TaskParticipationType;
import crac.enums.TaskRepetitionState;
import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.RepetitionDate;
import crac.models.db.relation.UserTaskRel;

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

	private String address;

	private String location;

	private double lat;

	private double lng;

	@NotNull
	@Column(name = "start_time")
	private Calendar startTime;

	@NotNull
	@Column(name = "end_time")
	private Calendar endTime;

	private int urgency;

	@Column(name = "max_amount_of_volunteers")
	private int maxAmountOfVolunteers;

	@Column(name = "min_amount_of_volunteers")
	private int minAmountOfVolunteers;

	private String feedback;

	@Column(name = "task_state")
	private TaskState taskState;

	@NotNull
	@Column(name = "task_type")
	private TaskType taskType;

	@Column(name = "task_repetition_state")
	private TaskRepetitionState taskRepetitionState;

	@Column(name = "ready_to_publish")
	private boolean readyToPublish;

	@Column(name = "creation_date")
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
	@JoinColumn(name = "repetition_date_id")
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

	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Material> materials;

	/**
	 * constructors
	 */

	public Task() {
		this.taskState = TaskState.NOT_PUBLISHED;
		this.taskRepetitionState = TaskRepetitionState.ONCE;
		this.readyToPublish = false;
		this.creationDate = Calendar.getInstance();
		/*
		 * Calendar time = new GregorianCalendar(); time.set(2030, 9, 10, 14,
		 * 30, 00); this.startTime = time; Calendar time2 = new
		 * GregorianCalendar(); time2.set(2031, 9, 10, 14, 30, 00); this.endTime
		 * = time2;
		 */
		this.mappedCompetences = new HashSet<>();
		this.materials = new HashSet<Material>();
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
		t.setMaxAmountOfVolunteers(maxAmountOfVolunteers);
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
		int total = maxAmountOfVolunteers;
		for (Task t : childTasks) {
			total -= t.getMaxAmountOfVolunteers();
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
	public Set<UserTaskRel> getAllParticipants() {
		Set<UserTaskRel> participants = new HashSet<UserTaskRel>();
		getAllIntern(participants, TaskParticipationType.PARTICIPATING);
		return participants;
	}
	
	@JsonIgnore
	private void getAllIntern(Set<UserTaskRel> users, TaskParticipationType type) {

		if (userRelationships != null) {
			for (UserTaskRel u : userRelationships) {
				if (u.getParticipationType() == type) {
					users.add(u);
				}
			}
			if (childTasks != null) {
				if(!childTasks.isEmpty()){
					for(Task t : childTasks){
						t.getAllIntern(users, type);
					}
				}
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
	/*
	 * @JsonIgnore public boolean readyToPublish() { if (!this.fieldsFilled()) {
	 * return false; } if (childTasks != null) { if (!childTasks.isEmpty()) {
	 * for (Task c : childTasks) { if (!c.isReadyToPublish()) { return false; }
	 * } } } return true; }
	 */

	@JsonIgnore
	public boolean updateReadyStatus() {
		boolean ready = this.fieldsFilled() && this.childTasksReady();
		if (this.getTaskType() == TaskType.ORGANISATIONAL && !this.hasChildTasks()) {
			ready = false;
		}
		this.readyToPublish = ready;
		DataAccess.getRepo(TaskDAO.class).save(this);
		if (this.getSuperTask() != null) {
			this.getSuperTask().updateReadyStatus();
		}
		return ready;
	}

	public boolean childTasksReady() {
		if (!this.hasChildTasks()) {
			return true;
		}

		for (Task c : childTasks) {
			if (!c.isReadyToPublish()) {
				return false;
			}
		}

		return true;

	}

	public boolean hasChildTasks() {
		if (childTasks != null) {
			if (childTasks.isEmpty()) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/*
	 * @JsonIgnore public void readyToPublishTree(HashMap<String, String>
	 * errors, TaskDAO taskDAO) { if (this.fieldsFilled()) { this.readyToPublish
	 * = true; taskDAO.save(this); if (this.hasChildTasks()) { for (Task t :
	 * childTasks) { t.readyToPublishTree(errors, taskDAO); }
	 * 
	 * } } else { errors.put(this.id + "", "TASK_NOT_READY"); } }
	 */

	public boolean isSuperTask() {
		return this.getSuperTask() == null;
	}
	/*
	 * @JsonIgnore public boolean isLeaf() { if (childTasks == null) { return
	 * true; }
	 * 
	 * if (childTasks.isEmpty()) { return true; }
	 * 
	 * return false; }
	 */

	@JsonIgnore
	public boolean isExtendable() {
		return this.getTaskState() != TaskState.COMPLETED;
	}

	@JsonIgnore
	public boolean inConduction() {
		return this.getTaskState() == TaskState.STARTED || this.getTaskState() == TaskState.COMPLETED;
	}

	@JsonIgnore
	public boolean isJoinable() {

		boolean state = this.getTaskState() == TaskState.PUBLISHED || this.getTaskState() == TaskState.STARTED;
		boolean type = this.getTaskType() == TaskType.WORKABLE || this.getTaskType() == TaskType.SHIFT;

		if (this.getTaskType() == TaskType.WORKABLE && this.hasChildTasks()) {
			type = false;
		}

		return state && type;
	}

	@JsonIgnore
	public boolean isFollowable() {

		return this.getTaskState() == TaskState.PUBLISHED || this.getTaskState() == TaskState.STARTED;
	}

	@JsonIgnore
	public boolean isFull() {
		if (userRelationships != null) {
			if (maxAmountOfVolunteers == 0) {
				return false;
			}
			return maxAmountOfVolunteers == userRelationships.size();
		} else {
			return true;
		}
	}

	@JsonIgnore
	public boolean fieldsFilled() {
		boolean filled = !this.getName().equals("") && this.getStartTime() != null && this.getEndTime() != null;
		return filled;
	}

	@JsonIgnore
	private void setTreeState(TaskState state, TaskDAO taskDAO) {
		this.setTaskState(state);
		if (childTasks != null) {
			if (!childTasks.isEmpty()) {
				for (Task t : childTasks) {
					t.setTreeState(state, taskDAO);
					taskDAO.save(t);
				}
			}
		}
	}

	// FUNCTIONS FOR STATECHANGE

	@JsonIgnore
	public int publish() {

		if (this.isSuperTask() && this.getTaskState() == TaskState.NOT_PUBLISHED && fieldsFilled()) {
			if (childTasksReady()) {
				setTreeState(TaskState.PUBLISHED, DataAccess.getRepo(TaskDAO.class));
				return 3;
			} else {
				return 2;
			}
		} else {
			return 1;
		}
	}

	@JsonIgnore
	public int unpublish() {

		if (this.getUserRelationships() != null) {

			UserTaskRelDAO userTaskRelDAO = DataAccess.getRepo(UserTaskRelDAO.class);

			for (UserTaskRel utr : this.getUserRelationships()) {
				if (utr.getParticipationType() == TaskParticipationType.PARTICIPATING) {
					utr.setParticipationType(TaskParticipationType.FOLLOWING);
					userTaskRelDAO.save(utr);
				}
			}
		}

		setTreeState(TaskState.NOT_PUBLISHED, DataAccess.getRepo(TaskDAO.class));
		return 3;
	}

	@JsonIgnore
	public boolean checkStartAllowance() {
		return this.getStartTime().getTimeInMillis() < Calendar.getInstance().getTimeInMillis();
	}

	@JsonIgnore
	public int start() {

		if (this.getTaskState() == TaskState.PUBLISHED) {
			if (checkStartAllowance()) {
				this.setTaskState(TaskState.STARTED);
				DataAccess.getRepo(TaskDAO.class).save(this);
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
	public int forceComplete() {
		if (this.getTaskState() == TaskState.STARTED) {
			this.setTreeComplete(DataAccess.getRepo(TaskDAO.class));
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

	public int getMaxAmountOfVolunteers() {
		return maxAmountOfVolunteers;
	}

	public void setMaxAmountOfVolunteers(int maxAmountOfVolunteers) {
		this.maxAmountOfVolunteers = maxAmountOfVolunteers;
	}

	public int getParticipatingUsers() {

		return this.getAllParticipants().size();
		
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

	public int getMinAmountOfVolunteers() {
		return minAmountOfVolunteers;
	}

	public void setMinAmountOfVolunteers(int minAmountOfVolunteers) {
		this.minAmountOfVolunteers = minAmountOfVolunteers;
	}

	public Set<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(Set<Material> materials) {
		this.materials = materials;
	}

	public void addMaterial(Material material) {
		this.materials.add(material);
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}

	public void setCreationDate(Calendar creationDate) {
		this.creationDate = creationDate;
	}

	public void update(Task t) {
		if (t.getName() != null) {
			this.setName(t.getName());
		}

		if (t.getDescription() != null) {
			this.setDescription(t.getDescription());
		}

		if (t.getLocation() != null) {
			this.setLocation(t.getLocation());
		}

		if (t.getStartTime() != null) {
			this.setStartTime(t.getStartTime());
		}

		if (t.getEndTime() != null) {
			this.setEndTime(t.getEndTime());
		}

		if (t.getMaxAmountOfVolunteers() >= 0) {
			this.setMaxAmountOfVolunteers(t.getMaxAmountOfVolunteers());
		}

		if (t.getMinAmountOfVolunteers() >= 0) {
			this.setMinAmountOfVolunteers(t.getMinAmountOfVolunteers());
		}

		if (t.getFeedback() != null) {
			this.setFeedback(t.getFeedback());
		}
		
		if(t.getAddress() != null){
			this.setAddress(t.getAddress());
		}
		
		if(t.getLat() != 0){
			this.setLat(t.getLat());
		}
		
		if(t.getLng() != 0){
			this.setLng(t.getLng());
		}
		/*
		 * if (t.getTaskType() != null) { this.setTaskType(t.getTaskType()); }
		 */
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

}
