package crac.models.output;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Attachment;
import crac.models.db.entities.Comment;
import crac.models.db.entities.CracGroup;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Material;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;
import crac.module.storage.CompetenceStorage;
import lombok.Getter;
import lombok.Setter;

public class TaskDetails {

	@Getter
	@Setter
	private long id;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String address;

	@Getter
	@Setter
	private String location;

	@Getter
	@Setter
	private double geoLat;

	@Getter
	@Setter
	private double geoLng;

	@Getter
	@Setter
	private String geoName;
	
	@Getter
	@Setter
	private String geoCountry;
	
	@Getter
	@Setter
	private String geoCountryA;
	
	@Getter
	@Setter
	private String geoMacroRegion;
	
	@Getter
	@Setter
	private String geoRegion;
	
	@Getter
	@Setter
	private String geoLocality;

	@Getter
	@Setter
	private Calendar startTime;

	@Getter
	@Setter
	private Calendar endTime;

	@Getter
	@Setter
	private int minAmountOfVolunteers;

	@Getter
	@Setter
	private int maxAmountOfVolunteers;

	@Getter
	@Setter
	private int signedUsers;

	@Getter
	@Setter
	private TaskState taskState;

	@Getter
	@Setter
	private boolean readyToPublish;

	@Getter
	private Calendar creationDate;

	@Getter
	@Setter
	private TaskShort superTask;

	@Getter
	@Setter
	private Set<TaskShort> childTasks;

	@Getter
	private CracUser creator;

	@Getter
	@Setter
	private Set<Attachment> attachments;

	@Getter
	@Setter
	private Set<Comment> comments;

	@Getter
	@Setter
	private Set<Evaluation> mappedEvaluations;

	@Getter
	@Setter
	private Set<UserFriendDetails> userRelationships;

	@Getter
	@Setter
	private Set<CompetenceRelationDetails> taskCompetences;

	@Getter
	@Setter
	private Set<MaterialDetails> materials;

	@Getter
	@Setter
	private TaskType taskType;

	@Getter
	@Setter
	private Set<UserTaskRel> participationDetails;
	
	@Getter
	@Setter
	private Set<CracGroup> restrictingGroups;
	
	@Getter
	@Setter
	private Set<CracGroup> invitedGroups;

	@Getter
	@Setter
	private boolean assigned;

	@Getter
	@Setter
	private boolean permissions;

	public TaskDetails(Task t, CracUser u, UserTaskRelDAO userTaskRelDAO, CompetenceStorage cs) {
		this.id = t.getId();
		this.creationDate = t.getCreationDate();
		this.name = t.getName();
		this.description = t.getDescription();
		this.address = t.getAddress();
		this.location = t.getLocation();
		this.geoLat = t.getGeoLat();
		this.geoLng = t.getGeoLng();
		this.geoName = t.getGeoName();
		this.geoCountry = t.getGeoCountry();
		this.geoCountryA = t.getGeoCountryA();
		this.geoMacroRegion = t.getGeoMacroRegion();
		this.geoRegion = t.getGeoRegion();
		this.geoLocality = t.getGeoLocality();
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
		//TODO one call!
		this.participationDetails = userTaskRelDAO.findByUserAndTask(u, t);
		this.taskCompetences = new HashSet<>();
		this.taskCompetences = calcComps(t, u, cs);
		if (!this.participationDetails.isEmpty()) {
			this.assigned = true;
		} else {
			this.participationDetails = new HashSet<>();
			UserTaskRel rel = t.getIndirectLead(u);
			if(rel != null){
				this.participationDetails.add(rel);
			}
			this.assigned = false;
		}
		this.materials = addMaterials(t);
		this.taskType = t.getTaskType();
		this.permissions = u.hasTaskPermissions(t);
		this.invitedGroups = t.getInvitedGroups();
		this.restrictingGroups = t.getRestrictingGroups();
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

	public Set<CompetenceRelationDetails> calcComps(Task t, CracUser u, CompetenceStorage cs) {

		Set<CompetenceRelationDetails> list = new HashSet<>();

		Set<CompetenceTaskRel> mctr = t.getMappedCompetences();
		
		if (mctr != null) {
			if (mctr.size() != 0) {
				for (CompetenceTaskRel ctr : mctr) {
					double bestVal = 0;
					if (u.getCompetenceRelationships() != null) {
						for (UserCompetenceRel ucr : u.getCompetenceRelationships()) {
							double newVal = cs.getCompetenceSimilarity(ucr.getCompetence(),
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
		Set<UserTaskRel> participantRels = t.getAllLeaderAndParticipantRels();
		//TODO better optimized way
		this.signedUsers = t.getAllParticipants().size();

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

}
