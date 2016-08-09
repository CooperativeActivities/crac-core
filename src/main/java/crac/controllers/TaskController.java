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
import crac.daos.UserTaskRelDAO;
import crac.elastic.ElasticConnector;
import crac.elastic.ElasticTask;
import crac.enums.Role;
import crac.enums.TaskParticipationType;
import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.daos.AttachmentDAO;
import crac.daos.CommentDAO;
import crac.daos.CompetenceDAO;
import crac.daos.CracUserDAO;
import crac.models.Attachment;
import crac.models.Comment;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.notifier.NotificationHelper;
import crac.relationmodels.UserTaskRel;
import crac.utility.JSonResponseHelper;
import crac.utility.SearchTransformer;

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
	
	@Autowired
	private UserTaskRelDAO userTaskRelDAO;

	
	
	private ElasticConnector<ElasticTask> ESConnTask = new ElasticConnector<ElasticTask>("localhost", 9300, "crac_core", "elastic_task");
	private SearchTransformer ST = new SearchTransformer();

	/**
	 * Returns all tasks
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() {
		Iterable<Task> taskList = taskDAO.findAll();
		ObjectMapper mapper = new ObjectMapper();
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(taskList));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
	}

	/**
	 * Returns target task with given id
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "task_id") Long id) {

		ObjectMapper mapper = new ObjectMapper();
		Task task = taskDAO.findOne(id);
		
		if(task != null){
			try {
				return ResponseEntity.ok().body(mapper.writeValueAsString(task));
			} catch (JsonProcessingException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.jsonWriteError();
			}
		}else{
			return JSonResponseHelper.idNotFound();
		}
	}

	/**
	 * Creates a new task
	 * @param json
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> create(@RequestBody String json) throws JsonMappingException, IOException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());
		ObjectMapper mapper = new ObjectMapper();
		Task task;
		try {
			task = mapper.readValue(json, Task.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonMapError();
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonReadError();
		}
		task.setCreator(user);
		taskDAO.save(task);
		//ESConnTask.indexOrUpdate("" + task.getId(), ST.transformTask(task));

		return JSonResponseHelper.successFullyCreated(task);

	}
	
	/**
	 * Creates a task, that is set as the child of the chosen existing task
	 * @param json
	 * @param supertask_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = "/{supertask_id}/extend", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> extendTask(@RequestBody String json, @PathVariable(value = "supertask_id") Long supertask_id) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ObjectMapper mapper = new ObjectMapper();
		CracUser user = userDAO.findByName(userDetails.getUsername());	
		
		Task superTask = taskDAO.findOne(supertask_id);
		
		if(superTask != null){
			Task task;
			try {
				task = mapper.readValue(json, Task.class);
			} catch (JsonMappingException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.jsonMapError();
			} catch (IOException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.jsonReadError();
			}
			task.setCreator(user);
			task.setSuperTask(superTask);
			task.setUserRelationships(userTaskRelDAO.findByParticipationTypeAndTask(TaskParticipationType.LEADING, superTask));
			taskDAO.save(task);

			return JSonResponseHelper.successFullyCreated(task);

		}else{
			return JSonResponseHelper.idNotFound();
		}
		
	}
	
	/**
	 * Adds target competence to target task
	 * @param task_id
	 * @param competence_id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/{task_id}/competence/{competence_id}/require", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> requireCompetence(@PathVariable(value = "task_id") Long task_id, @PathVariable(value = "competence_id") Long competence_id) {
		
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());	
		
		Task task = taskDAO.findOne(task_id);
		Competence competence = competenceDAO.findOne(competence_id);
		if(task  != null && competence != null){
			if(user.getCreatedTasks().contains(task) && task.getTaskState() == TaskState.NOT_PUBLISHED || user.getRole() == Role.ADMIN && task.getTaskState() == TaskState.NOT_PUBLISHED){
				task.getNeededCompetences().add(competence);
				taskDAO.save(task);
				return JSonResponseHelper.successFullyAssigned(competence);
			}else{
				return JSonResponseHelper.ressourceUnchangeable();
			}
		}else{
			return JSonResponseHelper.idNotFound();
		}
	}
	
	/**
	 * Adds target competence to target task
	 * @param task_id
	 * @param competence_id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/{task_id}/competence/{competence_id}/remove", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeCompetence(@PathVariable(value = "task_id") Long task_id, @PathVariable(value = "competence_id") Long competence_id) {
		
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());	

		Task task = taskDAO.findOne(task_id);
		Competence competence = competenceDAO.findOne(competence_id);
		if(task  != null && competence != null){
			if(user.getCreatedTasks().contains(task) && task.getTaskState() == TaskState.NOT_PUBLISHED || user.getRole() == Role.ADMIN && task.getTaskState() == TaskState.NOT_PUBLISHED){
				task.getNeededCompetences().remove(competence);
				taskDAO.save(task);
				return JSonResponseHelper.successFullyDeleted(competence);
			}else{
				return JSonResponseHelper.ressourceUnchangeable();
			}
		}else{
			return JSonResponseHelper.idNotFound();
		}
	}

	
	/**
	 * Finds and returns all tasks that contain a given pattern
	 * @param task_name
	 * @return ResponseEntity
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = "/search/{task_name}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getByName(@PathVariable(value = "task_name") String task_name) {
		List<Task> taskList = taskDAO.findMultipleByNameLike("%"+task_name+"%");
		ObjectMapper mapper = new ObjectMapper();
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(taskList));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
	}
	
	/**
	 * Adds target task to the open-tasks of the logged-in user or changes it's state
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/{state_name}", "/{task_id}/{state_name}/" }, method = RequestMethod.GET, produces = "application/json")
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
	 * Change the state of target task
	 * @param task_id
	 * @param stateName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/state/{state_name}", "/{task_id}/state/{state_name}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> changeTaskState(@PathVariable(value = "task_id") Long task_id, @PathVariable(value = "state_name") String stateName) {		
		
		
		Task task = taskDAO.findOne(task_id);		
		if(task != null){
			
			TaskState state = TaskState.NOT_PUBLISHED;
			
			if(stateName.equals("publish") && allowPublish(task)){
				state = TaskState.PUBLISHED;
			}else if(stateName.equals("start") && allowStart(task)){
				state = TaskState.STARTED;
			}else if(stateName.equals("complete")){
				state = TaskState.COMPLETED;
			}else{
				return JSonResponseHelper.stateNotAvailable(stateName);
			}

			task.setTaskState(state);
			taskDAO.save(task);
			return JSonResponseHelper.successTaskStateChanged(task, state);
		}else{
			return JSonResponseHelper.idNotFound();
		}
	}
	
	/**
	 * Declare someone the leader of a task as creator
	 * @param userId
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/nominateLeader/{user_id}", "/{task_id}/nominateLeader/{user_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> nominateLeader(@PathVariable(value = "user_id") Long userId, @PathVariable(value = "task_id") Long taskId) {
		
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser loggedU = userDAO.findByName(userDetails.getUsername());
		CracUser targetU = userDAO.findOne(userId);
		Task task = taskDAO.findOne(taskId);
		System.out.println(taskId);
				
		if(targetU != null && task != null){

			if (loggedU.getRole() == Role.ADMIN || loggedU.getCreatedTasks().contains(task)) {				
				NotificationHelper.createLeadNomination(loggedU, targetU, task);		
				return JSonResponseHelper.successfullySent();
			} else {
				return JSonResponseHelper.ressourceUnchangeable();
			}
		}else{
			return JSonResponseHelper.idNotFound();
		}
		
	}

	/**
	 * Returns the values for the enum taskParticipationType
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/taskParticipationTypes", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> taskParticipationTypes() {
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(TaskParticipationType.values()));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
	}

	/**
	 * Returns the values for the enum taskStates
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/taskStates", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> taskStates() {
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(TaskState.values()));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
	}
	
	/**
	 * Returns the values for the enum taskType
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/taskTypes", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> taskTypes() {
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(TaskType.values()));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
	}
	
	/**
	 * Returns all tasks, that are supertasks
	 * @return
	 */
	@RequestMapping(value = { "/parents", "/parents/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getParents() {
		
		ObjectMapper mapper = new ObjectMapper();
		List<Task> tasks = taskDAO.findBySuperTaskNull();

		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(tasks));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
	}
	
	/**
	 * Looks up, if the task is allowed to be published
	 * @param t
	 * @return boolean
	 */
	private boolean allowPublish(Task t){
		
		if(t.getAmountOfVolunteers() > 0 && t.getDescription() != null && t.getStartTime() != null && 
				t.getEndTime() != null && !t.getNeededCompetences().isEmpty() && t.getLocation() != null){
			return true;
		}return false;
		
	}
	
	/**
	 * Looks up, if the task is allowed to be started
	 * @param t
	 * @return boolean
	 */
	private boolean allowStart(Task t){
		if(t.getTaskType() == TaskType.SEQUENTIAL){
			return previousTaskDone(t) && childrenDone(t);
		}else{
			return childrenDone(t);
		}

	}
	
	/**
	 * Looks up, if the previous task is done, if there is one
	 * @param t
	 * @return boolean
	 */
	private boolean previousTaskDone(Task t){
		if(t.getTaskType() == TaskType.SEQUENTIAL){
			if(t.getPreviousTask().getTaskState() == TaskState.COMPLETED){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Looks up, if the child-tasks is are, if there are some
	 * @param t
	 * @return boolean
	 */
	private boolean childrenDone(Task t){
		boolean childrenDone = true;
		
		for(Task ct : t.getChildTasks()){
			if(ct.getTaskState() != TaskState.COMPLETED){
				childrenDone = false;
			}
		}
		
		return childrenDone;
	}
	
	//KEEP OR DELETE METHODS
	
	/**
	 * Add feedback to target task
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*
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
	*/

	/**
	 * Add an attachment to target task
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*
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
	*/

	/**
	 * Remove an attachment from target task
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*
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
	*/

	
	/**
	 * Add a comment to target task
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*
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
	*/
	
	/**
	 * Remove a comment from target task
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*
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
	*/
	
	/**
	 * Returns all comments of a task
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonProcessingException 
	 */
	/*
	@RequestMapping(value = "/{task_id}/comments", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getComments(@PathVariable(value = "task_id") Long task_id) throws JsonProcessingException {
		Task myTask = taskDAO.findOne(task_id);
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(myTask.getComments()));
	}
	*/

	
}
