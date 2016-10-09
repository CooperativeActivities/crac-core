package crac.controllers;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.jena.atlas.json.JSON;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import crac.enums.Role;
import crac.enums.TaskParticipationType;
import crac.enums.TaskRepetitionState;
import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.daos.AttachmentDAO;
import crac.daos.CommentDAO;
import crac.daos.CompetenceDAO;
import crac.daos.CompetenceTaskRelDAO;
import crac.daos.CracUserDAO;
import crac.daos.RepetitionDateDAO;
import crac.models.Attachment;
import crac.models.Comment;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.notifier.NotificationHelper;
import crac.relationmodels.CompetenceTaskRel;
import crac.relationmodels.UserTaskRel;
import crac.utility.ElasticConnector;
import crac.utility.JSonResponseHelper;
import crac.utility.SearchHelper;
import crac.utilityModels.EvaluatedTask;
import crac.utilityModels.RepetitionDate;
import crac.utilityModels.SimpleQuery;

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
	private CompetenceTaskRelDAO competenceTaskRelDAO;

	@Autowired
	private UserTaskRelDAO userTaskRelDAO;

	@Autowired
	private RepetitionDateDAO repetitionDateDAO;

	@Autowired
	private SearchHelper searchHelper;

	@Value("${crac.elastic.bindEStoSearch}")
	private boolean bindES;

	@Value("${crac.elastic.url}")
	private String url;

	@Value("${crac.elastic.port}")
	private int port;

	/**
	 * Returns all tasks
	 * 
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
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "task_id") Long id) {

		ObjectMapper mapper = new ObjectMapper();
		Task task = taskDAO.findOne(id);

		if (task != null) {
			try {
				return ResponseEntity.ok().body(mapper.writeValueAsString(task));
			} catch (JsonProcessingException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.jsonWriteError();
			}
		} else {
			return JSonResponseHelper.idNotFound();
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
	@RequestMapping(value = { "/",
			"" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
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

		ElasticConnector<Task> eSConnTask = new ElasticConnector<Task>(url, port, "crac_core", "task");

		eSConnTask.indexOrUpdate("" + task.getId(), task);

		return JSonResponseHelper.successFullyCreated(task);

	}

	/**
	 * Creates a task, that is set as the child of the chosen existing task
	 * 
	 * @param json
	 * @param supertask_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/{supertask_id}/extend",
			"/{supertask_id}/extend/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> extendTask(@RequestBody String json,
			@PathVariable(value = "supertask_id") Long supertask_id) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ObjectMapper mapper = new ObjectMapper();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		Task superTask = taskDAO.findOne(supertask_id);

		if (superTask != null) {
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
			task.setUserRelationships(
					userTaskRelDAO.findByParticipationTypeAndTask(TaskParticipationType.LEADING, superTask));
			taskDAO.save(task);

			return JSonResponseHelper.successFullyCreated(task);

		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	/**
	 * Copy target task
	 * 
	 * @param task_id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/copy",
			"/{task_id}/copy/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> copyTask(@PathVariable(value = "task_id") Long task_id) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		Task original = taskDAO.findOne(task_id);

		if (original.getSuperTask() != null) {
			return JSonResponseHelper.actionNotPossible("child-tasks can't be copied");
		}

		Task copy = original.copy(null);
		copy.setCreator(user);

		taskDAO.save(copy);

		return JSonResponseHelper.successFullyCreated(copy);

	}

	/**
	 * Adds target competence to target task, it is mandatory to add the proficiency and importanceLvl
	 * 
	 * @param task_id
	 * @param competence_id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/competence/{competence_id}/require/{proficiency}/{importance}",
			"/{task_id}/competence/{competence_id}/require/{proficiency}/{importance}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> requireCompetence(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "competence_id") Long competence_id,
			@PathVariable(value = "proficiency") int proficiency, @PathVariable(value = "importance") int importance) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		Task task = taskDAO.findOne(task_id);
		Competence competence = competenceDAO.findOne(competence_id);
		if (task != null && competence != null) {
			if (user.getCreatedTasks().contains(task) && task.getTaskState() == TaskState.NOT_PUBLISHED
					|| user.getRole() == Role.ADMIN && task.getTaskState() == TaskState.NOT_PUBLISHED) {
				competenceTaskRelDAO.save(new CompetenceTaskRel(competence, task, proficiency, importance));
				return JSonResponseHelper.successFullyAssigned(competence);
			} else {
				return JSonResponseHelper.ressourceUnchangeable();
			}
		} else {
			return JSonResponseHelper.idNotFound();
		}
	}

	/**
	 * Adds target competence to target task
	 * 
	 * @param task_id
	 * @param competence_id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/competence/{competence_id}/remove", "/{task_id}/competence/{competence_id}/remove/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeCompetence(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "competence_id") Long competence_id) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		Task task = taskDAO.findOne(task_id);
		Competence competence = competenceDAO.findOne(competence_id);
		CompetenceTaskRel ctr = competenceTaskRelDAO.findByTaskAndCompetence(task, competence);
		if (task != null && competence != null && ctr != null) {
			if (user.getCreatedTasks().contains(task) && task.getTaskState() == TaskState.NOT_PUBLISHED
					|| user.getRole() == Role.ADMIN && task.getTaskState() == TaskState.NOT_PUBLISHED) {
				competenceTaskRelDAO.delete(ctr);
				return JSonResponseHelper.successFullyDeleted(competence);
			} else {
				return JSonResponseHelper.ressourceUnchangeable();
			}
		} else {
			return JSonResponseHelper.idNotFound();
		}
	}

	/**
	 * Finds and returns all tasks that contain a given pattern
	 * 
	 * @param task_name
	 * @return ResponseEntity
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = { "/searchDirect/{task_name}", "/searchDirect/{task_name}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getByName(@PathVariable(value = "task_name") String task_name) {
		List<Task> taskList = taskDAO.findMultipleByNameLike("%" + task_name + "%");
		ObjectMapper mapper = new ObjectMapper();
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(taskList));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
	}

	/**
	 * Adds target task to the open-tasks of the logged-in user or changes it's
	 * state
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/{state_name}",
			"/{task_id}/{state_name}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> changeTaskState(@PathVariable(value = "state_name") String stateName,
			@PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		Task task = taskDAO.findOne(taskId);

		if (task != null) {
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

		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	/**
	 * Sets the TaskRepetitionState from once to periodic if possible, mandatory to add a date as json
	 * 
	 * @param task_id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/periodic/set",
			"/{task_id}/priodic/set/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> setTaskPeriodical(@RequestBody String json,
			@PathVariable(value = "task_id") Long task_id) {

		Task toSet = taskDAO.findOne(task_id);

		if (toSet != null) {
			if (toSet.getSuperTask() != null) {
				return JSonResponseHelper.actionNotPossible("child-tasks can't be set periodical");
			} else {
				RepetitionDate nrd;
				ObjectMapper mapper = new ObjectMapper();

				try {
					nrd = mapper.readValue(json, RepetitionDate.class);
				} catch (JsonMappingException e) {
					System.out.println(e.toString());
					return JSonResponseHelper.jsonMapError();
				} catch (IOException e) {
					System.out.println(e.toString());
					return JSonResponseHelper.jsonReadError();
				}
				toSet.setTaskRepetitionState(TaskRepetitionState.PERIODIC);
				RepetitionDate ord = toSet.getRepetitionDate();
				repetitionDateDAO.save(nrd);
				toSet.setRepetitionDate(nrd);
				taskDAO.save(toSet);
				repetitionDateDAO.delete(ord);
				return JSonResponseHelper.successFullAction("task set to periodical");
			}
		} else {
			return JSonResponseHelper.idNotFound();
		}
	}

	/**
	 * Sets the TaskRepetitionState from periodic to once
	 * 
	 * @param task_id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/periodic/undo",
			"/{task_id}/priodic/undo/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> undoTaskPeriodical(@PathVariable(value = "task_id") Long task_id) {

		Task toSet = taskDAO.findOne(task_id);

		if (toSet.getTaskRepetitionState() != TaskRepetitionState.PERIODIC) {
			return JSonResponseHelper.actionNotPossible("task is not periodic");
		} else {
			toSet.setTaskRepetitionState(TaskRepetitionState.ONCE);
			RepetitionDate ord = toSet.getRepetitionDate();
			toSet.setRepetitionDate(null);
			repetitionDateDAO.delete(ord);
			taskDAO.save(toSet);
			return JSonResponseHelper.successFullAction("task set to once");
		}

	}

	/**
	 * Change the state of target task, for each state different prerequisite
	 * have to be fullfilled:
	 * 
	 * NOT_PUBLISHED: Default state PUBLISHED: Only allowed when the task-fields
	 * are all filled STARTED: Only allowed when the parent task is started and
	 * if sequential, the previous task is completed COMPLETED: A task can only
	 * be completed when its children are all completed or if it has none
	 * 
	 * @param task_id
	 * @param stateName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/state/{state_name}",
			"/{task_id}/state/{state_name}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> changeTaskState(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "state_name") String stateName) {

		Task task = taskDAO.findOne(task_id);
		if (task != null) {

			TaskState state = TaskState.NOT_PUBLISHED;

			if (stateName.equals("publish")) {
				if (allowPublish(task)) {
					state = TaskState.PUBLISHED;
				} else {
					return JSonResponseHelper.actionNotPossible("can not be published, task-fields must be filled");
				}
			} else if (stateName.equals("start")) {
				if (task.getTaskState() == TaskState.PUBLISHED && allowStart(task)) {
					state = TaskState.STARTED;
				} else {
					return JSonResponseHelper.actionNotPossible(
							"can not be started, task is not published or does not fullfill the prerequisites");
				}
			} else if (stateName.equals("complete")) {
				if (task.getTaskState() == TaskState.STARTED && childrenDone(task)) {
					if (task.getTaskRepetitionState() == TaskRepetitionState.PERIODIC) {
						adjustTaskTime(task, task.getRepetitionDate());
						state = TaskState.NOT_PUBLISHED;
					} else {
						state = TaskState.COMPLETED;
					}
				} else {
					return JSonResponseHelper.actionNotPossible(
							"can not be completed, task is not started or does not fullfill the prerequisites");
				}
			} else {
				return JSonResponseHelper.stateNotAvailable(stateName);
			}

			task.setTaskState(state);
			taskDAO.save(task);
			return JSonResponseHelper.successTaskStateChanged(task, state);
		} else {
			return JSonResponseHelper.idNotFound();
		}
	}

	/*
	 * @RequestMapping(value = { "/testadd/" }, method = RequestMethod.GET,
	 * produces = "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String> testAddTime() {
	 * 
	 * Task task = taskDAO.findOne((long) 1);
	 * 
	 * //System.out.println(task.getStartTime().getTimeInMillis());
	 * 
	 * adjustTaskTime(task, task.getRepetitionDate());
	 * 
	 * taskDAO.save(task);
	 * 
	 * //System.out.println(task.getStartTime().getTimeInMillis());
	 * 
	 * return JSonResponseHelper.bootSuccess(); }
	 */

	private void adjustTaskTime(Task t, RepetitionDate repetitionTime) {
		Calendar start = t.getStartTime();
		Calendar end = t.getEndTime();

		while (start.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
			start.set(Calendar.YEAR, start.get(Calendar.YEAR) + repetitionTime.getYear());
			start.set(Calendar.MONTH, start.get(Calendar.MONTH) + repetitionTime.getMonth());
			start.set(Calendar.DAY_OF_MONTH, start.get(Calendar.DAY_OF_MONTH) + repetitionTime.getDay());
			start.set(Calendar.HOUR, start.get(Calendar.HOUR) + repetitionTime.getHour());
			start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) + repetitionTime.getMinute());

			end.set(Calendar.YEAR, end.get(Calendar.YEAR) + repetitionTime.getYear());
			end.set(Calendar.MONTH, end.get(Calendar.MONTH) + repetitionTime.getMonth());
			end.set(Calendar.DAY_OF_MONTH, end.get(Calendar.DAY_OF_MONTH) + repetitionTime.getDay());
			end.set(Calendar.HOUR, end.get(Calendar.HOUR) + repetitionTime.getHour());
			end.set(Calendar.MINUTE, end.get(Calendar.MINUTE) + repetitionTime.getMinute());
		}

		taskDAO.save(t);

		for (Task child : t.getChildTasks()) {
			adjustTaskTime(child, repetitionTime);
		}

	}

	/**
	 * Nominate someone as the leader of a task as creator
	 * 
	 * @param userId
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/nominateLeader/{user_id}",
			"/{task_id}/nominateLeader/{user_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> nominateLeader(@PathVariable(value = "user_id") Long userId,
			@PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser loggedU = userDAO.findByName(userDetails.getUsername());
		CracUser targetU = userDAO.findOne(userId);
		Task task = taskDAO.findOne(taskId);
		System.out.println(taskId);

		if (targetU != null && task != null) {

			if (loggedU.getRole() == Role.ADMIN || loggedU.getCreatedTasks().contains(task)) {
				NotificationHelper.createLeadNomination(loggedU.getId(), targetU.getId(), task.getId());
				return JSonResponseHelper.successfullySent();
			} else {
				return JSonResponseHelper.ressourceUnchangeable();
			}
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	/**
	 * Issues an invite-notification to the target-user
	 * 
	 * @param userId
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/invite/{user_id}",
			"/{task_id}/invite/{user_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> invitePerson(@PathVariable(value = "user_id") Long userId,
			@PathVariable(value = "task_id") Long taskId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser logged = userDAO.findByName(userDetails.getUsername());

		UserTaskRel utr = userTaskRelDAO.findByUserAndTask(logged, taskDAO.findOne(taskId));

		if (utr != null) {
			if (utr.getParticipationType() == TaskParticipationType.LEADING) {
				NotificationHelper.createTaskInvitation(logged.getId(), userId, taskId);
				return JSonResponseHelper.successfullySent();
			} else {
				return JSonResponseHelper.actionNotPossible("locked in user is not leading the task");
			}
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	/**
	 * Returns the values for the enum taskParticipationType
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * Fulltext-queries all tasks with Elasticsearch and returns the found ones.
	 * If bound to competence-system, compares if tasks are doable
	 * 
	 * @param json
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/queryES",
			"/queryES/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> queryES(@RequestBody String json) {

		ObjectMapper mapper = new ObjectMapper();

		SimpleQuery query;
		try {
			query = mapper.readValue(json, SimpleQuery.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonMapError();
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonReadError();
		}

		ElasticConnector<Task> eSConnTask = new ElasticConnector<Task>(url, port, "crac_core", "task");

		ArrayList<EvaluatedTask> et = eSConnTask.query(query.getText(), taskDAO);

		System.out.println("testing bindES: " + bindES);

		if (bindES) {

			UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication()
					.getPrincipal();
			CracUser user = userDAO.findByName(userDetails.getUsername());
			ArrayList<EvaluatedTask> doables = searchHelper.findMatch(user);

			for (EvaluatedTask ets : et) {
				ets.setDoable(false);
				for (EvaluatedTask etd : doables) {
					if (etd.getTask().getId() == ets.getTask().getId()) {
						ets.setDoable(true);
					}
				}
			}
		}

		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(et));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
	}

	/**
	 * Return a sorted list of elements with the best fitting users for the
	 * given task
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/findMatchingUsers/{task_id}",
			"/findMatchingUsers/{task_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> findUsers(@PathVariable(value = "task_id") Long taskId) {

		ObjectMapper mapper = new ObjectMapper();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return ResponseEntity.ok().headers(headers)
					.body(mapper.writeValueAsString(searchHelper.findMatch(taskDAO.findOne(taskId))));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}

	}

	/**
	 * Looks up, if the task is allowed to be published
	 * 
	 * @param t
	 * @return boolean
	 */
	private boolean allowPublish(Task t) {

		if (t.getAmountOfVolunteers() > 0 && !t.getDescription().equals("") && t.getStartTime() != null
				&& t.getEndTime() != null && !t.getMappedCompetences().isEmpty() && !t.getLocation().equals("")) {
			return true;
		}
		return false;

	}

	/**
	 * Looks up, if the task is allowed to be started
	 * 
	 * @param t
	 * @return boolean
	 */
	private boolean allowStart(Task t) {

		Task parent = t.getSuperTask();

		boolean startedParent = true;

		if (parent != null) {
			startedParent = t.getSuperTask().getTaskState() == TaskState.STARTED;
		}

		if (t.getTaskType() == TaskType.SEQUENTIAL) {
			return previousTaskDone(t) && startedParent;
		} else {
			return startedParent;
		}

	}

	/**
	 * Looks up, if the previous task is done, if there is one
	 * 
	 * @param t
	 * @return boolean
	 */
	private boolean previousTaskDone(Task t) {
		if (t.getTaskType() == TaskType.SEQUENTIAL) {
			if (t.getPreviousTask().getTaskState() == TaskState.COMPLETED) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Looks up, if the child-tasks (if there are some) are all completed. If
	 * there are none, returns always true
	 * 
	 * @param t
	 * @return boolean
	 */
	private boolean childrenDone(Task t) {
		boolean childrenDone = true;

		Set<Task> children = t.getChildTasks();

		if (children != null) {
			for (Task ct : t.getChildTasks()) {
				if (ct.getTaskState() != TaskState.COMPLETED) {
					childrenDone = false;
				}
			}

			return childrenDone;
			// return true;
		} else {
			return true;
		}

	}

	// KEEP OR DELETE METHODS

	/**
	 * Add feedback to target task
	 * 
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*
	 * @RequestMapping(value = "/{task_id}/addFeedback", method =
	 * RequestMethod.POST, produces = "application/json", consumes =
	 * "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String> addFeedback(@RequestBody
	 * String json, @PathVariable(value = "task_id") Long task_id) throws
	 * JsonMappingException, IOException { Task myTask =
	 * taskDAO.findOne(task_id); ObjectMapper mapper = new ObjectMapper(); Task
	 * newTask = mapper.readValue(json, Task.class); String feedback =
	 * newTask.getFeedback(); myTask.setFeedback(feedback);
	 * taskDAO.save(myTask); return
	 * ResponseEntity.ok().body("{\"added\":\"true\",\"feedback\":\""+myTask.
	 * getId()+"\",\"competence\":\""+feedback+"\"}"); }
	 */

	/**
	 * Add an attachment to target task
	 * 
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*
	 * @RequestMapping(value = "/{task_id}/addAttachment", method =
	 * RequestMethod.POST, produces = "application/json", consumes =
	 * "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String> addAttachment(@RequestBody
	 * String json, @PathVariable(value = "task_id") Long task_id) throws
	 * JsonMappingException, IOException { Task myTask =
	 * taskDAO.findOne(task_id); ObjectMapper mapper = new ObjectMapper();
	 * Attachment myAttachment = mapper.readValue(json, Attachment.class);
	 * myTask.getAttachments().add(myAttachment); myAttachment.setTask(myTask);
	 * taskDAO.save(myTask); return
	 * ResponseEntity.ok().body("{\"added\":\"true\",\"feedback\":\""+myTask.
	 * getId()+"\",\"competence\":\""+myAttachment.getName()+"\"}"); }
	 */

	/**
	 * Remove an attachment from target task
	 * 
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*
	 * @RequestMapping(value = "/{task_id}/removeAttachment/{attachment_id}",
	 * method = RequestMethod.DELETE, produces = "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String>
	 * removeAttachment(@PathVariable(value = "task_id") Long
	 * task_id, @PathVariable(value = "attachment_id") Long attachment_id) {
	 * Task myTask = taskDAO.findOne(task_id); Attachment myAttachment =
	 * attachmentDAO.findOne(attachment_id);
	 * myTask.getAttachments().remove(myAttachment);
	 * attachmentDAO.delete(myAttachment); taskDAO.save(myTask); return
	 * ResponseEntity.ok().body("{\"removed\":\"true\",\"feedback\":\""+myTask.
	 * getId()+"\",\"competence\":\""+myAttachment.getName()+"\"}"); }
	 */

	/**
	 * Add a comment to target task
	 * 
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*
	 * @RequestMapping(value = "/{task_id}/addComment", method =
	 * RequestMethod.POST, produces = "application/json", consumes =
	 * "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String> addComment(@RequestBody
	 * String json, @PathVariable(value = "task_id") Long task_id) throws
	 * JsonMappingException, IOException { Task myTask =
	 * taskDAO.findOne(task_id); ObjectMapper mapper = new ObjectMapper();
	 * Comment myComment = mapper.readValue(json, Comment.class);
	 * myTask.getComments().add(myComment); myComment.setTask(myTask);
	 * taskDAO.save(myTask); return
	 * ResponseEntity.ok().body("{\"added\":\"true\",\"name\":\""+myComment.
	 * getName()+"\"}"); }
	 */

	/**
	 * Remove a comment from target task
	 * 
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	/*
	 * @RequestMapping(value = "/{task_id}/removeComment/{comment_id}", method =
	 * RequestMethod.DELETE, produces = "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String>
	 * removeComment(@PathVariable(value = "task_id") Long
	 * task_id, @PathVariable(value = "comment_id") Long comment_id) { Task
	 * myTask = taskDAO.findOne(task_id); Comment myComment =
	 * commentDAO.findOne(comment_id);
	 * myTask.getAttachments().remove(myComment); commentDAO.delete(myComment);
	 * taskDAO.save(myTask); return
	 * ResponseEntity.ok().body("{\"removed\":\"true\",\"name\":\""+myComment.
	 * getName()+"\"}"); }
	 */

	/**
	 * Returns all comments of a task
	 * 
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonProcessingException
	 */
	/*
	 * @RequestMapping(value = "/{task_id}/comments", method =
	 * RequestMethod.GET, produces = "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String>
	 * getComments(@PathVariable(value = "task_id") Long task_id) throws
	 * JsonProcessingException { Task myTask = taskDAO.findOne(task_id);
	 * ObjectMapper mapper = new ObjectMapper(); return
	 * ResponseEntity.ok().body(mapper.writeValueAsString(myTask.getComments()))
	 * ; }
	 */

}
