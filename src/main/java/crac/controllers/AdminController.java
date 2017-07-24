package crac.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.components.utility.DataAccess;
import crac.components.utility.JSONResponseHelper;
import crac.components.utility.UpdateEntitiesHelper;
import crac.enums.ErrorCause;
import crac.enums.TaskParticipationType;
import crac.enums.TaskType;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CompetenceRelationshipDAO;
import crac.models.db.daos.CompetenceRelationshipTypeDAO;
import crac.models.db.daos.CompetenceTaskRelDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.GroupDAO;
import crac.models.db.daos.RoleDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Role;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceRelationshipType;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserTaskRel;
import crac.models.komet.daos.TxExabiscompetencesDescriptorDAO;
import crac.models.komet.daos.TxExabiscompetencesDescriptorsDescriptorMmDAO;

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
	private RoleDAO roleDAO;

	@Autowired
	private GroupDAO groupDAO;

	@Autowired
	private CompetenceTaskRelDAO competenceTaskRelDAO;

	@Autowired
	private CompetenceRelationshipDAO competenceRelationshipDAO;

	@Autowired
	private UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	private UserTaskRelDAO userTaskRelDAO;

	@Autowired
	private CompetenceRelationshipTypeDAO competenceRelationshipTypeDAO;

	@Autowired
	private TxExabiscompetencesDescriptorDAO txExabiscompetencesDescriptorDAO;

	@Autowired
	private TxExabiscompetencesDescriptorsDescriptorMmDAO txExabiscompetencesDescriptorsDescriptorMmDAO;

	@Value("${crac.elastic.url}")
	private String url;

	@Value("${crac.elastic.port}")
	private int port;

	@Autowired
	private CompetenceRelationshipTypeDAO typeDAO;

	// ROLE-SECTION

	/**
	 * Adds a role to target User
	 * 
	 * @param roleId
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/user/{user_id}/role/{role_id}/add",
			"/user/{user_id}/role/{role_id}/add/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addRole(@PathVariable(value = "role_id") Long roleId,
			@PathVariable(value = "user_id") Long userId) {

		CracUser user = userDAO.findOne(userId);
		Role role = roleDAO.findOne(roleId);

		if (role != null) {
			if (user.getRoles().contains(role)) {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ALREADY_ASSIGNED);
			} else {
				user.getRoles().add(role);
				userDAO.save(user);
				return JSONResponseHelper.successfullyAssigned(role);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Removes a role from target user
	 * 
	 * @param roleId
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/user/{user_id}/role/{role_id}/remove",
			"/user/{user_id}/role/{role_id}/remove/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeRole(@PathVariable(value = "role_id") Long roleId,
			@PathVariable(value = "user_id") Long userId) {
		CracUser user = userDAO.findOne(userId);
		Role role = roleDAO.findOne(roleId);

		if (user.getRoles().contains(role)) {
			user.getRoles().remove(role);
			userDAO.save(user);
			return JSONResponseHelper.successfullyDeleted(role);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}
	}

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
			return JSONResponseHelper.successfullyCreated(u);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.DATASETS_ALREADY_EXISTS);
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
			ResponseEntity<String> v = JSONResponseHelper.successfullyDeleted(u);
			u.getCompetenceRelationships().clear();
			userDAO.delete(u);
			return v;
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		CracUser oldUser = userDAO.findOne(id);

		if (oldUser != null) {
			UpdateEntitiesHelper.checkAndUpdateUser(oldUser, updatedUser);
			userDAO.save(oldUser);
			return JSONResponseHelper.successfullyUpdated(oldUser);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	// TASK-SECTION

	/**
	 * Deletes target task
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/task/{task_id}",
			"/task/{task_id}/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> destroyTask(@PathVariable(value = "task_id") Long id) {
		Task deleteTask = taskDAO.findOne(id);
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		if (user.hasTaskPermissions(deleteTask)) {
			if (deleteTask != null) {
				DataAccess.getConnector(Task.class).delete("" + deleteTask.getId());
				ResponseEntity<String> v = JSONResponseHelper.successfullyDeleted(deleteTask);
				taskDAO.delete(deleteTask);
				return v;
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
		}

	}

	/**
	 * Creates a new task
	 * 
	 * @param json
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@PreAuthorize("hasRole('ADMIN') OR hasRole('EDITOR')")
	@RequestMapping(value = { "/task",
			"/task/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> create(@RequestBody String json) throws JsonMappingException, IOException {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		ObjectMapper mapper = new ObjectMapper();
		Task task;
		try {
			task = mapper.readValue(json, Task.class);
		} catch (JsonMappingException e) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		if (task.getTaskType() == TaskType.SHIFT) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.CANNOT_CREATE);
		}

		task.setCreator(user);
		task.updateReadyStatus();

		try {
			taskDAO.save(task);
		} catch (Exception e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		UserTaskRel newRel = new UserTaskRel();
		newRel.setParticipationType(TaskParticipationType.LEADING);
		newRel.setTask(task);
		newRel.setUser(user);

		userTaskRelDAO.save(newRel);

		DataAccess.getConnector(Task.class).indexOrUpdate("" + task.getId(), task);

		return JSONResponseHelper.successfullyCreated(task);

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
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		ObjectMapper mapper = new ObjectMapper();
		Competence myCompetence;
		try {
			myCompetence = mapper.readValue(json, Competence.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}
		myCompetence.setCreator(user);
		competenceDAO.save(myCompetence);
		return JSONResponseHelper.successfullyCreated(myCompetence);

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
			return JSONResponseHelper.successfullyDeleted(deleteCompetence);

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		Competence oldCompetence = competenceDAO.findOne(id);

		if (oldCompetence != null) {
			UpdateEntitiesHelper.checkAndUpdateCompetence(oldCompetence, updatedCompetence);
			competenceDAO.save(oldCompetence);
			return JSONResponseHelper.successfullyUpdated(oldCompetence);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}
		typeDAO.save(t);
		return JSONResponseHelper.successfullyCreated(t);
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
			ResponseEntity<String> v = JSONResponseHelper.successfullyDeleted(type);
			typeDAO.delete(type);
			return v;

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		CompetenceRelationshipType oldupdatedCrt = typeDAO.findOne(id);

		if (oldupdatedCrt != null) {
			UpdateEntitiesHelper.checkAndUpdateCompetenceRelType(oldupdatedCrt, updatedCrt);
			typeDAO.save(oldupdatedCrt);
			return JSONResponseHelper.successfullyUpdated(oldupdatedCrt);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	// ROLE-SECTION

	/**
	 * Creates a new role
	 * 
	 * @param json
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/role/",
			"/role" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> createRole(@RequestBody String json) throws JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Role r = mapper.readValue(json, Role.class);

		if (roleDAO.findByName(r.getName()) == null) {
			roleDAO.save(r);
			return JSONResponseHelper.successfullyCreated(r);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.DATASETS_ALREADY_EXISTS);
		}

	}

	/**
	 * Deletes target role
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/role/{role_id}",
			"/role/{role_id}/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> destroyRole(@PathVariable(value = "role_id") Long roleId) {
		Role r = roleDAO.findOne(roleId);

		if (r != null) {
			ResponseEntity<String> v = JSONResponseHelper.successfullyDeleted(r);
			roleDAO.delete(r);
			return v;
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Updates target role
	 * 
	 * @param json
	 * @param id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/role/{role_id}",
			"/role/{role_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateRole(@RequestBody String json, @PathVariable(value = "role_id") Long roleId) {
		ObjectMapper mapper = new ObjectMapper();
		Role updatedRole;
		try {
			updatedRole = mapper.readValue(json, Role.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		Role oldRole = roleDAO.findOne(roleId);

		if (oldRole != null) {
			oldRole.setName(updatedRole.getName());
			roleDAO.save(oldRole);
			return JSONResponseHelper.successfullyUpdated(oldRole);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

}
