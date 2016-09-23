package crac.controllers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import crac.daos.UserCompetenceRelDAO;
import crac.daos.UserRelationshipDAO;
import crac.daos.UserTaskRelDAO;
import crac.enums.TaskParticipationType;
import crac.enums.Role;
import crac.daos.CracUserDAO;
import crac.daos.GroupDAO;
import crac.models.Competence;
import crac.models.Task;
import crac.notifier.NotificationHelper;
import crac.relationmodels.UserCompetenceRel;
import crac.relationmodels.UserRelationship;
import crac.relationmodels.UserTaskRel;
import crac.utility.JSonResponseHelper;
import crac.utility.SearchHelper;
import crac.utility.UpdateEntitiesHelper;
import crac.utilityModels.SimpleUserRelationship;
import crac.models.CracUser;

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
	private SearchHelper searchHelper;

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
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
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

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser oldUser = userDAO.findByName(userDetails.getUsername());

		if (oldUser != null) {
			UpdateEntitiesHelper.checkAndUpdateUser(oldUser, updatedUser);
			userDAO.save(oldUser);
			return JSonResponseHelper.successFullyUpdated(oldUser);
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	/**
	 * Adds target competence to the currently logged-in user
	 * 
	 * @param competenceId
	 * @return ResponseEntity
	 */

	@RequestMapping(value = { "/competence/{competence_id}/add",
			"/competence/{competence_id}/add/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addCompetence(@PathVariable(value = "competence_id") Long competenceId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());
		UserCompetenceRel rel = new UserCompetenceRel();

		Competence competence = competenceDAO.findOne(competenceId);

		if (competence != null) {
			rel.setUser(user);
			rel.setCompetence(competence);
			user.getCompetenceRelationships().add(rel);
			userDAO.save(user);
			return JSonResponseHelper.successFullyAssigned(competence);
		} else {
			return JSonResponseHelper.idNotFound();
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

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

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
	 * Removes target task from the open-tasks of the logged-in user
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/task/{task_id}/remove", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeTask(@PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		Task task = taskDAO.findOne(taskId);

		if (task != null) {
			userTaskRelDAO.delete(userTaskRelDAO.findByUserAndTask(user, task));
			return JSonResponseHelper.successFullyDeleted(task);
		} else {
			return JSonResponseHelper.idNotFound();
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

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		Set<UserTaskRel> taskRels = userTaskRelDAO.findByUser(user);

		if (taskRels.size() != 0) {
			Set<Task> taskListFollow = new HashSet<Task>();
			Set<Task> taskListPart = new HashSet<Task>();
			Set<Task> taskListLead = new HashSet<Task>();

			for (UserTaskRel utr : taskRels) {
				if (utr.getParticipationType() == TaskParticipationType.FOLLOWING) {
					taskListFollow.add(utr.getTask());
				} else if (utr.getParticipationType() == TaskParticipationType.PARTICIPATING) {
					taskListPart.add(utr.getTask());
				} else if (utr.getParticipationType() == TaskParticipationType.LEADING) {
					taskListLead.add(utr.getTask());
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
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());
		return JSonResponseHelper.checkUserSuccess(user);

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

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		ObjectMapper mapper = new ObjectMapper();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return ResponseEntity.ok().headers(headers).body(mapper.writeValueAsString(searchHelper.findMatch(user)));
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
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser sender = userDAO.findByName(userDetails.getUsername());
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
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser me = userDAO.findByName(userDetails.getUsername());
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
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

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
	 * Shows the friends of the logged in user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/relationships",
			"/relationships/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> showRelationships() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

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
	 * returns the values for the enum taskParticipationType
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/roles", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> roles() {

		ObjectMapper mapper = new ObjectMapper();

		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(Role.values()));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
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
