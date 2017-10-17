package crac.models.output;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import crac.enums.ConcreteTaskState;
import crac.enums.TaskParticipationType;
import crac.enums.TaskType;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Attachment;
import crac.models.db.entities.Comment;
import crac.models.db.entities.CracGroup;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Material;
import crac.models.db.entities.Task;
import crac.models.db.entities.Task.TaskShort;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import crac.models.db.relation.UserMaterialSubscription;
import crac.models.db.relation.UserMaterialSubscription.SubscriptionShort;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;
import crac.module.storage.CompetenceStorage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class TaskDetails {

	private long id;
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
	private Calendar startTime;
	private Calendar endTime;
	private int minAmountOfVolunteers;
	private int maxAmountOfVolunteers;
	private int signedUsers;
	private ConcreteTaskState taskState;
	private boolean readyToPublish;
	private Calendar creationDate;
	private TaskShort superTask;
	private Set<TaskShort> childTasks;
	private CracUser creator;
	private Set<Attachment> attachments;
	private Set<Comment> comments;
	private Set<SubscriptionShort> materialSubscription;
	private Set<Evaluation> mappedEvaluations;
	private Set<UserFriendDetails> userRelationships;
	private Set<CompetenceRelationDetails> taskCompetences;
	private Set<MaterialDetails> materials;
	private TaskType taskType;
	private Set<UserTaskRel> participationDetails;
	private Set<CracGroup> restrictingGroups;
	private Set<CracGroup> invitedGroups;
	private boolean assigned;
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
			this.superTask = t.getSuperTask().toShort();
		} else {
			this.superTask = null;
		}
		this.childTasks = addChildren(t);
		this.attachments = t.getAttachments();
		this.comments = t.getComments();
		this.userRelationships = calcFriends(t, u);
		this.taskCompetences = new HashSet<>();
		this.taskCompetences = calcComps(t, u, cs);
		
		this.participationDetails = userTaskRelDAO.findByUserAndTask(u, t);
		if (!this.participationDetails.isEmpty()) {
			this.assigned = true;
		} else {
			this.participationDetails = t.getRelationships(1, TaskParticipationType.LEADING, TaskParticipationType.PARTICIPATING);
			this.participationDetails.removeIf(rel -> rel.getUser().getId() != u.getId());
			this.assigned = false;
		}
		
		this.materials = addMaterials(t);
		this.taskType = t.getTaskType();
		this.permissions = t.isLeader(u);
		this.invitedGroups = t.getInvitedGroups();
		this.restrictingGroups = t.getRestrictingGroups();
		this.materialSubscription = t.getMaterials().stream()
				.map( m -> m.getSubscribedUsers() )
				.flatMap( l -> l.stream())
				.map( m -> m.toShort() )
				.collect(Collectors.toSet() );			
		
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
				list.add(tc.toShort());
			}
		}
		return list;

	}

	public Set<CompetenceRelationDetails> calcComps(Task t, CracUser u, CompetenceStorage cs) {

		Set<CompetenceRelationDetails> list = new HashSet<>();

		Set<CompetenceTaskRel> mctr = t.getMappedCompetences();
		
		mctr.forEach( ctr -> {
			double bestVal = u.getCompetenceRelationships().stream()
					.map( ucr -> cs.getCompetenceSimilarity(ucr.getCompetence(), ctr.getCompetence()) )
					.max(Double::compare)
					.get();
			CompetenceRelationDetails cd = new CompetenceRelationDetails(ctr.getCompetence());
			cd.setMandatory(ctr.isMandatory());
			cd.setRelationValue(bestVal);
			cd.setImportanceLevel(ctr.getImportanceLevel());
			cd.setNeededProficiencyLevel(ctr.getNeededProficiencyLevel());

			list.add(cd);
		});
		
		return list;

	}

	private Set<UserFriendDetails> calcFriends(Task t, CracUser u) {

		Set<UserTaskRel> participantRels = t.getRelationships(0, TaskParticipationType.PARTICIPATING);
		
		this.signedUsers = participantRels.size();

		participantRels.addAll(t.getRelationships(1, TaskParticipationType.LEADING));

		return participantRels.stream()
				.map( rel -> new UserFriendDetails(u, u.isFriend(rel.getUser()), rel) )
				.collect(Collectors.toSet());
		
	}

}
