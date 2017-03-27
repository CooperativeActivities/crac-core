package crac.controllers;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.decider.core.Decider;
import crac.decider.core.UserFilterParameters;
import crac.enums.ErrorCause;
import crac.enums.RESTAction;
import crac.enums.TaskParticipationType;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.GroupDAO;
import crac.models.db.daos.RoleDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.TokenDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracToken;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Role;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserCompetenceRel;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;
import crac.models.output.TaskDetails;
import crac.models.output.TaskShort;
import crac.models.utility.EvaluatedTask;
import crac.models.utility.SimpleUserRelationship;
import crac.notifier.NotificationHelper;
import crac.notifier.notifications.FriendRequest;
import crac.utility.JSonResponseHelper;
import crac.utility.UpdateEntitiesHelper;

/**
 * REST controller for managing users.
 */

@RestController
@RequestMapping("/user")
public class CracUserController {

	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private GroupDAO groupDAO;

	@Autowired
	private UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	private UserTaskRelDAO userTaskRelDAO;

	@Autowired
	private UserRelationshipDAO userRelationshipDAO;

	@Autowired
	private RoleDAO roleDAO;

	@Autowired
	private TokenDAO tokenDAO;

	/**
	 * Returns all users
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/all/", "/all" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() throws JsonProcessingException {
		return JSonResponseHelper.createResponse(userDAO.findAll(), true);
	}

	/**
	 * Returns the user with given id
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{user_id}", "/{user_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "user_id") Long id) {
		CracUser user = userDAO.findOne(id);

		if (user != null) {
			return JSonResponseHelper.createResponse(user, true);
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Returns the logged in user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getLogged() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		return JSonResponseHelper.createResponse(userDAO.findByName(userDetails.getName()), true);
	}

	/**
	 * Update the currently logged in user
	 * 
	 * @param json
	 * @param id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/",
			"" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateLogged(@RequestBody String json) {
		ObjectMapper mapper = new ObjectMapper();
		CracUser updatedUser;
		try {
			updatedUser = mapper.readValue(json, CracUser.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser oldUser = userDAO.findByName(userDetails.getName());

		if (oldUser != null) {
			UpdateEntitiesHelper.checkAndUpdateUser(oldUser, updatedUser);
			userDAO.save(oldUser);
			return JSonResponseHelper.successfullyUpdated(oldUser);
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Add a competence with given ID to the currently logged in user, likeValue and proficiencyValue are mandatory
	 * 
	 * @param competenceId
	 * @return ResponseEntity
	 */

	@RequestMapping(value = { "/competence/{competence_id}/add/{likeValue}/{proficiencyValue}",
			"/competence/{competence_id}/add/{likeValue}/{proficiencyValue}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addCompetence(@PathVariable(value = "competence_id") Long competenceId,
			@PathVariable(value = "likeValue") int likeValue,
			@PathVariable(value = "proficiencyValue") int proficiencyValue) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		UserCompetenceRel rel = new UserCompetenceRel();

		Competence competence = competenceDAO.findOne(competenceId);

		if (competence != null) {
			rel.setUser(user);
			rel.setCompetence(competence);
			rel.setLikeValue(likeValue);
			rel.setProficiencyValue(proficiencyValue);
			user.getCompetenceRelationships().add(rel);
			userDAO.save(user);
			return JSonResponseHelper.successfullyAssigned(competence);
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Adjust the values of a user-competence connection
	 * @param competenceId
	 * @param likeValue
	 * @param proficiencyValue
	 * @return
	 */
	@RequestMapping(value = { "/competence/{competence_id}/adjust/{likeValue}/{proficiencyValue}",
			"/competence/{competence_id}/adjust/{likeValue}/{proficiencyValue}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> adjustCompetence(@PathVariable(value = "competence_id") Long competenceId,
			@PathVariable(value = "likeValue") int likeValue,
			@PathVariable(value = "proficiencyValue") int proficiencyValue) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Competence competence = competenceDAO.findOne(competenceId);

		if (competence != null) {
			UserCompetenceRel ucr = userCompetenceRelDAO.findByUserAndCompetence(user, competence);
			
			if(ucr != null){
				ucr.setLikeValue(likeValue);
				ucr.setProficiencyValue(proficiencyValue);
				userCompetenceRelDAO.save(ucr);
				return JSonResponseHelper.successfullyAssigned(competence);
			}else{
				return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
			}
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}
	
	@RequestMapping(value = { "/competence/available", "/competence/available/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getAvailableCompetences() {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Iterable<Competence> competenceList = competenceDAO.findAll();

		Set<UserCompetenceRel> competenceRels = user.getCompetenceRelationships();

		ArrayList<Competence> found = new ArrayList<Competence>();

		if (competenceList != null) {
			for (Competence c : competenceList) {
				boolean in = false;
				for (UserCompetenceRel ucr : competenceRels) {
					if (c.getId() == ucr.getCompetence().getId()) {
						in = true;
					}
				}
				if (!in) {
					found.add(c);
				}
			}
		}

		if (found.size() != 0) {
			return JSonResponseHelper.createResponse(found, true);		
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.EMPTY_DATA);
		}

	}

	/**
	 * Removes target competence from the currently logged-in user
	 * 
	 * @param competenceId
	 * @return ResponseEntity
	 */

	@RequestMapping(value = { "/competence/{competence_id}/remove",
			"/competence/{competence_id}/remove/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeCompetence(@PathVariable(value = "competence_id") Long competenceId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Competence competence = competenceDAO.findOne(competenceId);

		if (competence != null) {
			UserCompetenceRel rel = userCompetenceRelDAO.findByUserAndCompetence(user, competence);
			if (rel != null) {
				userCompetenceRelDAO.delete(rel);
				return JSonResponseHelper.successfullyDeleted(competence);
			} else {
				return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
			}
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Adds target task to the open-tasks of the logged-in user or changes it's
	 * state
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/task/{task_id}/{state_name}",
			"/task/{task_id}/{state_name}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> changeTaskState(@PathVariable(value = "state_name") String stateName,
			@PathVariable(value = "task_id") Long taskId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task task = taskDAO.findOne(taskId);

		if (task != null) {
			TaskParticipationType state = TaskParticipationType.PARTICIPATING;
			if (stateName.equals("participate")) {
				if (task.isJoinable()) {
					if (!task.isFull()) {
						state = TaskParticipationType.PARTICIPATING;
					} else {
						return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_IS_FULL);
					}
				} else {
					return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_NOT_JOINABLE);
				}
			} else if (stateName.equals("follow")) {
				if (task.isFollowable()) {
					state = TaskParticipationType.FOLLOWING;
				} else {
					return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_NOT_JOINABLE);
				}
			}/* else if (stateName.equals("lead")) {
				state = TaskParticipationType.LEADING;
			} */else {
				HashMap<String, Object> meta = new HashMap<>();
				meta.put("state", stateName);
				return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.STATE_NOT_AVAILABLE, meta);
			}

			UserTaskRel rel = userTaskRelDAO.findByUserAndTask(user, task);

			if (rel == null) {
				rel = new UserTaskRel();
				rel.setUser(user);
				rel.setTask(task);
				rel.setParticipationType(state);
				user.getTaskRelationships().add(rel);
				userDAO.save(user);
			} else {
				rel.setParticipationType(state);
				userTaskRelDAO.save(rel);
			}

			return JSonResponseHelper.successfullyAssigned(task);

		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Removes target task from the open-tasks of the logged-in user
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/task/{task_id}/remove", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeTask(@PathVariable(value = "task_id") Long taskId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task task = taskDAO.findOne(taskId);

		if (task != null) {
			if (!task.inConduction()) {
				userTaskRelDAO.delete(userTaskRelDAO.findByUserAndTask(user, task));
				return JSonResponseHelper.successfullyDeleted(task);
			} else {
				return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_ALREADY_IN_PROCESS);
			}
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}
	
	//TODO Verbinde TaskDetail und Relation

	/**
	 * Returns target task and its relationship to the logged in user
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/task/{task_id}",
			"/task/{task_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getUserRelTask(@PathVariable(value = "task_id") Long taskId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task task = taskDAO.findOne(taskId);

		if (task != null) {
			UserTaskRel rel = userTaskRelDAO.findByUserAndTask(user, task);
			if (rel != null) {
				ObjectMapper mapper = new ObjectMapper();
				try {
					return ResponseEntity.ok()
							.body("[" + mapper.writeValueAsString(new TaskDetails(task, user)) + "," + mapper.writeValueAsString(rel) + "]");
				} catch (JsonProcessingException e) {
					System.out.println(e.toString());
					return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_WRITE_ERROR);
				}
			}

		}

		return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);

	}

	/**
	 * Returns the competences of the currently logged in user, wrapped in the
	 * relationship-object
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/competence",
			"/competence/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getCompetences() {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Set<UserCompetenceRel> competenceRels = userCompetenceRelDAO.findByUser(user);

		if (competenceRels.size() != 0) {		
			return JSonResponseHelper.createResponse(competenceRels, true);

		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.EMPTY_DATA);
		}

	}

	//TODO what should we do with that
	/**
	 * Returns all tasks of logged in user, divided in the
	 * TaskParticipationTypes
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/task", "/task/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getTasks() {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Set<UserTaskRel> taskRels = userTaskRelDAO.findByUser(user);

		if (taskRels.size() != 0) {
			Set<TaskShort> taskListFollow = new HashSet<TaskShort>();
			Set<TaskShort> taskListPart = new HashSet<TaskShort>();
			Set<TaskShort> taskListLead = new HashSet<TaskShort>();

			for (UserTaskRel utr : taskRels) {
				if (utr.getParticipationType() == TaskParticipationType.FOLLOWING) {
					taskListFollow.add(new TaskShort(utr.getTask()));
				} else if (utr.getParticipationType() == TaskParticipationType.PARTICIPATING) {
					taskListPart.add(new TaskShort(utr.getTask()));
				} else if (utr.getParticipationType() == TaskParticipationType.LEADING) {
					taskListLead.add(new TaskShort(utr.getTask()));
				}
			}

			ObjectMapper mapper = new ObjectMapper();
			try {
				return ResponseEntity.ok()
						.body("{\"following\": " + mapper.writeValueAsString(taskListFollow) + ", \"participating\": "
								+ mapper.writeValueAsString(taskListPart) + ", \"leading\": "
								+ mapper.writeValueAsString(taskListLead) + "}");
			} catch (JsonProcessingException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_WRITE_ERROR);
			}

		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.EMPTY_DATA);
		}

	}

	/**
	 * returns a json if the logged in user is valid
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/check", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> checkLoginData() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		return JSonResponseHelper.createResponse(user, true);

	}

	/**
	 * Get a valid token for the system and confirm your user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> loginUser() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		CracToken t = tokenDAO.findByUserId(user.getId());
		if (t != null) {
			
			HashMap<String, Object> meta = new HashMap<>();
			meta.put("action", "CREATE_TOKEN");
			meta.put("issue", "TOKEN_ALREADY_CREATED");
			meta.put("user", user);
			meta.put("roles", user.getRoles());
			return JSonResponseHelper.createResponse(t, true, meta);
		} else {
			CracToken token = new CracToken();

			SecureRandom random = new SecureRandom();
			String code = new BigInteger(130, random).toString(32);

			token.setCode(code);
			token.setUserId(user.getId());
			tokenDAO.save(token);
			
			HashMap<String, Object> meta = new HashMap<>();
			meta.put("action", "CREATE_TOKEN");
			meta.put("user", user);
			meta.put("roles", user.getRoles());
			return JSonResponseHelper.createResponse(token, true, meta);
		}

	}

	/**
	 * Delete your token
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> logoutUser() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		CracToken t = tokenDAO.findByUserId(user.getId());
		if (t != null) {
			ResponseEntity<String> v = JSonResponseHelper.createResponse(t, true, RESTAction.DELETE);
			userDAO.save(user);
			tokenDAO.delete(t);
			return v;
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.NO_TOKEN, RESTAction.DELETE);
		}

	}

	/**
	 * Return a sorted list of elements with the best fitting tasks for the
	 * logged in user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/findMatchingTasks",
			"/findMatchingTasks/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> findTasks() {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Decider unit = new Decider();
		
		return JSonResponseHelper.createResponse(unit.findTasks(user, new UserFilterParameters()), true);
		
	}
	
	/**
	 * Return a sorted list of a defined number of elements with the best fitting tasks for the
	 * logged in user.
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/findMatchingTasks/{number_of_tasks}",
			"/findMatchingTasks/{number_of_tasks}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> findBestTasks(@PathVariable(value = "number_of_tasks") int numberOfTasks) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Decider unit = new Decider();
		
		ArrayList<EvaluatedTask> tasks = new ArrayList<>();
		
		int count = 0;
		
		for(EvaluatedTask task : unit.findTasks(user, new UserFilterParameters())){
			
			if(count == numberOfTasks){
				break;
			}
			
			tasks.add(task);
			count ++;
		}
			
		return JSonResponseHelper.createResponse(tasks, true);
		
	}


	/**
	 * Issues a friend-request-notification to target user
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{user_id}/friend",
			"/{user_id}/friend/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addFriend(@PathVariable(value = "user_id") Long id) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser sender = userDAO.findByName(userDetails.getName());
		CracUser receiver = userDAO.findOne(id);
		
		FriendRequest n = new FriendRequest(sender.getId(), receiver.getId());
		NotificationHelper.createNotification(n);
		return JSonResponseHelper.successfullyCreated(n);
	}

	/**
	 * Unfriends target user
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{user_id}/unfriend",
			"/{user_id}/unfriend/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeFriend(@PathVariable(value = "user_id") Long id) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser me = userDAO.findByName(userDetails.getName());
		CracUser friend = userDAO.findOne(id);
		UserRelationship rel = userRelationshipDAO.findByC1AndC2(me, friend);
		if (rel != null) {
			if (rel.isFriends()) {
				rel.setLikeValue(0.5);
				rel.setFriends(false);
				userRelationshipDAO.save(rel);
				return JSonResponseHelper.successfullyDeleted(friend);
				//return JSonResponseHelper.successfullUnfriend(friend);
			} else {
				return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.USERS_NOT_FRIENDS);
			}
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}
	}

	/**
	 * Shows the friends of the logged in user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/friends", "/friends/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> showFriends() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Set<CracUser> friends = new HashSet<CracUser>();

		for (UserRelationship ur : user.getUserRelationshipsAs1()) {
			if (ur.isFriends()) {
				friends.add(ur.getC2());
			}
		}

		for (UserRelationship ur : user.getUserRelationshipsAs2()) {
			if (ur.isFriends()) {
				friends.add(ur.getC1());
			}
		}
		
		return JSonResponseHelper.createResponse(friends, true);

	}

	/**
	 * Shows the relationships of the logged in user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/relationships",
			"/relationships/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> showRelationships() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Set<SimpleUserRelationship> rels = new HashSet<SimpleUserRelationship>();

		for (UserRelationship ur : user.getUserRelationshipsAs1()) {
			rels.add(new SimpleUserRelationship(ur.getC2(), ur.getLikeValue(), ur.isFriends()));
		}

		for (UserRelationship ur : user.getUserRelationshipsAs2()) {
			rels.add(new SimpleUserRelationship(ur.getC1(), ur.getLikeValue(), ur.isFriends()));
		}

		return JSonResponseHelper.createResponse(rels, true);

	}

	/**
	 * Adds a role to the logged in User
	 * 
	 * @param roleId
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/role/{role_id}/add",
			"/role/{role_id}/add/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addRole(@PathVariable(value = "role_id") Long roleId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		Role role = roleDAO.findOne(roleId);

		if (role != null) {
			user.getRoles().add(role);
			userDAO.save(user);
			return JSonResponseHelper.successfullyAssigned(role);
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Removes a role from the logged in user
	 * 
	 * @param roleId
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/role/{role_id}/remove",
			"/role/{role_id}/remove/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeRole(@PathVariable(value = "role_id") Long roleId) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		Role role = roleDAO.findOne(roleId);

		if (user.getRoles().contains(role)) {
			user.getRoles().remove(role);
			return JSonResponseHelper.successfullyDeleted(role);
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}
	}

	// KEEP OR DELETE

	/**
	 * Adds target group to the groups of the logged-in user
	 * 
	 * @param groupId
	 * @return ResponseEntity
	 */
	/*
	 * @RequestMapping(value = "/group/{group_id}/enter", method =
	 * RequestMethod.GET, produces = "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String>
	 * enterGroup(@PathVariable(value = "group_id") Long groupId) {
	 * 
	 * UserDetails userDetails = (UserDetails)
	 * SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	 * 
	 * Group myGroup= groupDAO.findOne(groupId); CracUser myUser =
	 * userDAO.findByName(userDetails.getUsername());
	 * myGroup.getEnroledUsers().add(myUser); groupDAO.save(myGroup); return
	 * ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() +
	 * "\", \"group\":\"" + myGroup.getName() + "\", \"assigned\":\"true\"}"); }
	 */

	/**
	 * Removes target group from the groups of the logged-in user
	 * 
	 * @param groupId
	 * @return ResponseEntity
	 */
	/*
	 * @RequestMapping(value = "/group/{group_id}/leave", method =
	 * RequestMethod.GET, produces = "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String>
	 * leaveGroup(@PathVariable(value = "group_id") Long groupId) {
	 * 
	 * UserDetails userDetails = (UserDetails)
	 * SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	 * 
	 * Group myGroup= groupDAO.findOne(groupId); CracUser myUser =
	 * userDAO.findByName(userDetails.getUsername());
	 * myGroup.getEnroledUsers().remove(myUser); groupDAO.save(myGroup); return
	 * ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() +
	 * "\", \"group\":\"" + myGroup.getName() + "\", \"removed\":\"true\"}"); }
	 */

}
