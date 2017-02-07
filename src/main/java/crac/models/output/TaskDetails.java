package crac.models.output;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

import crac.enums.TaskRepetitionState;
import crac.enums.TaskState;
import crac.models.Attachment;
import crac.models.Comment;
import crac.models.CracUser;
import crac.models.Evaluation;
import crac.models.Task;
import crac.models.relation.CompetenceTaskRel;
import crac.models.relation.UserCompetenceRel;
import crac.models.relation.UserRelationship;
import crac.models.relation.UserTaskRel;
import crac.models.utility.RepetitionDate;
import crac.storage.CompetenceStorage;

public class TaskDetails {

	private long id;

	private String name;

	private String description;

	private String location;

	private Calendar startTime;

	private Calendar endTime;

	private int amountOfVolunteers;
	
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
	
	public TaskDetails(Task t, CracUser u){
		this.id = t.getId();
		this.creationDate = t.getCreationDate();
		this.name = t.getName();
		this.description = t.getDescription();
		this.location = t.getLocation();
		this.startTime = t.getStartTime();
		this.endTime = t.getEndTime();
		this.amountOfVolunteers = t.getAmountOfVolunteers();
		this.signedUsers = t.getSignedUsers();
		this.taskState = t.getTaskState();
		this.readyToPublish = t.isReadyToPublish();
		this.superTask = new TaskShort(t.getSuperTask());
		this.childTasks = addChildren(t);
		this.attachments = t.getAttachments();
		this.comments = t.getComments();
		this.userRelationships = calcFriends(t, u);
		this.taskCompetences = calcComps(t, u);
	}
	
	public Set<TaskShort> addChildren(Task t){
		
		Set<TaskShort> list = new HashSet<>();
		
		for(Task tc : t.getChildTasks()){
			list.add(new TaskShort(tc));
		}
		
		return list;
		
	}
	
	public Set<CompetenceRelationDetails> calcComps(Task t, CracUser u){
		
		Set<CompetenceRelationDetails> list = new HashSet<>();
		
		for(CompetenceTaskRel ctr : t.getMappedCompetences()){
			double bestVal = 0;
			for(UserCompetenceRel ucr : u.getCompetenceRelationships()){
				double newVal = CompetenceStorage.getCompetenceSimilarity(ucr.getCompetence(), ctr.getCompetence());
				if(newVal > bestVal){
					bestVal = newVal;
				}
			}
			
			System.out.println("name: "+ctr.getCompetence().getName()+" val: "+bestVal);
			CompetenceRelationDetails cd = new CompetenceRelationDetails(ctr.getCompetence());
			cd.setMandatory(ctr.isMandatory());
			cd.setRelationValue(bestVal);
			
			list.add(cd);
		}
			
		return list;
		
	}
	
	private Set<UserFriendDetails> calcFriends(Task t, CracUser u){
		
		Set<UserFriendDetails> list = new HashSet<>();
		UserRelationship found = null;
		boolean friend = false;
		CracUser otherU = null;
		
		for(UserTaskRel utr : t.getUserRelationships()){
			found = null;
			otherU = utr.getUser();
			for(UserRelationship ur : u.getUserRelationshipsAs1()){
				if(otherU.getId() == ur.getC2().getId()){
					found = ur;
				}
			}
			for(UserRelationship ur : u.getUserRelationshipsAs2()){
				if(utr.getUser().getId() == ur.getC1().getId()){
					found = ur;
				}
			}
			
			if(found != null){
				friend = found.isFriends();
			}else{
				friend = false;
			}
			
			UserFriendDetails fd = new UserFriendDetails(otherU, friend);
			
			if(otherU.getId() == u.getId()){
				fd.setSelf(true);
			}
			
			list.add(fd);
			
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

	public int getAmountOfVolunteers() {
		return amountOfVolunteers;
	}

	public void setAmountOfVolunteers(int amountOfVolunteers) {
		this.amountOfVolunteers = amountOfVolunteers;
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

}
