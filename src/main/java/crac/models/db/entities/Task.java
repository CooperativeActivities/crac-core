package crac.models.db.entities;

import java.util.Calendar;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.enums.TaskParticipationType;
import crac.enums.ConcreteTaskState;
import crac.enums.TaskType;
import crac.exception.InvalidActionException;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.RepetitionDate;
import crac.models.db.relation.UserTaskRel;
import lombok.Getter;
import lombok.Setter;

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

	private double geoLat;

	private double geoLng;
	
	private String geoName;
	
	private String geoCountry;
	
	private String geoCountryA;
	
	private String geoMacroRegion;
	
	private String geoRegion;
	
	private String geoLocality;

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
	@Enumerated(EnumType.ORDINAL)
	private ConcreteTaskState taskState;

	@NotNull
	@Column(name = "task_type")
	private TaskType taskType;

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
	
	@JsonIdentityReference(alwaysAsId=true)
	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<CompetenceTaskRel> mappedCompetences;

	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Material> materials;
	
	@ManyToMany(mappedBy = "restrictedTasks", fetch = FetchType.LAZY)
	private Set<CracGroup> restrictingGroups;
	
	@ManyToMany(mappedBy = "invitedToTasks", fetch = FetchType.LAZY)
	private Set<CracGroup> invitedGroups;


	/**
	 * constructors
	 */

	public Task() {
		this.taskState = ConcreteTaskState.NOT_PUBLISHED;
		this.readyToPublish = false;
		this.creationDate = Calendar.getInstance();
		this.mappedCompetences = new HashSet<>();
		this.materials = new HashSet<>();
		this.attachments = new HashSet<>();
		this.minAmountOfVolunteers = 1;
		this.userRelationships = new HashSet<>();
		this.childTasks = new HashSet<>();
	}
	
	@JsonIgnore
	public Task copy(Calendar date){
		Calendar startd = date;
		Calendar endd = (Calendar) date.clone();
		endd.add(Calendar.YEAR, 1);
		return copy(null, startd, endd);
	}

	@JsonIgnore
	private Task copy(Task stask, Calendar startd, Calendar endd) {
		
		Task clone = new Task();
		
		clone.setSuperTask(stask);
		clone.update(this);
		clone.setCreator(creator);
		
		attachments.forEach( x -> clone.getAttachments().add(x.copy(clone)) );
		mappedCompetences.forEach( x -> clone.getMappedCompetences().add(x.copy(clone)) );
		materials.forEach( x -> clone.getMaterials().add(x.copy(clone)) );
				
		clone.setStartTime(startd);
		clone.setEndTime(endd);
		clone.setTaskType(taskType);
		
		childTasks.forEach( x -> clone.getChildTasks().add(x.copy(clone, startd, endd)) );
		
		
		return clone;
		
	}

	// UTILITY FUNCTIONS



	@JsonIgnore
	public int possibleNumberOfVolunteers() {
		int total = maxAmountOfVolunteers;
		for (Task t : childTasks) {
			total -= t.getMaxAmountOfVolunteers();
		}
		return total;
	}
/*
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
	public UserTaskRel getIndirectLead(CracUser u){
		
		for(UserTaskRel utr : userRelationships){
			if(utr.getUser().getId() == u.getId()){
				return utr;
			}
		}
		
		if(this.superTask != null){
			return superTask.getIndirectLead(u);
		}else{
			return null;
		}
		
	}
	
	
	*/
	
	@JsonIgnore
	public Set<UserTaskRel> getUserInvolvement(CracUser u){
		Set<UserTaskRel> set = new HashSet<>();
		getRelationshipsUp(set, TaskParticipationType.LEADING);
		getRelationshipsUp(set, TaskParticipationType.PARTICIPATING);
		set.removeIf( x -> x.getUser().getId() != u.getId() );
		return set;
	}
	
	@JsonIgnore
	private void getRelationshipsUp(Set<UserTaskRel> users, TaskParticipationType type) {

		if (userRelationships != null) {
			for (UserTaskRel u : userRelationships) {
				if (u.getParticipationType() == type) {
					users.add(u);
				}
			}
			if (superTask != null) {
				superTask.getRelationshipsUp(users, type);
			}
		}

	}
	
	@JsonIgnore
	public boolean isLeader(CracUser u){
		return getAllLeaders().contains(u);
	}

	@JsonIgnore
	public Set<UserTaskRel> getAllLeaders() {
		Set<UserTaskRel> leaders = new HashSet<UserTaskRel>();
		getRelationshipsUp(leaders, TaskParticipationType.LEADING);
		return leaders;
	}

	@JsonIgnore
	public Set<UserTaskRel> getAllParticipants() {
		Set<UserTaskRel> participants = new HashSet<UserTaskRel>();
		getRelationshipsDown(participants, TaskParticipationType.PARTICIPATING);
		return participants;
	}
		
	@JsonIgnore
	public Set<UserTaskRel> getAllLeaderAndParticipantRels() {
		Set<UserTaskRel> participants = new HashSet<UserTaskRel>();
		getRelationshipsDown(participants, TaskParticipationType.PARTICIPATING);
		getRelationshipsUp(participants, TaskParticipationType.LEADING);
		return participants;
	}


	@JsonIgnore
	private void getRelationshipsDown(Set<UserTaskRel> users, TaskParticipationType type) {

		if (userRelationships != null) {
			for (UserTaskRel u : userRelationships) {
				if (u.getParticipationType() == type) {
					boolean notPartOf = true;
					for (UserTaskRel srel : users) {
						if (srel.getUser().getId() == u.getUser().getId() && srel.getParticipationType() == type) {
							notPartOf = false;
						}
					}
					if (notPartOf) {
						users.add(u);
					}

				}
			}
			if (childTasks != null) {
				if (!childTasks.isEmpty()) {
					for (Task t : childTasks) {
						t.getRelationshipsDown(users, type);
					}
				}
			}
		}

	}

	/*
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
	}*/

	@JsonIgnore
	public boolean updateReadyStatus(TaskDAO taskDAO) {
		boolean ready = this.fieldsFilled() && this.childTasksReady();
		if (this.getTaskType() == TaskType.ORGANISATIONAL && !this.hasChildTasks()) {
			ready = false;
		}
		this.readyToPublish = ready;
		taskDAO.save(this);
		if (this.getSuperTask() != null) {
			this.getSuperTask().updateReadyStatus(taskDAO);
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

	public boolean isSuperTask() {
		return this.getSuperTask() == null;
	}

	@JsonIgnore
	public boolean isFull() {		
		return userRelationships.size() >= maxAmountOfVolunteers;
	}

	@JsonIgnore
	public boolean fieldsFilled() {
		boolean filled = !this.getName().equals("") && this.getStartTime() != null && this.getEndTime() != null;
		return filled;
	}

	@JsonIgnore
	public void setGlobalTreeState(ConcreteTaskState state, TaskDAO taskDAO) {
		this.setTaskState(state);
		if (childTasks != null) {
			if (!childTasks.isEmpty()) {
				for (Task t : childTasks) {
					t.setGlobalTreeState(state, taskDAO);
					taskDAO.save(t);
				}
			}
		}
	}

	// FUNCTIONS FOR STATECHANGE


	@JsonIgnore
	public int unpublish(UserTaskRelDAO userTaskRelDAO, TaskDAO taskDAO) {

		if (this.getUserRelationships() != null) {

			for (UserTaskRel utr : this.getUserRelationships()) {
				if (utr.getParticipationType() == TaskParticipationType.PARTICIPATING) {
					utr.setParticipationType(TaskParticipationType.FOLLOWING);
					userTaskRelDAO.save(utr);
				}
			}
		}

		setGlobalTreeState(ConcreteTaskState.NOT_PUBLISHED, taskDAO);
		return 3;
	}
	
	@JsonIgnore
	public int forceComplete(TaskDAO taskDAO) {
		if (this.getTaskState() == ConcreteTaskState.STARTED) {
			this.setGlobalTreeState(ConcreteTaskState.COMPLETED, taskDAO);
			return 3;
		} else {
			return 1;
		}
	}

	@JsonIgnore
	public boolean checkStartAllowance() {
		return this.getStartTime().getTimeInMillis() < Calendar.getInstance().getTimeInMillis();
	}

	@JsonIgnore
	public void setTaskState(ConcreteTaskState state, TaskDAO taskDAO) throws InvalidActionException{
		this.getTaskState().setTaskState(this, state, taskDAO);
	}

	/*
	@JsonIgnore
	public int publish(TaskDAO taskDAO) {

		if (this.isSuperTask() && this.getTaskState() == TaskState.NOT_PUBLISHED && fieldsFilled()) {
			if (childTasksReady()) {
				setGlobalTreeState(TaskState.PUBLISHED, taskDAO);
				return 3;
			} else {
				return 2;
			}
		} else {
			return 1;
		}
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
				if (!u.isCompleted() && u.getParticipationType() == TaskParticipationType.PARTICIPATING) {
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
	}*/


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

	public ConcreteTaskState getTaskState() {
		return taskState;
	}

	public void setTaskState(ConcreteTaskState taskState) {
		this.taskState = taskState;
	}

	public RepetitionDate getRepetitionDate() {
		return repetitionDate;
	}

	public void setRepetitionDate(RepetitionDate repetitionDate) {
		this.repetitionDate = repetitionDate;
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

		if (t.getAddress() != null) {
			this.setAddress(t.getAddress());
		}

		if (t.getGeoLat() != 0) {
			this.setGeoLat(t.getGeoLat());
		}

		if (t.getGeoLng() != 0) {
			this.setGeoLng(t.getGeoLng());
		}
		
		if(t.getGeoName() != null){
			this.setGeoName(t.getGeoName());
		}
		
		if(t.getGeoCountry() != null){
			this.setGeoCountry(t.getGeoCountry());
		}

		if(t.getGeoCountryA() != null){
			this.setGeoCountryA(t.getGeoCountryA());
		}

		if(t.getGeoMacroRegion() != null){
			this.setGeoMacroRegion(t.getGeoMacroRegion());
		}

		if(t.getGeoRegion() != null){
			this.setGeoRegion(t.getGeoRegion());
		}

		if(t.getGeoLocality() != null){
			this.setGeoLocality(t.getGeoLocality());
		}	
				
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Set<CracGroup> getRestrictingGroups() {
		return restrictingGroups;
	}

	public void setRestrictingGroups(Set<CracGroup> restrictingGroups) {
		this.restrictingGroups = restrictingGroups;
	}

	public Set<CracGroup> getInvitedGroups() {
		return invitedGroups;
	}

	public void setInvitedGroups(Set<CracGroup> invitedGroups) {
		this.invitedGroups = invitedGroups;
	}

	public double getGeoLat() {
		return geoLat;
	}

	public void setGeoLat(double geoLat) {
		this.geoLat = geoLat;
	}

	public double getGeoLng() {
		return geoLng;
	}

	public void setGeoLng(double geoLng) {
		this.geoLng = geoLng;
	}

	public String getGeoName() {
		return (geoName != null) ? geoName : "";
	}

	public void setGeoName(String geoName) {
		this.geoName = geoName;
	}

	public String getGeoCountry() {
		return (geoCountry != null) ? geoCountry : "";
	}

	public void setGeoCountry(String geoCountry) {
		this.geoCountry = geoCountry;
	}

	public String getGeoCountryA() {
		return (geoCountryA != null) ? geoCountryA : "";
	}

	public void setGeoCountryA(String geoCountryA) {
		this.geoCountryA = geoCountryA;
	}

	public String getGeoMacroRegion() {
		return (geoMacroRegion != null) ? geoMacroRegion : "";
	}

	public void setGeoMacroRegion(String geoMacroRegion) {
		this.geoMacroRegion = geoMacroRegion;
	}

	public String getGeoRegion() {
				
		return (geoRegion != null) ? geoRegion : "";
	}

	public void setGeoRegion(String geoRegion) {
		this.geoRegion = geoRegion;
	}

	public String getGeoLocality() {
		return (geoLocality != null) ? geoLocality : "";
	}

	public void setGeoLocality(String geoLocality) {
		this.geoLocality = geoLocality;
	}
	
	@JsonIgnore
	public TaskShort toShort(){
		TaskShort t = new TaskShort();
		t.setId(this.id);
		t.setName(this.name);
		t.setDescription(this.description);
		t.setAddress(this.address);
		t.setStartTime(this.startTime);
		t.setEndTime(this.endTime);
		t.setTaskState(this.taskState);
		t.setReadyToPublish(this.readyToPublish);
		t.setSuperTask(null);
		return t;
	}
	
	@JsonIgnore
	public TaskCrumb toCrumb(){
		TaskCrumb t = new TaskCrumb();
		t.setId(this.id);
		t.setName(this.name);
		TaskCrumb st = (superTask != null) ? superTask.toCrumb() : null;
		t.setSuperTask(st);
		return t;
	}
	
	public TaskShort toShortWithCrumbs(){
		TaskShort t = toShort();
		TaskCrumb st = (superTask != null) ? superTask.toCrumb() : null;
		t.setSuperTask(st);
		return t;
	}

	
	public class TaskShort {

		@Getter
		@Setter
		long id;

		@Getter
		@Setter
		String name;

		@Getter
		@Setter
		private String description;

		@Getter
		@Setter
		private String address;
		
		@Getter
		@Setter
		private Calendar startTime;

		@Getter
		@Setter
		private Calendar endTime;
		
		@Getter
		@Setter
		private ConcreteTaskState taskState;
		
		@Getter
		@Setter
		private boolean readyToPublish;
		
		@Getter
		@Setter
		private TaskCrumb superTask;

		public TaskShort(){
		}
		
	}
	
	public class TaskCrumb {

		@Getter
		@Setter
		long id;

		@Getter
		@Setter
		String name;
		
		@Getter
		@Setter
		private TaskCrumb superTask;

		public TaskCrumb(){
		}
		
	}

	
}
