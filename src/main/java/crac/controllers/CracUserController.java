package crac.controllers;

import java.io.IOException;

import org.apache.jena.atlas.json.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CompetenceDAO;
import crac.daos.TaskDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.daos.UserTaskRelDAO;
import crac.elastic.ElasticConnector;
import crac.elastic.ElasticUser;
import crac.enums.TaskParticipationType;
import crac.enums.TaskState;
import crac.enums.Role;
import crac.elastic.ElasticTask;
import crac.daos.CracUserDAO;
import crac.daos.GroupDAO;
import crac.models.Competence;
import crac.models.Task;
import crac.relationmodels.UserCompetenceRel;
import crac.relationmodels.UserTaskRel;
import crac.utility.JSonResponseHelper;
import crac.utility.SearchTransformer;
import crac.utility.UpdateEntitiesHelper;
import crac.models.CracUser;
import crac.models.Group;

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

	
	private ElasticConnector<ElasticUser> ESConnUser = new ElasticConnector<ElasticUser>("localhost", 9300, "crac_core", "elastic_user");
	
	@Autowired
	private SearchTransformer ST;



	/**
	 * GET / or blank -> get all users.
	 */

	@RequestMapping(value = { "/all/", "/all" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() throws JsonProcessingException {
		Iterable<CracUser> userList = userDAO.findAll();
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(userList));
	}

	/**
	 * GET /{user_id} -> get the user with given ID.
	 */

	@RequestMapping(value = { "/{user_id}", "/{user_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "user_id") Long id) {
		ObjectMapper mapper = new ObjectMapper();
		CracUser user = userDAO.findOne(id);
		
		if(user != null){
			try {
				return ResponseEntity.ok().body(mapper.writeValueAsString(user));
			} catch (JsonProcessingException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.jsonWriteError();
			}
		}else{
			return JSonResponseHelper.idNotFound();
		}
		
	}

	/**
	 * GET / -> get the logged-in user.
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
	 * @param json
	 * @param id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
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
			ESConnUser.indexOrUpdate("" + oldUser.getId(), ST.transformUser(oldUser));
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

	@RequestMapping(value = { "/competence/{competence_id}/add", "/competence/{competence_id}/add/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addCompetence(@PathVariable(value = "competence_id") Long competenceId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());
		UserCompetenceRel rel = new UserCompetenceRel();

		Competence competence = competenceDAO.findOne(competenceId);
		
		if(competence != null){
			rel.setUser(user);
			rel.setCompetence(competence);
			user.getCompetenceRelationships().add(rel);
			userDAO.save(user);
			ESConnUser.indexOrUpdate("" + user.getId(), ST.transformUser(user));
			return JSonResponseHelper.successFullyAssigned(competence);
		}else{
			return JSonResponseHelper.idNotFound();
		}
		
	}
	
	/**
	 * Removes target competence from the currently logged-in user
	 * 
	 * @param competenceId
	 * @return ResponseEntity
	 */

	@RequestMapping(value = { "/competence/{competence_id}/remove", "/competence/{competence_id}/remove/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeCompetence(@PathVariable(value = "competence_id") Long competenceId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		Competence competence = competenceDAO.findOne(competenceId);
		
		if(competence != null){
			UserCompetenceRel rel = userCompetenceRelDAO.findByUserAndCompetence(user, competence);
			if(rel != null){
				userCompetenceRelDAO.delete(rel);
				ESConnUser.indexOrUpdate("" + user.getId(), ST.transformUser(user));
				return JSonResponseHelper.successFullyDeleted(competence);
			}else{
				return JSonResponseHelper.idNotFound();
			}
		}else{
			return JSonResponseHelper.idNotFound();
		}
		
		
	}

	/**
	 * Adds target task to the open-tasks of the logged-in user or changes it's state
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/task/{task_id}/{state_name}", "/task/{task_id}/{state_name}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> changeTaskState(@PathVariable(value = "state_name") String stateName, @PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		Task task = taskDAO.findOne(taskId);
		
		if(task != null){
			TaskParticipationType state = TaskParticipationType.PARTICIPATING;

			if (stateName.equals("participate")) {
				state = TaskParticipationType.PARTICIPATING;
			} else if (stateName.equals("follow")) {
				state = TaskParticipationType.FOLLOWING;
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

		}else{
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
		
		if(task != null){
			userTaskRelDAO.delete(userTaskRelDAO.findByUserAndTask(user, task));
			return JSonResponseHelper.successFullyDeleted(task);
		}else{
			return JSonResponseHelper.idNotFound();
		}
				
	}

	/**
	 * returns a json if the logged in user is valid
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/check", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> login() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());
		return JSonResponseHelper.checkUserSuccess(user);

	}
	
	/**
	 * returns the values for the enum taskParticipationType
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
	
	//KEEP OR DELETE
	
	/**
	 * Adds target group to the groups of the logged-in user
	 * 
	 * @param groupId
	 * @return ResponseEntity
	 */
	/*
	@RequestMapping(value = "/group/{group_id}/enter", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> enterGroup(@PathVariable(value = "group_id") Long groupId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Group myGroup= groupDAO.findOne(groupId);
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myGroup.getEnroledUsers().add(myUser);
		groupDAO.save(myGroup);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"group\":\"" + myGroup.getName()
				+ "\", \"assigned\":\"true\"}");
	}
	*/
	
	/**
	 * Removes target group from the groups of the logged-in user
	 * 
	 * @param groupId
	 * @return ResponseEntity
	 */
	/*
	@RequestMapping(value = "/group/{group_id}/leave", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> leaveGroup(@PathVariable(value = "group_id") Long groupId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Group myGroup= groupDAO.findOne(groupId);
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myGroup.getEnroledUsers().remove(myUser);
		groupDAO.save(myGroup);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"group\":\"" + myGroup.getName()
				+ "\", \"removed\":\"true\"}");
	}
	*/

	
}
