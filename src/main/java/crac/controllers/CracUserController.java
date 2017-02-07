package crac.controllers;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
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

import crac.daos.CompetenceDAO;
import crac.daos.TaskDAO;
import crac.daos.TokenDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.daos.UserRelationshipDAO;
import crac.daos.UserTaskRelDAO;
import crac.decider.core.Decider;
import crac.decider.core.UserFilterParameters;
import crac.enums.TaskParticipationType;
import crac.daos.CracUserDAO;
import crac.daos.GroupDAO;
import crac.daos.RoleDAO;
import crac.models.Competence;
import crac.models.Task;
import crac.models.output.TaskDetails;
import crac.models.output.TaskShort;
import crac.models.relation.UserCompetenceRel;
import crac.models.relation.UserRelationship;
import crac.models.relation.UserTaskRel;
import crac.models.utility.EvaluatedTask;
import crac.models.utility.SimpleUserRelationship;
import crac.models.CracToken;
import crac.notifier.NotificationHelper;
import crac.utility.JSonResponseHelper;
import crac.utility.UpdateEntitiesHelper;
import crac.models.CracUser;
import crac.models.Role;

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
		Iterable<CracUser> userList = userDAO.findAll();
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(userList));
	}

	/**
	 * Returns the user with given id
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{user_id}", "/{user_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "user_id") Long id) {
		ObjectMapper mapper = new ObjectMapper();
		CracUser user = userDAO.findOne(id);

		if (user != null) {
			try {
				return ResponseEntity.ok().body(mapper.writeValueAsString(user));
			} catch (JsonProcessingException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.jsonWriteError();
			}
		} else {
			return JSonResponseHelper.idNotFound();
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
		CracUser myUser = userDAO.findByName(userDetails.getName());
		ObjectMapper mapper = new ObjectMapper();
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(myUser));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
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
			return JSonResponseHelper.jsonMapError();
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonReadError();
		}

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser oldUser = userDAO.findByName(userDetails.getName());

		if (oldUser != null) {
			UpdateEntitiesHelper.checkAndUpdateUser(oldUser, updatedUser);
			userDAO.save(oldUser);
			return JSonResponseHelper.successFullyUpdated(oldUser);
		} else {
			return JSonResponseHelper.idNotFound();
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
			return JSonResponseHelper.successFullyAssigned(competence);
		} else {
			return JSonResponseHelper.idNotFound();
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
				return JSonResponseHelper.successFullyAssigned(competence);
			}else{
				return JSonResponseHelper.idNotFound();
			}
		} else {
			return JSonResponseHelper.idNotFound();
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

			ObjectMapper mapper = new ObjectMapper();
			try {
				return ResponseEntity.ok().body(mapper.writeValueAsString(found));
			} catch (JsonProcessingException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.jsonWriteError();
			}

		} else {
			return JSonResponseHelper.emptyData();
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
				return JSonResponseHelper.successFullyDeleted(competence);
			} else {
				return JSonResponseHelper.idNotFound();
			}
		} else {
			return JSonResponseHelper.idNotFound();
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
				if (task.isJoinable() && task.isLeaf()) {
					if (!task.isFull()) {
						state = TaskParticipationType.PARTICIPATING;
					} else {
						return JSonResponseHelper.actionNotPossible("This task is already full");
					}
				} else {
					return JSonResponseHelper.actionNotPossible("This task cannot be joined like this");
				}
			} else if (stateName.equals("follow")) {
				if (task.isJoinable()) {
					state = TaskParticipationType.FOLLOWING;
				} else {
					return JSonResponseHelper.actionNotPossible("This task cannot be joined like this");
				}
			} else if (stateName.equals("lead")) {
				state = TaskParticipationType.LEADING;
			} else {
				return JSonResponseHelper.stateNotAvailable(stateName);
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

			return JSonResponseHelper.successFullyAssigned(task);

		} else {
			return JSonResponseHelper.idNotFound();
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
				return JSonResponseHelper.successFullyDeleted(task);
			} else {
				return JSonResponseHelper.actionNotPossible("task already in progress");
			}
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

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
					return JSonResponseHelper.jsonWriteError();
				}
			}

		}

		return JSonResponseHelper.idNotFound();

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

			ObjectMapper mapper = new ObjectMapper();
			try {
				return ResponseEntity.ok().body(mapper.writeValueAsString(competenceRels));
			} catch (JsonProcessingException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.jsonWriteError();
			}

		} else {
			return JSonResponseHelper.emptyData();
		}

	}

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
				return JSonResponseHelper.jsonWriteError();
			}

		} else {
			return JSonResponseHelper.emptyData();
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
		return JSonResponseHelper.checkUserSuccess(user);

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
			return JSonResponseHelper.tokenFailure(user, t);
		} else {
			CracToken token = new CracToken();

			SecureRandom random = new SecureRandom();
			String code = new BigInteger(130, random).toString(32);

			token.setCode(code);
			token.setUserId(user.getId());
			tokenDAO.save(token);
			return JSonResponseHelper.tokenSuccess(user, token);
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
			userDAO.save(user);
			tokenDAO.delete(t);
			return JSonResponseHelper.tokenDestroySuccess(user);
		} else {
			return JSonResponseHelper.tokenDestroyFailure(user);
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

		ObjectMapper mapper = new ObjectMapper();
		Decider unit = new Decider();
		
		try {
			return JSonResponseHelper.print(mapper.writeValueAsString(unit.findTasks(user, new UserFilterParameters(), taskDAO)));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}

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

		ObjectMapper mapper = new ObjectMapper();
		Decider unit = new Decider();
		
		ArrayList<EvaluatedTask> tasks = new ArrayList<>();
		
		int count = 0;
		
		for(EvaluatedTask task : unit.findTasks(user, new UserFilterParameters(), taskDAO)){
			
			if(count == numberOfTasks){
				break;
			}
			
			tasks.add(task);
			count ++;
		}
				
		try {
			return JSonResponseHelper.print(mapper.writeValueAsString(tasks));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}

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
		NotificationHelper.createFriendRequest(sender.getId(), receiver.getId());
		return JSonResponseHelper.successfullFriendRequest(receiver);
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
				return JSonResponseHelper.successfullUnfriend(friend);
			} else {
				return JSonResponseHelper.actionNotPossible("these users are not friends");
			}
		} else {
			return JSonResponseHelper.actionNotPossible("no such relationship found");
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

		ObjectMapper mapper = new ObjectMapper();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return ResponseEntity.ok().headers(headers).body(mapper.writeValueAsString(friends));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
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

		ObjectMapper mapper = new ObjectMapper();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return ResponseEntity.ok().headers(headers).body(mapper.writeValueAsString(rels));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
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
			return JSonResponseHelper.successFullyAssigned(role);
		} else {
			return JSonResponseHelper.idNotFound();
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
			return JSonResponseHelper.successFullyDeleted(role);
		} else {
			return JSonResponseHelper.idNotFound();
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
