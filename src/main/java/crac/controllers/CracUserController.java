package crac.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
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
import crac.daos.CracUserDAO;
import crac.daos.GroupDAO;
import crac.models.Competence;
import crac.models.Task;
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


	/**
	 * GET / or blank -> get all users.
	 */

	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() throws JsonProcessingException {
		Iterable<CracUser> userList = userDAO.findAll();
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(userList));
	}

	/**
	 * GET /{user_id} -> get the user with given ID.
	 */

	@RequestMapping(value = "/{user_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "user_id") Long id) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		CracUser myUser = userDAO.findOne(id);
		return ResponseEntity.ok().body(mapper.writeValueAsString(myUser));
	}

	/**
	 * POST / or blank -> create a new user.
	 */

	@RequestMapping(value = { "/", "" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> create(@RequestBody String json) throws JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		CracUser myUser = mapper.readValue(json, CracUser.class);
		if (userDAO.findByName(myUser.getName()) == null) {
			userDAO.save(myUser);
		} else {
			return ResponseEntity.ok().body("{\"created\":\"false\", \"exception\":\"name already exists\"}");
		}

		return ResponseEntity.ok().body(
				"{\"user\":\"" + myUser.getId() + "\",\"name\":\"" + myUser.getName() + "\",\"created\":\"true\"}");

	}

	/**
	 * DELETE /{user_id} -> delete the user with given ID.
	 */

	@RequestMapping(value = "/{user_id}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> destroy(@PathVariable(value = "user_id") Long id) {
		CracUser deleteUser = userDAO.findOne(id);
		long userId = deleteUser.getId();
		String userName = deleteUser.getName();
		userDAO.delete(deleteUser);
		return ResponseEntity.ok()
				.body("{\"user\":\"" + userId + "\",\"name\":\"" + userName + "\",\"deleted\":\"true\"}");

	}

	/**
	 * PUT /{user_id} -> update the user with given ID.
	 */

	@RequestMapping(value = "/{user_id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> update(@RequestBody String json, @PathVariable(value = "user_id") Long id)
			throws JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		CracUser updatedUser = mapper.readValue(json, CracUser.class);
		CracUser oldUser = userDAO.findOne(id);

		String oldName = oldUser.getName();
		if (updatedUser.getName() != null) {
			oldUser.setName(updatedUser.getName());
		}
		if (updatedUser.getPassword() != null) {
			oldUser.setPassword(updatedUser.getPassword());
		}

		userDAO.save(oldUser);

		return ResponseEntity.ok().body("{\"user\":\"" + oldUser.getId() + "\",\"old_name\":\"" + oldName
				+ "\",\"new_name\":\"" + oldUser.getName() + "\",\"updated\":\"true\"}");

	}

	/**
	 * GET /me -> get the logged-in user.
	 */

	@RequestMapping(value = "/me", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getLogged() throws JsonProcessingException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(myUser));
	}

	/**
	 * Adds target competence to the currently logged-in user
	 * 
	 * @param competenceId
	 * @return ResponseEntity
	 */

	@RequestMapping(value = "/addCompetence/{competence_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addCompetence(@PathVariable(value = "competence_id") Long competenceId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Competence myCompetence = competenceDAO.findOne(competenceId);
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myUser.getCompetences().add(myCompetence);
		userDAO.save(myUser);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"competence\":\""
				+ myCompetence.getName() + "\", \"assigned\":\"true\"}");
	}
	
	/**
	 * Removes target competence from the currently logged-in user
	 * 
	 * @param competenceId
	 * @return ResponseEntity
	 */

	@RequestMapping(value = "/removeCompetence/{competence_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeCompetence(@PathVariable(value = "competence_id") Long competenceId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Competence myCompetence = competenceDAO.findOne(competenceId);
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myUser.getCompetences().remove(myCompetence);
		userDAO.save(myUser);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"competence\":\""
				+ myCompetence.getName() + "\", \"removed\":\"true\"}");
	}

	/**
	 * Adds target task to the open-tasks of the logged-in user
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/addTask/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addTask(@PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Task myTask = taskDAO.findOne(taskId);
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myUser.getOpenTasks().add(myTask);
		userDAO.save(myUser);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"task\":\"" + myTask.getName()
				+ "\", \"assigned\":\"true\"}");
	}
	
	/**
	 * Removes target task from the open-tasks of the logged-in user
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/removeTask/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeTask(@PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Task myTask = taskDAO.findOne(taskId);
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myUser.getOpenTasks().remove(myTask);
		userDAO.save(myUser);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"task\":\"" + myTask.getName()
				+ "\", \"removed\":\"true\"}");
	}

	/**
	 * Adds target group to the groups of the logged-in user
	 * 
	 * @param groupId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/enterGroup/{group_id}", method = RequestMethod.GET, produces = "application/json")
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
	
	/**
	 * Removes target group from the groups of the logged-in user
	 * 
	 * @param groupId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/leaveGroup/{group_id}", method = RequestMethod.GET, produces = "application/json")
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
	
	/**
	 * Adds target task to the follow-tasks of the logged-in user
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/followTask/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> followTask(@PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Task myTask = taskDAO.findOne(taskId);
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myUser.getFollowingTasks().add(myTask);
		userDAO.save(myUser);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"task\":\"" + myTask.getName()
				+ "\", \"assigned\":\"true\"}");
	}
	
	/**
	 * Removes target task from the follow-tasks of the logged-in user
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/unfollowTask/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> unfollowTask(@PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Task myTask = taskDAO.findOne(taskId);
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myUser.getFollowingTasks().remove(myTask);
		userDAO.save(myUser);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"task\":\"" + myTask.getName()
				+ "\", \"removed\":\"true\"}");
	}
	
	/**
	 * Adds target task to the responsible-tasks of the logged-in user
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/leadTask/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> leadTask(@PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Task myTask = taskDAO.findOne(taskId);
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myUser.getResponsibleForTasks().add(myTask);
		userDAO.save(myUser);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"task\":\"" + myTask.getName()
				+ "\", \"assigned\":\"true\"}");
	}
	
	/**
	 * Removes target task from the responsible-tasks of the logged-in user
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/abandonTask/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> abandonTask(@PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Task myTask = taskDAO.findOne(taskId);
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myUser.getResponsibleForTasks().remove(myTask);
		userDAO.save(myUser);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"task\":\"" + myTask.getName()
				+ "\", \"removed\":\"true\"}");
	}

	/**
	 * returns a json if the logged in user is valid
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> login() throws JsonMappingException, IOException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		return ResponseEntity.ok().body(
				"{\"user\":\"" + myUser.getId() + "\",\"name\":\"" + myUser.getName() + "\",\"login\":\"true\"}");

	}
	
}
