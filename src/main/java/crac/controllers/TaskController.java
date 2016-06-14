package crac.controllers;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import crac.models.Attachment;
import crac.models.Comment;
import crac.models.Competence;
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

	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private AttachmentDAO attachmentDAO;

	@Autowired
	private CommentDAO commentDAO;

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
	@PreAuthorize("hasRole('ADMIN')")
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
		
		if(updatedTask.getName() != null){
			oldTask.setName(updatedTask.getName());
		}
		
		if(updatedTask.getDescription() != null){
			oldTask.setDescription(updatedTask.getDescription());
		}

		if(updatedTask.getLocation() != null){
			oldTask.setLocation(updatedTask.getLocation());
		}

		if(updatedTask.getStartTime() != null){
			oldTask.setStartTime(updatedTask.getStartTime());
		}
		
		if(updatedTask.getEndTime() != null){
			oldTask.setEndTime(updatedTask.getEndTime());
		}

		if(updatedTask.getUrgency() > 0){
			oldTask.setUrgency(updatedTask.getUrgency());
		}
		
		if(updatedTask.getAmountOfVolunteers() > 0){
			oldTask.setAmountOfVolunteers(updatedTask.getAmountOfVolunteers());
		}
		
		if(updatedTask.getFeedback() != null){
			oldTask.setFeedback(updatedTask.getFeedback());
		}
		
		oldTask.setCompleted(updatedTask.isCompleted());
		
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
	
	/**
	 * Adds target competence to target task
	 * @param task_id
	 * @param competence_id
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = "/{task_id}/addCompetence/{competence_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addCompetence(@PathVariable(value = "task_id") Long task_id, @PathVariable(value = "competence_id") Long competence_id) {
		Task myTask = taskDAO.findOne(task_id);
		Competence myCompetence = competenceDAO.findOne(competence_id);
		myTask.getNeededCompetences().add(myCompetence);
		taskDAO.save(myTask);
		return ResponseEntity.ok().body("{\"added\":\"true\",\"task\":\""+myTask.getId()+"\",\"competence\":\""+myCompetence.getId()+"\"}");
	}
	
	/**
	 * Add feedback to target task
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/{task_id}/addFeedback", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addFeedback(@RequestBody String json, @PathVariable(value = "task_id") Long task_id) throws JsonMappingException, IOException {
		Task myTask = taskDAO.findOne(task_id);
		ObjectMapper mapper = new ObjectMapper();
		Task newTask = mapper.readValue(json, Task.class);
		String feedback = newTask.getFeedback();
		myTask.setFeedback(feedback);
		taskDAO.save(myTask);
		return ResponseEntity.ok().body("{\"added\":\"true\",\"feedback\":\""+myTask.getId()+"\",\"competence\":\""+feedback+"\"}");
	}

	/**
	 * Add an attachment to target task
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/{task_id}/addAttachment", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addAttachment(@RequestBody String json, @PathVariable(value = "task_id") Long task_id) throws JsonMappingException, IOException {
		Task myTask = taskDAO.findOne(task_id);
		ObjectMapper mapper = new ObjectMapper();
		Attachment myAttachment = mapper.readValue(json, Attachment.class);
		myTask.getAttachments().add(myAttachment);
		myAttachment.setTask(myTask);
		taskDAO.save(myTask);
		return ResponseEntity.ok().body("{\"added\":\"true\",\"feedback\":\""+myTask.getId()+"\",\"competence\":\""+myAttachment.getName()+"\"}");
	}

	/**
	 * Remove an attachment from target task
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/{task_id}/removeAttachment/{attachment_id}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeAttachment(@PathVariable(value = "task_id") Long task_id, @PathVariable(value = "attachment_id") Long attachment_id) {
		Task myTask = taskDAO.findOne(task_id);
		Attachment myAttachment = attachmentDAO.findOne(attachment_id);
		myTask.getAttachments().remove(myAttachment);
		attachmentDAO.delete(myAttachment);
		taskDAO.save(myTask);
		return ResponseEntity.ok().body("{\"removed\":\"true\",\"feedback\":\""+myTask.getId()+"\",\"competence\":\""+myAttachment.getName()+"\"}");
	}
	
	/**
	 * Finds and returns all tasks that contain a given pattern
	 * @param task_name
	 * @return ResponseEntity
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = "/getByName/{task_name}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getByName(@PathVariable(value = "task_name") String task_name) throws JsonProcessingException {
		List<Task> taskList = taskDAO.findMultipleByNameLike("%"+task_name+"%");
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(taskList));
	}
	
	/**
	 * Add a comment to target task
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/{task_id}/addComment", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addComment(@RequestBody String json, @PathVariable(value = "task_id") Long task_id) throws JsonMappingException, IOException {
		Task myTask = taskDAO.findOne(task_id);
		ObjectMapper mapper = new ObjectMapper();
		Comment myComment = mapper.readValue(json, Comment.class);
		myTask.getComments().add(myComment);
		myComment.setTask(myTask);
		taskDAO.save(myTask);
		return ResponseEntity.ok().body("{\"added\":\"true\",\"name\":\""+myComment.getName()+"\"}");
	}
	
	/**
	 * Remove a comment from target task
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/{task_id}/removeComment/{comment_id}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeComment(@PathVariable(value = "task_id") Long task_id, @PathVariable(value = "comment_id") Long comment_id) {
		Task myTask = taskDAO.findOne(task_id);
		Comment myComment = commentDAO.findOne(comment_id);
		myTask.getAttachments().remove(myComment);
		commentDAO.delete(myComment);
		taskDAO.save(myTask);
		return ResponseEntity.ok().body("{\"removed\":\"true\",\"name\":\""+myComment.getName()+"\"}");
	}
	
	/**
	 * Returns all comments of a task
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonProcessingException 
	 */
	@RequestMapping(value = "/{task_id}/getComments", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getComments(@PathVariable(value = "task_id") Long task_id) throws JsonProcessingException {
		Task myTask = taskDAO.findOne(task_id);
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(myTask.getComments()));
	}
	
	@RequestMapping(value = "/{task_id}/complete", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> completeTask(@PathVariable(value = "task_id") Long task_id) {
		
		Task completedTask = taskDAO.findOne(task_id);
		
		completedTask.setCompleted(true);
		
		taskDAO.save(completedTask);

		return ResponseEntity.ok().body("{\"task\":\""+completedTask.getId()+"\",\"completed\":\"true\"}");

	}
	
	@RequestMapping(value = "/{task_id}/notComplete", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> notCompleteTask(@PathVariable(value = "task_id") Long task_id) {
		
		Task completedTask = taskDAO.findOne(task_id);
		
		completedTask.setCompleted(false);
		
		taskDAO.save(completedTask);

		return ResponseEntity.ok().body("{\"task\":\""+completedTask.getId()+"\",\"completed\":\"false\"}");

	}

}
