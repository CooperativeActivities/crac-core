package crac.models.output;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import crac.components.storage.CompetenceStorage;
import crac.components.utility.DataAccess;
import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Attachment;
import crac.models.db.entities.Comment;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Material;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;

public class TaskDetails {

	private long id;

	private String name;

	private String description;

	private String address;

	private String location;

	private double lat;

	private double lng;

	private Calendar startTime;

	private Calendar endTime;

	private int minAmountOfVolunteers;

	private int maxAmountOfVolunteers;

	private int signedUsers;

	private TaskState taskState;

	private boolean readyToPublish;

	private Calendar creationDate;

	private TaskShort superTask;

	private Set<TaskShort> childTasks;

	private CracUser creator;

	private Set<Attachment> attachments;

	private Set<Comment> comments;

	private Set<Evaluation> mappedEvaluations;

	private Set<UserFriendDetails> userRelationships;

	private Set<CompetenceRelationDetails> taskCompetences;

	private Set<MaterialDetails> materials;

	private TaskType taskType;

	private Set<UserTaskRel> participationDetails;

	private boolean assigned;

	private boolean permissions;

	public TaskDetails(Task t, CracUser u) {
		this.id = t.getId();
		this.creationDate = t.getCreationDate();
		this.name = t.getName();
		this.description = t.getDescription();
		this.address = t.getAddress();
		this.location = t.getLocation();
		this.lat = t.getLat();
		this.lng = t.getLng();
		this.startTime = t.getStartTime();
		this.endTime = t.getEndTime();
		this.maxAmountOfVolunteers = t.getMaxAmountOfVolunteers();
		this.minAmountOfVolunteers = t.getMinAmountOfVolunteers();
		this.taskState = t.getTaskState();
		this.readyToPublish = t.isReadyToPublish();
		if (t.getSuperTask() != null) {
			this.superTask = new TaskShort(t.getSuperTask());
		} else {
			this.superTask = null;
		}
		this.childTasks = addChildren(t);
		this.attachments = t.getAttachments();
		this.comments = t.getComments();
		this.userRelationships = calcFriends(t, u);
		this.participationDetails = DataAccess.getRepo(UserTaskRelDAO.class).findByUserAndTask(u, t);
		this.taskCompetences = new HashSet<>();
		if (!this.participationDetails.isEmpty()) {
			this.taskCompetences = calcComps(t, u);
			this.assigned = true;
		} else {
			this.participationDetails = new HashSet<>();
			this.participationDetails.add(t.getIndirectLead(u));
			this.assigned = false;
		}
		this.materials = addMaterials(t);
		this.taskType = t.getTaskType();
		this.permissions = u.hasTaskPermissions(t);
	}
	
	public Set<MaterialDetails> addMaterials(Task t){
		Set<MaterialDetails> materials = new HashSet<>();
		for(Material m : t.getMaterials()){
			materials.add(new MaterialDetails(m));
		}
		return materials;
	}

	public Set<TaskShort> addChildren(Task t) {

		Set<TaskShort> list = new HashSet<>();

		if (t.getChildTasks() != null) {

			for (Task tc : t.getChildTasks()) {
				list.add(new TaskShort(tc));
			}
		}
		return list;

	}

	public Set<CompetenceRelationDetails> calcComps(Task t, CracUser u) {

		Set<CompetenceRelationDetails> list = new HashSet<>();

		Set<CompetenceTaskRel> mctr = t.getMappedCompetences();

		if (mctr != null) {
			if (mctr.size() != 0) {
				for (CompetenceTaskRel ctr : mctr) {
					double bestVal = 0;
					if (u.getCompetenceRelationships() != null) {
						for (UserCompetenceRel ucr : u.getCompetenceRelationships()) {
							double newVal = CompetenceStorage.getCompetenceSimilarity(ucr.getCompetence(),
									ctr.getCompetence());
							if (newVal > bestVal) {
								bestVal = newVal;
							}
						}
					}
					System.out.println("name: " + ctr.getCompetence().getName() + " val: " + bestVal);
					CompetenceRelationDetails cd = new CompetenceRelationDetails(ctr.getCompetence());
					cd.setMandatory(ctr.isMandatory());
					cd.setRelationValue(bestVal);
					cd.setImportanceLevel(ctr.getImportanceLevel());
					cd.setNeededProficiencyLevel(ctr.getNeededProficiencyLevel());

					list.add(cd);
				}
			}
		}
		return list;

	}

	private Set<UserFriendDetails> calcFriends(Task t, CracUser u) {

		Set<UserFriendDetails> list = new HashSet<>();
		UserRelationship found = null;
		boolean friend = false;
		CracUser otherU = null;
		Set<UserTaskRel> participantRels = t.getAllParticipants();
		this.signedUsers = participantRels.size();

		if (participantRels.size() != 0) {
			for (UserTaskRel utr : participantRels) {
				found = null;
				otherU = utr.getUser();

				if (u.getUserRelationshipsAs1() != null) {
					if (u.getUserRelationshipsAs1().size() != 0) {
						for (UserRelationship ur : u.getUserRelationshipsAs1()) {
							if (otherU.getId() == ur.getC2().getId()) {
								found = ur;
							}
						}
					}
				}
				if (u.getUserRelationshipsAs2() != null) {
					if (u.getUserRelationshipsAs2().size() != 0) {
						for (UserRelationship ur : u.getUserRelationshipsAs2()) {
							if (utr.getUser().getId() == ur.getC1().getId()) {
								found = ur;
							}
						}
					}
				}

				if (found != null) {
					friend = found.isFriends();
				} else {
					friend = false;
				}

				UserFriendDetails fd = new UserFriendDetails(otherU, friend, utr);

				if (otherU.getId() == u.getId()) {
					fd.setSelf(true);
				}

				list.add(fd);
			}

		}

		return list;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public int getMinAmountOfVolunteers() {
		return minAmountOfVolunteers;
	}

	public void setMinAmountOfVolunteers(int minAmountOfVolunteers) {
		this.minAmountOfVolunteers = minAmountOfVolunteers;
	}

	public int getMaxAmountOfVolunteers() {
		return maxAmountOfVolunteers;
	}

	public void setMaxAmountOfVolunteers(int maxAmountOfVolunteers) {
		this.maxAmountOfVolunteers = maxAmountOfVolunteers;
	}

	public int getSignedUsers() {
		return signedUsers;
	}

	public void setSignedUsers(int signedUsers) {
		this.signedUsers = signedUsers;
	}

	public TaskState getTaskState() {
		return taskState;
	}

	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
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

	public void setCreationDate(Calendar creationDate) {
		this.creationDate = creationDate;
	}

	public TaskShort getSuperTask() {
		return superTask;
	}

	public void setSuperTask(TaskShort superTask) {
		this.superTask = superTask;
	}

	public Set<TaskShort> getChildTasks() {
		return childTasks;
	}

	public void setChildTasks(Set<TaskShort> childTasks) {
		this.childTasks = childTasks;
	}

	public CracUser getCreator() {
		return creator;
	}

	public void setCreator(CracUser creator) {
		this.creator = creator;
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

	public Set<Evaluation> getMappedEvaluations() {
		return mappedEvaluations;
	}

	public void setMappedEvaluations(Set<Evaluation> mappedEvaluations) {
		this.mappedEvaluations = mappedEvaluations;
	}

	public Set<UserFriendDetails> getUserRelationships() {
		return userRelationships;
	}

	public void setUserRelationships(Set<UserFriendDetails> userRelationships) {
		this.userRelationships = userRelationships;
	}

	public Set<CompetenceRelationDetails> getTaskCompetences() {
		return taskCompetences;
	}

	public void setTaskCompetences(Set<CompetenceRelationDetails> taskCompetences) {
		this.taskCompetences = taskCompetences;
	}

	public Set<MaterialDetails> getMaterials() {
		return materials;
	}

	public void setMaterials(Set<MaterialDetails> materials) {
		this.materials = materials;
	}

	public TaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}

	public boolean isAssigned() {
		return assigned;
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	public Set<UserTaskRel> getParticipationDetails() {
		return participationDetails;
	}

	public void setParticipationDetails(Set<UserTaskRel> participationDetails) {
		this.participationDetails = participationDetails;
	}

	public boolean isPermissions() {
		return permissions;
	}

	public void setPermissions(boolean permissions) {
		this.permissions = permissions;
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
