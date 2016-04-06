package crac.controllers;

import java.io.IOException;
import java.util.ArrayList;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CompetenceDAO;
import crac.daos.TaskDAO;
import crac.daos.CracUserDAO;
import crac.models.Competence;
import crac.models.Task;
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
	 * POST / -> create a new user.
	 */

	@RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
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

	@RequestMapping(value = "/addCompetence", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addCompetenceByName(@RequestBody String json)
			throws JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Competence myCompetence = mapper.readValue(json, Competence.class);
		Competence realCompetence = competenceDAO.findByName(myCompetence.getName());
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myUser.getCompetencies().add(realCompetence);
		userDAO.save(myUser);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"competence\":\""
				+ realCompetence.getName() + "\", \"assigned\":\"true\"}");
	}

	@RequestMapping(value = "/addTask", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addTask(@RequestBody String json) throws JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Task myTask = mapper.readValue(json, Task.class);
		Task realTask = taskDAO.findByName(myTask.getName());
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		myUser.getOpenTasks().add(realTask);
		userDAO.save(myUser);
		return ResponseEntity.ok().body("{\"user\":\"" + myUser.getName() + "\", \"task\":\"" + realTask.getName()
				+ "\", \"assigned\":\"true\"}");
	}
	/*
	 * TODO:Rewrite when login is done
	 * 
	 * @RequestMapping("/getTasks")
	 * 
	 * @ResponseBody public String getTasks(long user){
	 * 
	 * Set<Task> myTasks = null;
	 * 
	 * try { myTasks = userDAO.findOne(user).getOpenTasks(); } catch (Exception
	 * ex) { return "Error: " + ex.toString(); } String print = "";
	 * 
	 * for (Task s : myTasks) { print += s.getName(); }
	 * 
	 * return print; }
	 * 
	 * @RequestMapping("/getCompetences")
	 * 
	 * @ResponseBody public String getCompetences(long user){
	 * 
	 * Set<Competence> myCompetencies = null;
	 * 
	 * try { myCompetencies = userDAO.findOne(user).getCompetencies(); } catch
	 * (Exception ex) { return "Error: " + ex.toString(); } String print = "";
	 * 
	 * for (Competence s : myCompetencies) { print += s.getName(); }
	 * 
	 * return print; }
	 * 
	 * @RequestMapping("/getCreatedTasks")
	 * 
	 * @ResponseBody public String getCreatedTasks(long user){
	 * 
	 * Set<Task> myTasks = null;
	 * 
	 * try { myTasks = userDAO.findOne(user).getCreated_tasks(); } catch
	 * (Exception ex) { return "Error: " + ex.toString(); }
	 * 
	 * String print = "";
	 * 
	 * for (Task s : myTasks) { print += s.getName(); }
	 * 
	 * return print; }
	 * 
	 * @RequestMapping("/getCreatedCompetences")
	 * 
	 * @ResponseBody public String getCreatedCompetences(long user){
	 * 
	 * Set<Competence> myCompetences = null;
	 * 
	 * try { myCompetences = userDAO.findOne(user).getCreated_competences(); }
	 * catch (Exception ex) { return "Error: " + ex.toString(); }
	 * 
	 * String print = "";
	 * 
	 * for (Competence s : myCompetences) { print += s.getName(); }
	 * 
	 * return print; }
	 */

}
