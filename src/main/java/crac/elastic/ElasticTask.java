package crac.elastic;

import java.sql.Timestamp;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import crac.models.Attachment;
import crac.models.Comment;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Project;
import crac.models.Task;

public class ElasticTask {
	
	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
	private String id;

	@JsonIdentityReference(alwaysAsId=true)
	private Task superTask;
	
	@JsonIdentityReference(alwaysAsId=true)
	private Set<Task> childTasks;
	
	private Set<Competence> neededCompetences;

	@JsonIdentityReference(alwaysAsId=true)
	private Set<CracUser> signedUsers;
	
	@JsonIdentityReference(alwaysAsId=true)
	private Set<CracUser> responsibleUsers;
	
	@JsonIdentityReference(alwaysAsId=true)
	private Set<CracUser> followingUsers;

	private String name;

	private String description;

	private String location;
	
	private Timestamp startTime;
	
	private Timestamp endTime;
	
	private int urgency;
	
	private int amountOfVolunteers;
	
	private String feedback;
	
	@JsonIdentityReference(alwaysAsId=true)
	private CracUser creator;

	@JsonIdentityReference(alwaysAsId=true)
	private Set<Attachment> attachments;

	@JsonIdentityReference(alwaysAsId=true)
	private Set<Comment> comments;

	public ElasticTask(String id, Task superTask, Set<Task> childTasks,
			Set<Competence> neededCompetences, Set<CracUser> signedUsers, Set<CracUser> responsibleUsers,
			Set<CracUser> followingUsers, String name, String description, String location, Timestamp startTime,
			Timestamp endTime, int urgency, int amountOfVolunteers, String feedback, CracUser creator,
			Set<Attachment> attachments, Set<Comment> comments) {
		this.id = id;
		this.superTask = superTask;
		this.childTasks = childTasks;
		this.neededCompetences = neededCompetences;
		this.signedUsers = signedUsers;
		this.responsibleUsers = responsibleUsers;
		this.followingUsers = followingUsers;
		this.name = name;
		this.description = description;
		this.location = location;
		this.startTime = startTime;
		this.endTime = endTime;
		this.urgency = urgency;
		this.amountOfVolunteers = amountOfVolunteers;
		this.feedback = feedback;
		this.creator = creator;
		this.attachments = attachments;
		this.comments = comments;
	}

	public ElasticTask() {
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

	public Set<Competence> getNeededCompetences() {
		return neededCompetences;
	}

	public void setNeededCompetences(Set<Competence> neededCompetences) {
		this.neededCompetences = neededCompetences;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
	
	
	
}
