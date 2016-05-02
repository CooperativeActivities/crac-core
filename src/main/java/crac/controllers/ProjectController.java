package crac.controllers;

import java.io.IOException;
import java.util.List;

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

import crac.daos.TaskDAO;
import crac.daos.AttachmentDAO;
import crac.daos.CommentDAO;
import crac.daos.CompetenceDAO;
import crac.daos.CracUserDAO;
import crac.daos.ProjectDAO;
import crac.models.Attachment;
import crac.models.Comment;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Project;
import crac.models.Task;

/**
 * REST controller for managing projects.
 */

@RestController
@RequestMapping("/project")
public class ProjectController {

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private AttachmentDAO attachmentDAO;

	@Autowired
	private CommentDAO commentDAO;
	
	@Autowired
	private ProjectDAO projectDAO;

	/**
	 * GET / or blank -> get all tasks.
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() throws JsonProcessingException {
		Iterable<Project> projectList = projectDAO.findAll();
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(projectList));
	}

	/**
	 * GET /{project_id} -> get the project with given ID.
	 */
	@RequestMapping(value = "/{project_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "project_id") Long id) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		Project myProject = projectDAO.findOne(id);
		return ResponseEntity.ok().body(mapper.writeValueAsString(myProject));
	}

	/**
	 * POST / or blank -> create a new project, creator is the logged-in user.
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> create(@RequestBody String json) throws JsonMappingException, IOException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());		
		ObjectMapper mapper = new ObjectMapper();
		Project myProject = mapper.readValue(json, Project.class);
		myProject.setCreator(myUser);
		projectDAO.save(myProject);

		return ResponseEntity.ok().body("{\"created\":\"true\"}");

	}

	/**
	 * DELETE /{project_id} -> delete the task with given ID.
	 */
	@RequestMapping(value = "/{project_id}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> destroy(@PathVariable(value = "project_id") Long id) {
		Project deleteProject = projectDAO.findOne(id);
		projectDAO.delete(deleteProject);
		return ResponseEntity.ok().body("{\"deleted\":\"true\"}");

	}

	/**
	 * PUT /{project_id} -> update the task with given ID.
	 */
	@RequestMapping(value = "/{project_id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> update(@RequestBody String json, @PathVariable(value = "project_id") Long id)
			throws JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Project updatedProject = mapper.readValue(json, Project.class);
		Project oldProject = projectDAO.findOne(id);
		oldProject = updatedProject;
		projectDAO.save(oldProject);
		return ResponseEntity.ok().body("{\"updated\":\"true\"}");

	}
	
	/**
	 * Creates a task, that is set as the child of the chosen existing project
	 * @param json
	 * @param project_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/{project_id}/addTask", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addTask(@RequestBody String json, @PathVariable(value = "project_id") Long project_id) throws JsonMappingException, IOException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());		
		ObjectMapper mapper = new ObjectMapper();
		Task myTask = mapper.readValue(json, Task.class);
		myTask.setCreator(myUser);
		myTask.setSuperProject(projectDAO.findOne(project_id));
		taskDAO.save(myTask);

		return ResponseEntity.ok().body("{\"created\":\"true\",\"project\":\""+myTask.getSuperProject().getId()+"\",\"task\":\""+myTask.getId()+"\"}");

	}
	
	@RequestMapping(value = "/{project_id}/removeTask/{task_id}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeTask(@PathVariable(value = "project_id") Long project_id, @PathVariable(value = "task_id") Long task_id) {

		Project myProject = projectDAO.findOne(project_id);
		Task myTask = taskDAO.findOne(task_id);
		myProject.getChildTasks().remove(myTask);
		taskDAO.delete(task_id);
		projectDAO.save(myProject);
		return ResponseEntity.ok().body("{\"deleted\":\"true\",\"project\":\""+myProject.getId()+"\",\"task\":\""+myTask.getId()+"\"}");

	}
	

}
