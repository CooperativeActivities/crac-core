package crac.controllers;

import java.io.IOException;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CompetenceDAO;
import crac.daos.CompetenceRelationshipTypeDAO;
import crac.daos.CracUserDAO;
import crac.daos.GroupDAO;
import crac.daos.TaskDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.relationmodels.CompetenceRelationshipType;
import crac.utility.ElasticConnector;
import crac.utility.JSonResponseHelper;
import crac.utility.UpdateEntitiesHelper;

/**
 * 
 * @author David This controller contains all REAL rest-endpoints. With
 *         ADMIN-rights, all datasets can be manipulated here.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

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

	@Value("${crac.elastic.url}")
	private String url;

	@Value("${crac.elastic.port}")
	private int port;

	@Autowired
	private CompetenceRelationshipTypeDAO typeDAO;

	// USER-SECTION

	/**
	 * Creates a new user
	 * 
	 * @param json
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/user/",
			"/user" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> createUser(@RequestBody String json) throws JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		CracUser u = mapper.readValue(json, CracUser.class);

		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
		u.setPassword(bcryptEncoder.encode(u.getPassword()));

		if (userDAO.findByName(u.getName()) == null) {
			userDAO.save(u);
			return JSonResponseHelper.successFullyCreated(u);
		} else {
			return JSonResponseHelper.alreadyExists();
		}

	}

	/**
	 * Deletes target user
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/user/{user_id}",
			"/user/{user_id}/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> destroyUser(@PathVariable(value = "user_id") Long id) {
		CracUser u = userDAO.findOne(id);

		if (u != null) {
			u.getCompetenceRelationships().clear();
			userDAO.delete(u);
			return JSonResponseHelper.successFullyDeleted(u);
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	/**
	 * Updates target user
	 * 
	 * @param json
	 * @param id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/user/{user_id}",
			"/user/{user_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateUser(@RequestBody String json, @PathVariable(value = "user_id") Long id) {
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

		CracUser oldUser = userDAO.findOne(id);

		if (oldUser != null) {
			UpdateEntitiesHelper.checkAndUpdateUser(oldUser, updatedUser);
			userDAO.save(oldUser);
			return JSonResponseHelper.successFullyUpdated(oldUser);
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	// TASK-SECTION

	/**
	 * Deletes target task
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/task/{task_id}",
			"/task/{task_id}/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> destroyTask(@PathVariable(value = "task_id") Long id) {
		Task deleteTask = taskDAO.findOne(id);

		if (deleteTask != null) {
			deleteTask.getCompetenceTaskRels().clear();
			deleteTask.getUserRelationships().clear();
			taskDAO.delete(deleteTask);
			ElasticConnector<Task> eSConnTask = new ElasticConnector<Task>(url, port, "crac_core", "task");
			eSConnTask.delete("" + deleteTask.getId());
			return JSonResponseHelper.successFullyDeleted(deleteTask);
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	/**
	 * Updates target task
	 * 
	 * @param json
	 * @param id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/task/{task_id}",
			"/task/{task_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateTask(@RequestBody String json, @PathVariable(value = "task_id") Long id) {
		ObjectMapper mapper = new ObjectMapper();
		Task updatedTask;
		try {
			updatedTask = mapper.readValue(json, Task.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonMapError();
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonReadError();
		}
		Task oldTask = taskDAO.findOne(id);

		if (oldTask != null) {
			UpdateEntitiesHelper.checkAndUpdateTask(oldTask, updatedTask);
			taskDAO.save(oldTask);
			ElasticConnector<Task> eSConnTask = new ElasticConnector<Task>(url, port, "crac_core", "task");
			eSConnTask.indexOrUpdate("" + oldTask.getId(), oldTask);
			return JSonResponseHelper.successFullyUpdated(oldTask);
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	// COMPETENCE-SECTION

	/**
	 * Creates a new competence
	 * 
	 * @param json
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/competence/",
			"/competence" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> createCompetence(@RequestBody String json) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		ObjectMapper mapper = new ObjectMapper();
		Competence myCompetence;
		try {
			myCompetence = mapper.readValue(json, Competence.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonMapError();
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonReadError();
		}
		myCompetence.setCreator(myUser);
		competenceDAO.save(myCompetence);
		return JSonResponseHelper.successFullyCreated(myCompetence);

	}

	/**
	 * Deletes target competence
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/competence/{competence_id}",
			"/competence/{competence_id}/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> destroyCompetence(@PathVariable(value = "competence_id") Long id) {
		Competence deleteCompetence = competenceDAO.findOne(id);

		if (deleteCompetence != null) {
			competenceDAO.delete(deleteCompetence);
			return JSonResponseHelper.successFullyDeleted(deleteCompetence);

		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	/**
	 * Updates target competence
	 * 
	 * @param json
	 * @param id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/competence/{competence_id}",
			"/competence/{competence_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateCompetence(@RequestBody String json,
			@PathVariable(value = "competence_id") Long id) {
		ObjectMapper mapper = new ObjectMapper();
		Competence updatedCompetence;
		try {
			updatedCompetence = mapper.readValue(json, Competence.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonMapError();
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonReadError();
		}

		Competence oldCompetence = competenceDAO.findOne(id);

		if (oldCompetence != null) {
			UpdateEntitiesHelper.checkAndUpdateCompetence(oldCompetence, updatedCompetence);
			competenceDAO.save(oldCompetence);
			return JSonResponseHelper.successFullyUpdated(oldCompetence);
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	/**
	 * Add a new relationship-type for competences
	 * 
	 * @param json
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "competence/type",
			"competence/type/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addCompRelType(@RequestBody String json) {
		ObjectMapper mapper = new ObjectMapper();
		CompetenceRelationshipType t;
		try {
			t = mapper.readValue(json, CompetenceRelationshipType.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonMapError();
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonReadError();
		}
		typeDAO.save(t);
		return JSonResponseHelper.successFullyCreated(t);
	}

	/**
	 * Deletes target relationship-type for competences
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "competence/type/{type_id}",
			"competence/type/{type_id}/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> deleteCompRelType(@PathVariable(value = "type_id") Long id) {

		CompetenceRelationshipType type = typeDAO.findOne(id);

		if (type != null) {
			typeDAO.delete(type);
			return JSonResponseHelper.successFullyDeleted(type);

		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	/**
	 * Updates target relationship-type for competences
	 * 
	 * @param json
	 * @param id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "competence/type/{type_id}",
			"/competence/type/{type_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateType(@RequestBody String json, @PathVariable(value = "type_id") Long id) {
		ObjectMapper mapper = new ObjectMapper();
		CompetenceRelationshipType updatedCrt;
		try {
			updatedCrt = mapper.readValue(json, CompetenceRelationshipType.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonMapError();
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonReadError();
		}

		CompetenceRelationshipType oldupdatedCrt = typeDAO.findOne(id);

		if (oldupdatedCrt != null) {
			UpdateEntitiesHelper.checkAndUpdateCompetenceRelType(oldupdatedCrt, updatedCrt);
			typeDAO.save(oldupdatedCrt);
			return JSonResponseHelper.successFullyUpdated(oldupdatedCrt);
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/refreshESTasks",
			"/refreshESTasks/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> refreshESTasks() {

		ElasticConnector<Task> eSConnTask = new ElasticConnector<Task>(url, port, "crac_core", "task");

		DeleteIndexResponse deleted = eSConnTask.deleteIndex();
		if (deleted.isAcknowledged()) {
			return JSonResponseHelper.indexSuccessFullyDeleted(url);
		} else {
			return JSonResponseHelper.idNotFound();
		}
	}

}
