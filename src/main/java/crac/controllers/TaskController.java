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

import crac.daos.TaskDAO;
import crac.daos.CracUserDAO;
import crac.models.CracUser;
import crac.models.Task;

/**
 * REST controller for managing tasks.
 */

@RestController
@RequestMapping("/task")
public class TaskController {

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private CracUserDAO userDAO;

	/**
	 * GET / or blank -> get all tasks.
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() throws JsonProcessingException {
		Iterable<Task> taskList = taskDAO.findAll();
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(taskList));
	}

	/**
	 * GET /{task_id} -> get the task with given ID.
	 */
	@RequestMapping(value = "/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "task_id") Long id) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		Task myTask = taskDAO.findOne(id);
		return ResponseEntity.ok().body(mapper.writeValueAsString(myTask));
	}

	/**
	 * POST / or blank -> create a new task, creator is the logged-in user.
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> create(@RequestBody String json) throws JsonMappingException, IOException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());		
		ObjectMapper mapper = new ObjectMapper();
		Task myTask = mapper.readValue(json, Task.class);
		myTask.setCreator(myUser);
		taskDAO.save(myTask);

		return ResponseEntity.ok().body("{\"created\":\"true\"}");

	}

	/**
	 * DELETE /{task_id} -> delete the task with given ID.
	 */
	@RequestMapping(value = "/{task_id}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> destroy(@PathVariable(value = "task_id") Long id) {
		Task deleteTask = taskDAO.findOne(id);
		taskDAO.delete(deleteTask);
		return ResponseEntity.ok().body("{\"deleted\":\"true\"}");

	}

	/**
	 * PUT /{task_id} -> update the task with given ID.
	 */
	@RequestMapping(value = "/{task_id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> update(@RequestBody String json, @PathVariable(value = "task_id") Long id)
			throws JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Task updatedTask = mapper.readValue(json, Task.class);
		Task oldTask = taskDAO.findOne(id);

		if (updatedTask.getName() != null) {
			oldTask.setName(updatedTask.getName());
		}
		if (updatedTask.getDescription() != null) {
			oldTask.setDescription(updatedTask.getDescription());
		}

		taskDAO.save(oldTask);
		return ResponseEntity.ok().body("{\"updated\":\"true\"}");

	}
	
	/**
	 * Creates a task, that is set as the child of the chosen existing task
	 * @param json
	 * @param supertask_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/{supertask_id}/addSubtask", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addSubtask(@RequestBody String json, @PathVariable(value = "supertask_id") Long supertask_id) throws JsonMappingException, IOException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());		
		ObjectMapper mapper = new ObjectMapper();
		Task myTask = mapper.readValue(json, Task.class);
		myTask.setCreator(myUser);
		myTask.setSuperTask(taskDAO.findOne(supertask_id));
		taskDAO.save(myTask);

		return ResponseEntity.ok().body("{\"created\":\"true\",\"parent_task\":\""+myTask.getSuperTask().getId()+"\",\"child_task\":\""+myTask.getId()+"\"}");

	}


}
