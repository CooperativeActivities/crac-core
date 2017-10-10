package crac.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.enums.ErrorCode;
import crac.enums.RESTAction;
import crac.enums.TaskParticipationType;
import crac.enums.ConcreteTaskState;
import crac.enums.TaskType;
import crac.exception.InvalidActionException;
import crac.models.db.daos.AttachmentDAO;
import crac.models.db.daos.CommentDAO;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CompetenceTaskRelDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.GroupDAO;
import crac.models.db.daos.MaterialDAO;
import crac.models.db.daos.RepetitionDateDAO;
import crac.models.db.daos.RoleDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserMaterialSubscriptionDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Attachment;
import crac.models.db.entities.Comment;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracGroup;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Material;
import crac.models.db.entities.Task;
import crac.models.db.entities.Task.TaskShort;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserMaterialSubscription;
import crac.models.db.relation.UserTaskRel;
import crac.models.input.CompetenceTaskMapping;
import crac.models.input.MaterialMapping;
import crac.models.input.PostOptions;
import crac.models.output.ArchiveTask;
import crac.models.output.TaskDetails;
import crac.models.utility.NotificationConfiguration;
import crac.models.utility.PersonalizedFilters;
import crac.models.utility.TaskLookup;
import crac.module.matching.Decider;
import crac.module.matching.configuration.UserFilterParameters;
import crac.module.matching.helpers.EvaluatedTask;
import crac.module.matching.interfaces.ErrorStatus;
import crac.module.notifier.Notification;
import crac.module.notifier.factory.NotificationFactory;
import crac.module.notifier.notifications.LeadNomination;
import crac.module.notifier.notifications.TaskDoneNotification;
import crac.module.notifier.notifications.TaskInvitation;
import crac.module.storage.CompetenceStorage;
import crac.module.utility.ElasticConnector;
import crac.module.utility.JSONResponseHelper;
import crac.module.utility.CracUtility;

/**
 * REST controller for managing tasks.
 */

@RestController
@RequestMapping("/task")
public class TaskController {

	@Autowired
	private GroupDAO groupDAO;

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private CommentDAO commentDAO;

	@Autowired
	private CompetenceTaskRelDAO competenceTaskRelDAO;

	@Autowired
	private UserTaskRelDAO userTaskRelDAO;

	@Autowired
	private RepetitionDateDAO repetitionDateDAO;

	@Autowired
	private RoleDAO roleDAO;

	@Autowired
	private UserMaterialSubscriptionDAO userMaterialSubscriptionDAO;

	@Autowired
	private AttachmentDAO attachmentDAO;

	@Autowired
	private MaterialDAO materialDAO;

	@Autowired
	private Decider decider;

	@Autowired
	private ElasticConnector<Task> ect;

	@Autowired
	private NotificationFactory nf;

	@Autowired
	private CompetenceStorage cs;

	@Autowired
	private TaskLookup tl;

	/**
	 * Returns all tasks affected by the chosen filters and the
	 * elasticsearch-query
	 * 
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/",
			"" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> index(@RequestBody String json)
			throws JsonParseException, JsonMappingException, IOException {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		ObjectMapper mapper = new ObjectMapper();
		PersonalizedFilters pf;
		pf = mapper.readValue(json, PersonalizedFilters.class);

		Set<TaskShort> set = tl.lookUp(user, pf).stream().map(task -> task.toShortWithCrumbs())
				.collect(Collectors.toSet());

		return JSONResponseHelper.createResponse(set, true);
	}

	/**
	 * Starts all tasks, that fullfill the prerequisites and are ready to starts
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/updateStarted",
			"/updateStarted/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> startPossibleTasks() {

		taskDAO.findAll().forEach(t -> {
			try {
				t.setTaskState(ConcreteTaskState.STARTED, taskDAO);
			} catch (InvalidActionException e) {
				System.out.println(e.getError());
			}
		});

		HashMap<String, Object> meta = new HashMap<>();
		meta.put("tasks", "UPDATED");
		return JSONResponseHelper.createResponse(true, meta);

	}

	/**
	 * Returns target task and its relationship to the logged in user (if one
	 * exists) and updates the task if it's ready to start
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/{task_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "task_id") Long id) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task task = taskDAO.findOne(id);

		if (task != null) {
			try {
				task.setTaskState(ConcreteTaskState.STARTED, taskDAO);
			} catch (InvalidActionException e) {
				System.out.println(e.getError());
			}
			return JSONResponseHelper.createResponse(new TaskDetails(task, user, userTaskRelDAO, cs), true);
		}

		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);

	}

	/**
	 * Adds target task to the open-tasks of the logged-in user or changes it's
	 * state
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	/*
	 * @RequestMapping(value = { "/{task_id}/add/{state_name}",
	 * "/task/{task_id}/{state_name}/" }, method = RequestMethod.PUT, produces =
	 * "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String> addToUser(@PathVariable(value
	 * = "state_name") String stateName,
	 * 
	 * @PathVariable(value = "task_id") Long taskId) {
	 * 
	 * UsernamePasswordAuthenticationToken userDetails =
	 * (UsernamePasswordAuthenticationToken) SecurityContextHolder
	 * .getContext().getAuthentication(); CracUser user =
	 * userDAO.findByName(userDetails.getName());
	 * 
	 * Task task = taskDAO.findOne(taskId);
	 * 
	 * if (task != null) { TaskParticipationType state =
	 * TaskParticipationType.PARTICIPATING; if (stateName.equals("participate"))
	 * { if (task.isJoinable()) { if (!task.isFull()) { state =
	 * TaskParticipationType.PARTICIPATING; } else { return
	 * JSONResponseHelper.createResponse(false, "bad_request",
	 * ErrorCode.TASK_IS_FULL); } } else { return
	 * JSONResponseHelper.createResponse(false, "bad_request",
	 * ErrorCode.TASK_NOT_JOINABLE); } } else if (stateName.equals("follow")) {
	 * if (task.isFollowable()) { state = TaskParticipationType.FOLLOWING; }
	 * else { return JSONResponseHelper.createResponse(false, "bad_request",
	 * ErrorCode.TASK_NOT_JOINABLE); } } else { HashMap<String, Object> meta =
	 * new HashMap<>(); meta.put("state", stateName); return
	 * JSONResponseHelper.createResponse(false, "bad_request",
	 * ErrorCode.STATE_NOT_AVAILABLE, meta); }
	 * 
	 * UserTaskRel rel =
	 * userTaskRelDAO.findByUserAndTaskAndParticipationTypeNot(user, task,
	 * TaskParticipationType.LEADING);
	 * 
	 * if (rel == null) { rel = new UserTaskRel(); rel.setUser(user);
	 * rel.setTask(task); rel.setParticipationType(state);
	 * user.getTaskRelationships().add(rel); userDAO.save(user); } else {
	 * rel.setParticipationType(state); userTaskRelDAO.save(rel); }
	 * 
	 * return JSONResponseHelper.successfullyAssigned(task);
	 * 
	 * } else { return JSONResponseHelper.createResponse(false, "bad_request",
	 * ErrorCode.ID_NOT_FOUND); }
	 * 
	 * }
	 */

	@RequestMapping(value = { "/{task_id}/add/{state}",
			"/task/{task_id}/{state}/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addToUser(@PathVariable(value = "state") TaskParticipationType typeName,
			@PathVariable(value = "task_id") Long taskId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task task = taskDAO.findOne(taskId);

		if (task != null) {

			ErrorStatus es = typeName.applicable(task);

			if (!es.hasError()) {
				UserTaskRel rel = userTaskRelDAO.findByUserAndTaskAndParticipationTypeNot(user, task,
						TaskParticipationType.LEADING);

				if (rel == null) {
					rel = new UserTaskRel();
					rel.setUser(user);
					rel.setTask(task);
					rel.setParticipationType(typeName);
					user.getTaskRelationships().add(rel);
					userDAO.save(user);
				} else {
					rel.setParticipationType(typeName);
					userTaskRelDAO.save(rel);
				}

				return JSONResponseHelper.successfullyAssigned(task);

			}

			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ACTION_NOT_VALID);

		}

		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);

	}

	/**
	 * Removes target task from the open-tasks of the logged-in user
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/{task_id}/remove", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeTask(@PathVariable(value = "task_id") Long taskId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task task = taskDAO.findOne(taskId);

		if (task != null) {
			if (!task.getTaskState().inConduction()) {
				userTaskRelDAO.delete(userTaskRelDAO.findByUserAndTaskAndParticipationTypeNot(user, task,
						TaskParticipationType.LEADING));
				return JSONResponseHelper.successfullyDeleted(task);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.TASK_ALREADY_IN_PROCESS);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Returns all tasks of logged in user, divided in the
	 * TaskParticipationTypes, but only if they are not completed
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/type", "/type/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getTasksByType() {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Set<UserTaskRel> taskRels = userTaskRelDAO.findByUser(user);

		HashMap<String, Object> meta = new HashMap<>();

		Stream.of(TaskParticipationType.values()).forEach(type -> {
			meta.put(type.toString(), taskRels.stream().filter(rel -> type == rel.getParticipationType())
					.map(rel -> rel.getTask().toShort()).collect(Collectors.toSet()));
		});

		return JSONResponseHelper.createResponse(user, true, meta);

	}

	/**
	 * Returns all completed tasks of a user by participationType
	 * 
	 * @param partType
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/completed/{part_type}",
			"/completed/{part_type}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getCompletedTasks(@PathVariable(value = "part_type") TaskParticipationType partType) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		Set<UserTaskRel> trels = userTaskRelDAO.findByUserAndParticipationType(user, partType);
		Set<ArchiveTask> tcomp = new HashSet<>();
		for (UserTaskRel tr : trels) {
			if (tr.getTask().getTaskState() == ConcreteTaskState.COMPLETED) {
				tcomp.add(new ArchiveTask(tr));
			}
		}
		return JSONResponseHelper.createResponse(tcomp, true);

	}

	/**
	 * Returns all completed projects
	 * 
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/completed/all",
			"/completed/all/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getAllCompletedTasks() {
		return JSONResponseHelper.createResponse(taskDAO.findBySuperTaskNullAndTaskState(ConcreteTaskState.COMPLETED),
				true);
	}

	@RequestMapping(value = { "/{task_id}/competence/available",
			"/{task_id}/competence/available/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> availableCompetences(@PathVariable(value = "task_id") Long taskId) {

		Task task = taskDAO.findOne(taskId);

		Iterable<Competence> competenceList = competenceDAO.findAll();

		Set<CompetenceTaskRel> competenceRels = task.getMappedCompetences();

		ArrayList<Competence> found = new ArrayList<Competence>();

		if (competenceList != null) {
			for (Competence c : competenceList) {
				boolean in = false;
				for (CompetenceTaskRel ctr : competenceRels) {
					if (c.getId() == ctr.getCompetence().getId()) {
						in = true;
					}
				}
				if (!in) {
					found.add(c);
				}
			}
		}

		if (found.size() != 0) {
			return JSONResponseHelper.createResponse(found, true);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.EMPTY_DATA);
		}

	}

	/**
	 * Updates target task
	 * 
	 * @param json
	 * @param id
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/{task_id}",
			"/{task_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateTask(@RequestBody String json, @PathVariable(value = "task_id") Long id)
			throws JsonParseException, JsonMappingException, IOException {
		Task oldTask = taskDAO.findOne(id);

		if (oldTask != null) {
			if (oldTask.getTaskState() != ConcreteTaskState.COMPLETED) {
				UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
						.getContext().getAuthentication();
				CracUser user = userDAO.findByName(userDetails.getName());

				if (user.hasTaskPermissions(oldTask)) {
					ObjectMapper mapper = new ObjectMapper();
					Task updatedTask;
					updatedTask = mapper.readValue(json, Task.class);

					int c = checkAmountOfVolunteers(oldTask, updatedTask.getMaxAmountOfVolunteers(), true);

					// System.out.println("answer: " + c);

					if (c == 0) {
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.QUANTITY_TOO_HIGH);
					} else if (c > 0) {
						HashMap<String, Object> meta = new HashMap<>();
						meta.put("lowest", 1 + "");
						meta.put("highest", c + "");
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.QUANTITY_INCORRECT,
								meta);
					}
					oldTask.update(updatedTask);
					taskDAO.save(oldTask);
					oldTask.updateReadyStatus(taskDAO);
					ect.indexOrUpdate("" + oldTask.getId(), oldTask);
					return JSONResponseHelper.successfullyUpdated(oldTask);
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request",
							ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
				}
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.TASK_ALREADY_IN_PROCESS);
			}

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	private int checkAmountOfVolunteers(Task t, int amount, boolean update) {
		Task st = t.getSuperTask();
		if (st != null) {
			if (st.getMaxAmountOfVolunteers() != 0) {
				int availableNumber;
				if (update) {
					availableNumber = st.possibleNumberOfVolunteers() + t.getMaxAmountOfVolunteers();
				} else {
					availableNumber = st.possibleNumberOfVolunteers();
				}
				System.out.println("amount: " + t.getMaxAmountOfVolunteers());
				System.out.println("available: " + availableNumber);
				if (amount > availableNumber) {
					return availableNumber;
				}
			}
		}
		return -1;
	}

	/**
	 * Creates a task, that is set as the child of the chosen existing task
	 * 
	 * @param json
	 * @param supertask_id
	 * @return ResponseEntity
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/{supertask_id}/extend",
			"/{supertask_id}/extend/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> extendTask(@RequestBody String json,
			@PathVariable(value = "supertask_id") Long supertask_id)
			throws JsonParseException, JsonMappingException, IOException {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task st = taskDAO.findOne(supertask_id);

		if (st != null) {
			if (user.hasTaskPermissions(st)) {

				if (st.getTaskState().isExtendable()) {
					return persistTask(st, user, json);
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.TASK_NOT_EXTENDABLE);
				}

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
			}

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Updates the value of an task with an open amount of volunteers, based on
	 * the amount of volunteers on their child-tasks
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/updateAmountOfVolunteers",
			"/{task_id}/updateAmountOfVolunteers/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateAmountOfVolunteers(@PathVariable(value = "task_id") Long taskId) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task t = taskDAO.findOne(taskId);

		if (t != null) {
			if (user.hasTaskPermissions(t)) {

				System.out.println("sfd " + t.getMaxAmountOfVolunteers());
				if (t.getMaxAmountOfVolunteers() == 0) {

					int val = 0;

					for (Task ct : t.getChildTasks()) {
						if (ct.getMaxAmountOfVolunteers() > 0) {
							val += ct.getMaxAmountOfVolunteers();
						} else {
							return JSONResponseHelper.createResponse(false, "bad_request",
									ErrorCode.TASK_HAS_OPEN_AMOUNT);
						}
					}

					t.setMaxAmountOfVolunteers(val);
					taskDAO.save(t);
					return JSONResponseHelper.successfullyUpdated(t);

				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ALREADY_FILLED);
				}

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	private ResponseEntity<String> persistTask(Task st, CracUser u, String json)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		Task t;
		t = mapper.readValue(json, Task.class);

		t.setSuperTask(st);

		int c = checkAmountOfVolunteers(t, t.getMaxAmountOfVolunteers(), false);

		if (st.getTaskType() == TaskType.ORGANISATIONAL && t.getTaskType() == TaskType.SHIFT) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ORGANISATIONAL_EXTENDS_SHIFT);
		}

		if (st.getTaskType() == TaskType.WORKABLE && t.getTaskType() == TaskType.ORGANISATIONAL) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.WORKABLE_EXTENDS_ORGANISATIONAL);
		}

		if (st.getTaskType() == TaskType.WORKABLE && t.getTaskType() == TaskType.WORKABLE) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.WORKABLE_EXTENDS_WORKABLE);
		}

		if (st.getTaskType() == TaskType.SHIFT) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.SHIFT_EXTENDS);
		}

		if (c == 0) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.QUANTITY_TOO_HIGH);
		} else if (c > 0) {
			HashMap<String, Object> meta = new HashMap<>();
			meta.put("lowest", 1 + "");
			meta.put("highest", c + "");
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.QUANTITY_INCORRECT, meta);
		}

		/*
		 * if (st.getAmountOfVolunteers() != 0) { if (t.getAmountOfVolunteers()
		 * > st.possibleNumberOfVolunteers()) { return
		 * JSonResponseHelper.actionNotPossible(
		 * "Amount of volunteers has to be between 1 and " +
		 * st.possibleNumberOfVolunteers()); } }
		 */
		if (t.getStartTime() == null) {
			t.setStartTime(st.getStartTime());
		}
		if (t.getEndTime() == null) {
			t.setEndTime(st.getEndTime());
		}
		t.setTaskState(st.getTaskState());
		t.setReadyToPublish(st.isReadyToPublish());
		t.setCreator(u);
		ect.indexOrUpdate("" + t.getId(), t);
		taskDAO.save(t);
		t.updateReadyStatus(taskDAO);

		return JSONResponseHelper.successfullyCreated(t);

	}

	/**
	 * Return a sorted list of elements with the best fitting tasks for the
	 * logged in user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/find", "/find/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> findTasks() {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Set<TaskShort> set = decider.findTasks(user, new UserFilterParameters()).stream()
				.map(evaltask -> evaltask.getTask().toShort()).collect(Collectors.toSet());

		return JSONResponseHelper.createResponse(set, true);

	}

	/**
	 * Return a sorted list of a defined number of elements with the best
	 * fitting tasks for the logged in user.
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/find/{number_of_tasks}",
			"/find/{number_of_tasks}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> findBestTasks(@PathVariable(value = "number_of_tasks") int numberOfTasks) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		ArrayList<EvaluatedTask> tasks = new ArrayList<>();

		int count = 0;

		for (EvaluatedTask task : decider.findTasks(user, new UserFilterParameters())) {

			if (count == numberOfTasks) {
				break;
			}

			tasks.add(task);
			count++;
		}

		return JSONResponseHelper.createResponse(tasks, true);

	}

	/**
	 * Add/Adjust multiple materials assigned to a task OR overwrite all
	 * materials assigned to a task
	 * 
	 * @param json
	 * @param taskId
	 * @param action
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/{task_id}/material/multiple/{action}",
			"/{task_id}/material/multiple/{action}/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addMaterials(@RequestBody String json, @PathVariable(value = "task_id") Long taskId,
			@PathVariable(value = "action") String action) throws JsonMappingException, IOException {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		if (!(action.equals("add") || action.equals("overwrite"))) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ACTION_NOT_VALID);
		}

		Task t = taskDAO.findOne(taskId);

		if (t != null) {

			if (user.hasTaskPermissions(t)) {

				if (action.equals("overwrite")) {

					Set<Material> del = new HashSet<>();

					for (Material m : t.getMaterials()) {
						del.add(m);
					}

					for (Material m : del) {
						m.getTask().getMaterials().remove(m);
						taskDAO.save(t);
						materialDAO.delete(m);
					}

				}

				ObjectMapper mapper = new ObjectMapper();
				MaterialMapping[] mma = null;
				mma = mapper.readValue(json, MaterialMapping[].class);

				HashMap<String, HashMap<String, String>> fullresponse = new HashMap<>();

				if (mma != null) {
					for (MaterialMapping mm : mma) {
						HashMap<String, String> response = new HashMap<>();
						Material m = mm.mapToMaterial(response, materialDAO);
						if (m != null) {
							m.setTask(t);
							materialDAO.save(m);
							fullresponse.put(m.getId() + "", response);
						}
					}
				}

				HashMap<String, Object> meta = new HashMap<>();
				meta.put("result", fullresponse);

				return JSONResponseHelper.createResponse(true, meta, RESTAction.CREATE);

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Add a material to target task
	 * 
	 * @param json
	 * @param taskId
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/{task_id}/material/add",
			"/{task_id}/material/add/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addMaterial(@RequestBody String json, @PathVariable(value = "task_id") Long taskId)
			throws JsonParseException, JsonMappingException, IOException {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task st = taskDAO.findOne(taskId);

		if (st != null) {
			if (user.hasTaskPermissions(st)) {
				ObjectMapper mapper = new ObjectMapper();
				Material m;

				m = mapper.readValue(json, Material.class);

				m.setTask(st);
				st.addMaterial(m);
				materialDAO.save(m);
				taskDAO.save(st);

				HashMap<String, Object> meta = new HashMap<>();
				meta.put("task", st);
				return JSONResponseHelper.createResponse(m, true, meta, RESTAction.CREATE);

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
			}

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Update fields of target material on target task
	 * 
	 * @param json
	 * @param taskId
	 * @param materialId
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/{task_id}/material/{material_id}/update",
			"/{task_id}/material/{material_id}/update/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateMaterial(@RequestBody String json, @PathVariable(value = "task_id") Long taskId,
			@PathVariable(value = "material_id") Long materialId)
			throws JsonParseException, JsonMappingException, IOException {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task st = taskDAO.findOne(taskId);

		if (st != null) {
			if (user.hasTaskPermissions(st)) {

				Material old = materialDAO.findOne(materialId);

				if (old != null) {

					ObjectMapper mapper = new ObjectMapper();
					Material updated;

					updated = mapper.readValue(json, Material.class);

					old.update(updated);
					materialDAO.save(old);

					return JSONResponseHelper.successfullyUpdated(st);
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
				}

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
			}

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Remove a material from target task based on ID
	 * 
	 * @param taskId
	 * @param materialId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/material/{material_id}/remove",
			"/{task_id}/material/{material_id}/remove/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeMaterial(@PathVariable(value = "task_id") Long taskId,
			@PathVariable(value = "material_id") Long materialId) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task st = taskDAO.findOne(taskId);

		if (st != null) {
			if (user.hasTaskPermissions(st)) {

				Material delm = null;

				for (Material m : st.getMaterials()) {
					if (m.getId() == materialId) {
						delm = m;
					}
				}

				st.getMaterials().remove(delm);

				materialDAO.delete(delm);

				taskDAO.save(st);
				return JSONResponseHelper.successfullyUpdated(st);

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
			}

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Subscribe to a material of a task with a quantity, or change the quantity
	 * if already subscribed
	 * 
	 * @param taskId
	 * @param materialId
	 * @param quantity
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/material/{material_id}/subscribe/{quantity}",
			"/{task_id}/material/{material_id}/subscribe/{quantity}/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> subscribeMaterial(@PathVariable(value = "task_id") Long taskId,
			@PathVariable(value = "material_id") Long materialId, @PathVariable(value = "quantity") Long quantity) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task st = taskDAO.findOne(taskId);

		if (st != null) {

			Material m = materialDAO.findOne(materialId);

			if (m != null) {

				UserMaterialSubscription um = userMaterialSubscriptionDAO.findByUserAndMaterial(user, m);

				if (um != null) {

					String status = m.subscribable(quantity, um);

					if (status.equals("OK")) {
						HashMap<String, Object> meta = new HashMap<>();
						meta.put("task_id", st.getId() + "");
						meta.put("material_id", m.getId() + "");
						meta.put("previous_quantity", um.getQuantity() + "");
						meta.put("new_quantity", quantity + "");
						meta.put("cause", "USER_ALREADY_SUBSCRIBED");
						meta.put("action", "QUANTITY_UPDATED");
						meta.put("task", st);

						um.setQuantity(quantity);
						userMaterialSubscriptionDAO.save(um);
						meta.put("subscription", um);

						return JSONResponseHelper.createResponse(m, true, meta, RESTAction.GET);

					}
				}

				String status = m.subscribable(quantity);

				if (status.equals("OK")) {
					UserMaterialSubscription ums = new UserMaterialSubscription(user, m, quantity);
					m.addUserSubscription(ums);
					materialDAO.save(m);

					HashMap<String, Object> meta = new HashMap<>();
					meta.put("task_id", st.getId() + "");
					meta.put("material_id", m.getId() + "");
					meta.put("quantity", quantity + "");
					meta.put("action", "QUANTITY_UPDATED");
					meta.put("material", m);
					meta.put("subscription", ums);

					return JSONResponseHelper.createResponse(st, true, meta, RESTAction.GET);
				}

				ErrorCode cause = ErrorCode.QUANTITY_TOO_HIGH;

				if (status.equals("QUANTITY_TOO_SMALL")) {
					cause = ErrorCode.QUANTITY_TOO_SMALL;
				} else if (status.equals("QUANTITY_TOO_HIGH")) {
					cause = ErrorCode.QUANTITY_TOO_HIGH;
				}

				return JSONResponseHelper.createResponse(false, "bad_request", cause);

			}
		}

		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
	}

	/**
	 * Set the fullfilled-variable of target subscription
	 * @param subscriptionId
	 * @param fullfilled
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/subscription/{subscription_id}/fullfilled/{fullfilled}",
			"/subscription/{subscription_id}/fullfilled/{fullfilled}/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> fullfillSubscription(@PathVariable(value = "subscription_id") Long subscriptionId,
			@PathVariable(value = "fullfilled") boolean fullfilled) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		UserMaterialSubscription ums = userMaterialSubscriptionDAO.findOne(subscriptionId);
		if (ums.getUser().equals(user) || user.hasTaskPermissions(ums.getMaterial().getTask())) {
			ums.setFullfilled(fullfilled);
			userMaterialSubscriptionDAO.save(ums);
			return JSONResponseHelper.successfullyUpdated(ums);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
		}
	}

	/**
	 * Unsubscribe to a subscribed material
	 * 
	 * @param taskId
	 * @param materialId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/material/{material_id}/unsubscribe",
			"/{task_id}/material/{material_id}/unsubscribe/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> unsubscribeMaterial(@PathVariable(value = "task_id") Long taskId,
			@PathVariable(value = "material_id") Long materialId) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task st = taskDAO.findOne(taskId);

		if (st != null) {

			Material m = materialDAO.findOne(materialId);

			if (m != null) {

				UserMaterialSubscription ums = userMaterialSubscriptionDAO.findByUserAndMaterial(user, m);

				if (ums != null) {

					HashMap<String, Object> meta = new HashMap<>();
					meta.put("task_id", st.getId() + "");
					meta.put("material_id", m.getId() + "");
					meta.put("action", "UNSUBSCRIBED");
					meta.put("task", st);
					meta.put("subscription", ums);

					ResponseEntity<String> v = JSONResponseHelper.createResponse(m, true, meta, RESTAction.GET);

					m.getSubscribedUsers().remove(ums);
					userMaterialSubscriptionDAO.delete(ums);

					return v;

				}
			}
		}

		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
	}

	/**
	 * Sets a single task ready to be published, only works if it's children are
	 * ready
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	/*
	 * @RequestMapping(value = { "/{task_id}/publish/ready/single",
	 * "/{task_id}/publish/ready/single/" }, method = RequestMethod.GET,
	 * produces = "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String>
	 * readyforPublish(@PathVariable(value = "task_id") Long taskId) { Task t =
	 * taskDAO.findOne(taskId); UsernamePasswordAuthenticationToken userDetails
	 * = (UsernamePasswordAuthenticationToken) SecurityContextHolder
	 * .getContext().getAuthentication(); CracUser user =
	 * userDAO.findByName(userDetails.getName());
	 * 
	 * if (t != null) { if (user.hasTaskPermissions(t)) {
	 * 
	 * if (t.fieldsFilled()) { if (t.childTasksReady()) {
	 * t.setReadyToPublish(true); taskDAO.save(t); return
	 * JSonResponseHelper.successFullyUpdated(t); } else { return
	 * JSonResponseHelper.createResponse(false, "bad_request",
	 * "CHILDREN_NOT_READY"); } } else { return
	 * JSonResponseHelper.createResponse(false, "bad_request",
	 * "TASK_NOT_READY"); }
	 * 
	 * } else { return JSonResponseHelper.createResponse(false, "bad_request",
	 * "PERMISSIONS_NOT_SUFFICIENT"); } } else { return
	 * JSonResponseHelper.idNotFound(); } }
	 */

	/**
	 * Sets target task and all children ready to be published
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	/*
	 * @RequestMapping(value = { "/{task_id}/publish/ready/tree",
	 * "/{task_id}/publish/ready/tree/" }, method = RequestMethod.GET, produces
	 * = "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String>
	 * forcePublish(@PathVariable(value = "task_id") Long taskId) { Task t =
	 * taskDAO.findOne(taskId); UsernamePasswordAuthenticationToken userDetails
	 * = (UsernamePasswordAuthenticationToken) SecurityContextHolder
	 * .getContext().getAuthentication(); CracUser user =
	 * userDAO.findByName(userDetails.getName());
	 * 
	 * if (t != null) { if (user.hasTaskPermissions(t)) { HashMap<String,
	 * String> results = new HashMap<>(); t.readyToPublishTree(results,
	 * taskDAO); if (results.size() == 0) { return
	 * JSonResponseHelper.successFullyUpdated(t); } else { return
	 * JSonResponseHelper.messageArray(results); }
	 * 
	 * } else { return JSonResponseHelper.createResponse(false, "bad_request",
	 * "PERMISSIONS_NOT_SUFFICIENT"); } } else { return
	 * JSonResponseHelper.idNotFound(); } }
	 */

	/**
	 * Copy target task
	 * 
	 * @param task_id
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@PreAuthorize("hasRole('ADMIN') OR hasRole('EDITOR')")
	@RequestMapping(value = { "/{task_id}/copy",
			"/{task_id}/copy/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> copyTask(@RequestBody String json, @PathVariable(value = "task_id") Long task_id)
			throws JsonParseException, JsonMappingException, IOException {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task t = taskDAO.findOne(task_id);

		if (t != null) {

			if (!t.isSuperTask() || t.getTaskState() != ConcreteTaskState.COMPLETED) {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.CANNOT_BE_COPIED);
			}

			ObjectMapper mapper = new ObjectMapper();

			Calendar ca = mapper.readValue(json, PostOptions.class).getDate();

			if (ca != null) {

				Task c = t.copy(ca);
				c.setCreator(user);

				taskDAO.save(c);
				return JSONResponseHelper.successfullyCreated(c);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.EMPTY_DATA);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);

		}
	}

	/**
	 * Adds target competence to target task, it is mandatory to add the
	 * proficiency and importanceLvl
	 * 
	 * @param task_id
	 * @param competence_id
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/{task_id}/competence/{competence_id}/require",
			"/{task_id}/competence/{competence_id}/require/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> requireCompetence(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "competence_id") Long competence_id, @RequestBody String json)
			throws JsonParseException, JsonMappingException, IOException {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		ObjectMapper mapper = new ObjectMapper();

		PostOptions po;

		po = mapper.readValue(json, PostOptions.class);

		Task task = taskDAO.findOne(task_id);
		Competence competence = competenceDAO.findOne(competence_id);
		if (task != null && competence != null) {
			if (user.getCreatedTasks().contains(task) && task.getTaskState() == ConcreteTaskState.NOT_PUBLISHED
					|| user.confirmRole("ADMIN") && task.getTaskState() == ConcreteTaskState.NOT_PUBLISHED) {
				competenceTaskRelDAO.save(new CompetenceTaskRel(competence, task, po.getProficiencyValue(),
						po.getImportanceValue(), po.isMandatory()));
				return JSONResponseHelper.successfullyAssigned(competence);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.RESOURCE_UNCHANGEABLE);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}
	}

	/**
	 * Add multiple competences
	 * 
	 * @param json
	 * @param taskId
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/{task_id}/competence/require",
			"/{task_id}/competence/require/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> requireCompetences(@RequestBody String json,
			@PathVariable(value = "task_id") Long taskId) throws JsonMappingException, IOException {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task t = taskDAO.findOne(taskId);

		if (t != null) {

			if (user.hasTaskPermissions(t)) {

				ObjectMapper mapper = new ObjectMapper();
				CompetenceTaskMapping[] m = null;
				m = mapper.readValue(json, CompetenceTaskMapping[].class);

				HashMap<String, HashMap<String, String>> fullresponse = new HashMap<>();

				if (m != null) {
					for (CompetenceTaskMapping singlem : m) {
						HashMap<String, String> singleresponse = new HashMap<>();
						Competence c = competenceDAO.findOne(singlem.getCompetenceId());

						if (c != null) {

							CompetenceTaskRel r = competenceTaskRelDAO.findByTaskAndCompetence(t, c);
							if (r != null) {
								singleresponse.put("competence_status", "ALREADY_ASSIGNED_VALUES_ADJUSTED");
							} else {
								r = new CompetenceTaskRel();
								r.setCompetence(c);
								r.setTask(t);
								singleresponse.put("competence_status", "COMPETENCE_ASSIGNED");
							}

							if (singlem.getImportanceLevel() == -200) {
								singleresponse.put("importanceLevel", "NOT_ASSIGNED");
							} else {
								if (singlem.getImportanceLevel() >= 0 && singlem.getImportanceLevel() <= 100) {
									r.setImportanceLevel(singlem.getImportanceLevel());
								} else {
									singleresponse.put("importanceLevel", "VALUE_NOT_VALID");
								}
							}

							if (singlem.getNeededProficiencyLevel() == -200) {
								singleresponse.put("neededProficiencyLevel", "NOT_ASSIGNED");
							} else {
								if (singlem.getNeededProficiencyLevel() >= 0
										&& singlem.getNeededProficiencyLevel() <= 100) {
									r.setNeededProficiencyLevel(singlem.getNeededProficiencyLevel());
								} else {
									singleresponse.put("neededProficiencyLevel", "VALUE_NOT_VALID");
								}
							}

							if (singlem.getMandatory() == -1) {
								singleresponse.put("mandatory", "NOT_ASSIGNED");
							} else if (singlem.getMandatory() == 0) {
								r.setMandatory(false);
							} else if (singlem.getMandatory() == 1) {
								r.setMandatory(true);
							} else {
								singleresponse.put("mandatory", "VALUE_NOT_VALID");
							}
							competenceTaskRelDAO.save(r);
							fullresponse.put(singlem.getCompetenceId() + "", singleresponse);
						} else {
							if (singlem.getCompetenceId() != 0) {
								singleresponse.put("competence_status", "COMPETENCE_NOT_FOUND");
								fullresponse.put(singlem.getCompetenceId() + "", singleresponse);
							}
						}
					}
				}
				HashMap<String, Object> meta = new HashMap<>();
				meta.put("result", fullresponse);

				return JSONResponseHelper.createResponse(true, meta, RESTAction.CREATE);

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Overwrites all assigned competences with given competences
	 * 
	 * @param json
	 * @param taskId
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/{task_id}/competence/overwrite",
			"/{task_id}/competence/overwrite/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> overwriteCompetences(@RequestBody String json,
			@PathVariable(value = "task_id") Long taskId) throws JsonMappingException, IOException {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task t = taskDAO.findOne(taskId);

		if (t != null) {

			if (user.hasTaskPermissions(t)) {

				Set<CompetenceTaskRel> toremove = new HashSet<>();

				for (CompetenceTaskRel ctr : t.getMappedCompetences()) {
					toremove.add(ctr);
				}

				for (CompetenceTaskRel ctr : toremove) {
					ctr.getCompetence().getCompetenceTaskRels().remove(ctr);
					ctr.getTask().getMappedCompetences().remove(ctr);
					competenceTaskRelDAO.delete(ctr);
				}

				ObjectMapper mapper = new ObjectMapper();
				CompetenceTaskMapping[] m = null;
				m = mapper.readValue(json, CompetenceTaskMapping[].class);

				HashMap<String, HashMap<String, String>> fullresponse = new HashMap<>();

				if (m != null) {
					for (CompetenceTaskMapping singlem : m) {
						HashMap<String, String> singleresponse = new HashMap<>();
						Competence c = competenceDAO.findOne(singlem.getCompetenceId());

						if (c != null) {

							CompetenceTaskRel r = new CompetenceTaskRel();
							r.setCompetence(c);
							r.setTask(t);
							singleresponse.put("competence_status", "COMPETENCE_ASSIGNED");

							if (singlem.getImportanceLevel() == -200) {
								singleresponse.put("importanceLevel", "NOT_ASSIGNED");
							} else {
								if (singlem.getImportanceLevel() >= 0 && singlem.getImportanceLevel() <= 100) {
									r.setImportanceLevel(singlem.getImportanceLevel());
								} else {
									singleresponse.put("importanceLevel", "VALUE_NOT_VALID");
								}
							}

							if (singlem.getNeededProficiencyLevel() == -200) {
								singleresponse.put("neededProficiencyLevel", "NOT_ASSIGNED");
							} else {
								if (singlem.getNeededProficiencyLevel() >= 0
										&& singlem.getNeededProficiencyLevel() <= 100) {
									r.setNeededProficiencyLevel(singlem.getNeededProficiencyLevel());
								} else {
									singleresponse.put("neededProficiencyLevel", "VALUE_NOT_VALID");
								}
							}

							if (singlem.getMandatory() == -1) {
								singleresponse.put("mandatory", "NOT_ASSIGNED");
							} else if (singlem.getMandatory() == 0) {
								r.setMandatory(false);
							} else if (singlem.getMandatory() == 1) {
								r.setMandatory(true);
							} else {
								singleresponse.put("mandatory", "VALUE_NOT_VALID");
							}
							competenceTaskRelDAO.save(r);
							fullresponse.put(singlem.getCompetenceId() + "", singleresponse);
						} else {
							if (singlem.getCompetenceId() != 0) {
								singleresponse.put("competence_status", "COMPETENCE_NOT_FOUND");
								fullresponse.put(singlem.getCompetenceId() + "", singleresponse);
							}
						}
					}
				}
				HashMap<String, Object> meta = new HashMap<>();
				meta.put("result", fullresponse);

				return JSONResponseHelper.createResponse(true, meta, RESTAction.CREATE);

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Adds target competence to target task
	 * 
	 * @param task_id
	 * @param competence_id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/competence/{competence_id}/remove",
			"/{task_id}/competence/{competence_id}/remove/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeCompetence(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "competence_id") Long competence_id) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task task = taskDAO.findOne(task_id);
		Competence competence = competenceDAO.findOne(competence_id);
		CompetenceTaskRel ctr = competenceTaskRelDAO.findByTaskAndCompetence(task, competence);
		if (task != null && competence != null && ctr != null) {
			if (user.getCreatedTasks().contains(task) && task.getTaskState() == ConcreteTaskState.NOT_PUBLISHED
					|| user.confirmRole("ADMIN") && task.getTaskState() == ConcreteTaskState.NOT_PUBLISHED) {
				task.getMappedCompetences().remove(ctr);
				competence.getCompetenceTaskRels().remove(ctr);
				competenceTaskRelDAO.delete(ctr);
				return JSONResponseHelper.successfullyDeleted(competence);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.RESOURCE_UNCHANGEABLE);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}
	}

	/**
	 * Adjust the values of a task-competence connection
	 * 
	 * @param taskId
	 * @param competenceId
	 * @param json
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/{task_id}/competence/{competence_id}/adjust",
			"/{competence_id}/adjust/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> adjustCompetence(@PathVariable(value = "task_id") Long taskId,
			@PathVariable(value = "competence_id") Long competenceId, @RequestBody String json)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		PostOptions po;

		po = mapper.readValue(json, PostOptions.class);

		Task task = taskDAO.findOne(taskId);
		Competence competence = competenceDAO.findOne(competenceId);

		if (competence != null && task != null) {
			CompetenceTaskRel ctr = competenceTaskRelDAO.findByTaskAndCompetence(task, competence);

			if (ctr != null) {
				ctr.setImportanceLevel(po.getImportanceLevel());
				ctr.setMandatory(po.isMandatory());
				ctr.setNeededProficiencyLevel(po.getNeededProficiencyLevel());
				ResponseEntity<String> v = JSONResponseHelper.successfullyUpdated(ctr);
				competenceTaskRelDAO.save(ctr);
				return v;
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Finds and returns all tasks that contain a given pattern
	 * 
	 * @param task_name
	 * @return ResponseEntity
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = { "/searchDirect/{task_name}",
			"/searchDirect/{task_name}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getByName(@PathVariable(value = "task_name") String task_name) {
		return JSONResponseHelper.createResponse(taskDAO.findMultipleByNameLike("%" + task_name + "%"), true);
	}

	/**
	 * Sets the relation between the logged in user and target task to done,
	 * meaning the user completed the task
	 * 
	 * @param task_id
	 * @param done
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/done/{done_boolean}",
			"/{task_id}/done/{done_boolean}/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> singleUserDone(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "done_boolean") String done) {

		Task task = taskDAO.findOne(task_id);

		if (task != null) {

			if (task.getTaskState() == ConcreteTaskState.STARTED) {

				UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
						.getContext().getAuthentication();
				CracUser user = userDAO.findByName(userDetails.getName());

				UserTaskRel utr = userTaskRelDAO.findByUserAndTaskAndParticipationTypeNot(user, task,
						TaskParticipationType.LEADING);

				if (utr != null) {
					if (utr.getParticipationType() == TaskParticipationType.PARTICIPATING) {
						if (done.equals("true")) {
							utr.setCompleted(true);
						} else if (done.equals("false")) {
							utr.setCompleted(false);
						} else {
							HashMap<String, Object> meta = new HashMap<>();
							meta.put("state", done);
							return JSONResponseHelper.createResponse(false, "bad_request",
									ErrorCode.STATE_NOT_AVAILABLE, meta);
						}
						userTaskRelDAO.save(utr);

						// Check if all users are done, if yes, notify the
						// leaders

						boolean alldone = true;

						for (UserTaskRel ut : task.getUserRelationships()) {
							if (ut.getParticipationType() == TaskParticipationType.PARTICIPATING && !ut.isCompleted()) {
								alldone = false;
								break;
							}
						}

						if (alldone) {
							for (UserTaskRel l : task.getRelationships(1, TaskParticipationType.LEADING)) {
								nf.createSystemNotification(TaskDoneNotification.class, l.getUser(),
										NotificationConfiguration.create().put("task", task.toShort()));
							}
						}

						return JSONResponseHelper.successfullyUpdated(task);
					}
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.USER_NOT_PARTICIPATING);
				}
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.TASK_NOT_STARTED);
			}
		}

		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);

	}

	/**
	 * Force state-changes on task-trees that are not following the normal
	 * state-change rules
	 * 
	 * @param task_id
	 * @param stateName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/state/force/{state_name}",
			"/{task_id}/state/force/{state_name}/" }, method = RequestMethod.PUT, produces = "application/json")

	@ResponseBody
	public ResponseEntity<String> changeTaskState(@PathVariable(value = "task_id") Long task_id,

			@PathVariable(value = "state_name") String stateName) {

		Task task = taskDAO.findOne(task_id);

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		if (user.hasTaskPermissions(task)) {

			if (task != null) {

				ConcreteTaskState oldState = task.getTaskState();

				ConcreteTaskState state = ConcreteTaskState.NOT_PUBLISHED;
				int s = 0;

				if (stateName.equals("unpublish")) {

					s = task.unpublish(userTaskRelDAO, taskDAO);
					switch (s) {
					case 3:
						state = ConcreteTaskState.NOT_PUBLISHED;
						break;
					default:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.UNDEFINED_ERROR);
					}

				} else if (stateName.equals("complete")) {

					s = task.forceComplete(taskDAO);
					switch (s) {
					case 1:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.TASK_NOT_READY);
					case 2:
						return JSONResponseHelper.createResponse(false, "bad_request",
								ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
					case 3:
						state = ConcreteTaskState.COMPLETED;
						break;
					default:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.UNDEFINED_ERROR);
					}

				} else {
					HashMap<String, Object> meta = new HashMap<>();
					meta.put("state", stateName);
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.STATE_NOT_AVAILABLE, meta);
				}

				task.setTaskState(state);
				taskDAO.save(task);
				HashMap<String, Object> meta = new HashMap<>();
				meta.put("old_state", oldState.toString());
				meta.put("new_state", state.toString());
				return JSONResponseHelper.createResponse(true, meta);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
		}
	}

	/**
	 * Change the state of target task, for each state different prerequisites
	 * have to be fullfilled
	 * 
	 * @param task_id
	 * @param stateName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/state/changeTo/{state}",
			"/{task_id}/state/changeTo/{state}/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> changeTaskState(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "state") ConcreteTaskState stateName) {

		Task task = taskDAO.findOne(task_id);

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		if (user.hasTaskPermissions(task)) {

			if (task != null) {

				ConcreteTaskState oldState = task.getTaskState();

				try {
					task.setTaskState(stateName, taskDAO);
				} catch (InvalidActionException e) {
					return JSONResponseHelper.createResponse(false, "bad_request", e.getError());
				}

				taskDAO.save(task);

				HashMap<String, Object> meta = new HashMap<>();
				meta.put("old_state", oldState.toString());
				meta.put("new_state", stateName.toString());

				return JSONResponseHelper.createResponse(true, meta);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
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

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser loggedU = userDAO.findByName(userDetails.getName());
		CracUser targetU = userDAO.findOne(userId);
		Task task = taskDAO.findOne(taskId);
		System.out.println(taskId);

		if (targetU != null && task != null) {

			if (loggedU.hasTaskPermissions(task)) {
				Notification n = nf.createNotification(LeadNomination.class, targetU, loggedU,
						NotificationConfiguration.create().put("task", task.toShort()));
				return JSONResponseHelper.successfullyCreated(n);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.RESOURCE_UNCHANGEABLE);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Issues an invite-notification to the target-user
	 * 
	 * @param userId
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/invite/{inv_type}/{inv_id}",
			"/{task_id}/invite/{inv_type}/{inv_id}/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> invitePerson(@PathVariable(value = "inv_id") Long invId,
			@PathVariable(value = "task_id") Long taskId, @PathVariable(value = "inv_type") String invType) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		Task task = taskDAO.findOne(taskId);
		List<CracUser> users = new ArrayList<>();

		if (user.hasTaskPermissions(task)) {
			if (task != null) {
				if (invType.equals("user")) {
					CracUser inv = userDAO.findOne(invId);
					if (inv != null) {
						users.add(inv);
					} else {
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
					}
				} else if (invType.equals("group")) {
					CracGroup inv = groupDAO.findOne(invId);
					if (inv != null) {
						if (!inv.getInvitedToTasks().contains(task)) {
							inv.getInvitedToTasks().add(task);
						} else {
							return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ALREADY_ASSIGNED);
						}
						for (CracUser u : inv.getEnroledUsers()) {
							users.add(u);
						}
					} else {
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
					}
				}
				for (CracUser u : users) {
					nf.createNotification(TaskInvitation.class, u, user,
							NotificationConfiguration.create().put("task", task.toShort()));
				}
				return JSONResponseHelper.successfullyCreated(users);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
		}

	}

	/**
	 * Restrict a task to target group
	 * 
	 * @param userId
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/restrict/group/{group_id}",
			"/{task_id}/restrict/group/{group_id}/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> restrictTask(@PathVariable(value = "group_id") Long groupId,
			@PathVariable(value = "task_id") Long taskId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		Task task = taskDAO.findOne(taskId);

		if (user.hasTaskPermissions(task)) {
			if (task != null) {
				CracGroup group = groupDAO.findOne(groupId);
				if (group != null) {
					if (!group.getRestrictedTasks().contains(task)) {
						group.getRestrictedTasks().add(task);
					} else {
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ALREADY_ASSIGNED);
					}
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
				}
				return JSONResponseHelper.successfullyUpdated(task);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
		}

	}

	/**
	 * Restrict a task to multiple groups and replace the groups already there
	 * 
	 * @param userId
	 * @param taskId
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/{task_id}/restrict/group/multiple",
			"/{task_id}/restrict/group/multiple/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> inviteMultipleGroups(@RequestBody String json,
			@PathVariable(value = "task_id") Long taskId) throws JsonParseException, JsonMappingException, IOException {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		Task task = taskDAO.findOne(taskId);

		if (user.hasTaskPermissions(task)) {
			if (task != null) {

				ObjectMapper mapper = new ObjectMapper();

				PostOptions[] mappings = null;
				mappings = mapper.readValue(json, PostOptions[].class);

				CracGroup group;

				if (mappings != null) {

					for (CracGroup g : task.getRestrictingGroups()) {
						g.getRestrictedTasks().remove(task);
					}

					for (PostOptions po : mappings) {
						group = groupDAO.findOne((long) po.getId());
						if (group != null) {
							group.getRestrictedTasks().add(task);
						}
						groupDAO.save(group);
					}

					return JSONResponseHelper.successfullyUpdated(task);

				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
				}
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
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
		return JSONResponseHelper.createResponse(TaskParticipationType.values(), true);
	}

	/**
	 * Returns the values for the enum taskStates
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/taskStates", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> taskStates() {
		return JSONResponseHelper.createResponse(ConcreteTaskState.values(), true);
	}

	/**
	 * Returns the values for the enum taskType
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/taskTypes", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> taskTypes() {
		return JSONResponseHelper.createResponse(TaskType.values(), true);
	}

	/**
	 * Returns all tasks, that are supertasks
	 * 
	 * @return
	 */
	@RequestMapping(value = { "/parents", "/parents/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getParents() {
		return JSONResponseHelper
				.createResponse(taskDAO.findBySuperTaskNullAndTaskStateNot(ConcreteTaskState.NOT_PUBLISHED), true);
	}

	/**
	 * Fulltext-queries all tasks with Elasticsearch and returns the found ones.
	 * If bound to competence-system, compares if tasks are doable
	 * 
	 * @param json
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/elastic/query",
			"/elastic/query/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> queryES(@RequestBody String json)
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		PostOptions query;
		query = mapper.readValue(json, PostOptions.class);

		ArrayList<EvaluatedTask> et = ect.query(query.getText());

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		ArrayList<EvaluatedTask> doables = decider.findTasks(user, new UserFilterParameters());

		for (EvaluatedTask ets : et) {
			ets.setDoable(false);
			for (EvaluatedTask etd : doables) {
				if (etd.getTask().getId() == ets.getTask().getId()) {
					ets.setDoable(true);
				}
			}
		}
		return JSONResponseHelper.createResponse(et, true);
	}

	/**
	 * Add an attachment to target task
	 * 
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws InvalidActionException
	 */

	@RequestMapping(value = { "/{task_id}/attachment",
			"/{task_id}/attachment/" }, method = RequestMethod.POST, headers = "content-type=multipart/*", produces = "application/json")

	@ResponseBody
	public ResponseEntity<String> addAttachment(@RequestParam("file") MultipartFile file,
			@PathVariable(value = "task_id") Long task_id) throws IOException, InvalidActionException {
		Task t = taskDAO.findOne(task_id);

		Attachment a = new Attachment();

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		if (t != null) {
			if (user.hasTaskPermissions(t)) {

				String name = file.getOriginalFilename();
				String path = CracUtility.processUpload(file, "image/jpeg", "image/jpg", "image/png",
						"application/pdf");
				a.setPath(path);
				a.setName(name);
				a.setTask(t);
				t.getAttachments().add(a);
				a.setTask(t);
				taskDAO.save(t);
				return JSONResponseHelper.successfullyUpdated(t);
			}
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);

		}
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);

	}

	/**
	 * Remove an attachment from target task
	 * 
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws InvalidActionException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/{task_id}/attachment/{attachment_id}",
			"/{task_id}/attachment/{attachment_id}/" }, method = RequestMethod.DELETE, produces = "application/json")

	@ResponseBody
	public ResponseEntity<String> removeAttachment(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "attachment_id") Long attachment_id) throws InvalidActionException {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task t = taskDAO.findOne(task_id);

		if (t != null) {
			if (user.hasTaskPermissions(t)) {
				Attachment a = attachmentDAO.findByIdAndTask(attachment_id, t);
				if (a != null) {
					CracUtility.removeFile(a.getPath());
					t.getAttachments().remove(a);
					attachmentDAO.delete(a);
					taskDAO.save(t);
					return JSONResponseHelper.successfullyUpdated(t);
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);

				}
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.PERMISSIONS_NOT_SUFFICIENT);
			}

		}
		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);

	}

	/**
	 * Get target attachment of target task
	 * 
	 * @param attachment_id
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws InvalidActionException
	 */
	@RequestMapping(value = { "/{task_id}/attachment/{attachment_id}",
			"/{task_id}/attachment/{attachment_id}/" }, method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public ResponseEntity<byte[]> getUserImage(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "attachment_id") Long attachment_id) throws IOException, InvalidActionException {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser u = userDAO.findByName(userDetails.getName());

		Task t = taskDAO.findOne(task_id);

		if (t != null) {
			if (u.hasTaskPermissions(t)) {
				Attachment a = attachmentDAO.findByIdAndTask(attachment_id, t);
				if (a != null) {

					byte[] img = CracUtility.getFile(a.getPath());

					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.IMAGE_JPEG);

					return ResponseEntity.ok().headers(headers).body(img);
				}
			}
		}
		throw new InvalidActionException(ErrorCode.NOT_FOUND);
	}

	/**
	 * Add a comment to target task
	 * 
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */

	@RequestMapping(value = "/{task_id}/comment/add", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")

	@ResponseBody
	public ResponseEntity<String> addComment(@RequestBody String json, @PathVariable(value = "task_id") Long task_id)
			throws JsonMappingException, IOException {
		Task myTask = taskDAO.findOne(task_id);
		ObjectMapper mapper = new ObjectMapper();
		Comment myComment = mapper.readValue(json, Comment.class);
		myTask.getComments().add(myComment);
		myComment.setTask(myTask);
		taskDAO.save(myTask);
		return JSONResponseHelper.successfullyCreated(myComment);
	}

	/**
	 * Remove a comment from target task
	 * 
	 * @param json
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonMappingException
	 * @throws IOException
	 */

	@RequestMapping(value = "/{task_id}/comment/{comment_id}/remove", method = RequestMethod.DELETE, produces = "application/json")

	@ResponseBody
	public ResponseEntity<String> removeComment(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "comment_id") Long comment_id) {
		Task myTask = taskDAO.findOne(task_id);
		Comment myComment = commentDAO.findOne(comment_id);
		myTask.getComments().remove(myComment);
		commentDAO.delete(myComment);
		taskDAO.save(myTask);
		return JSONResponseHelper.successfullyUpdated(myTask);
	}

	/**
	 * Returns all comments of a task
	 * 
	 * @param task_id
	 * @return ResponseEntity
	 * @throws JsonProcessingException
	 */

	@RequestMapping(value = "/{task_id}/comments", method = RequestMethod.GET, produces = "application/json")

	@ResponseBody
	public ResponseEntity<String> getComments(@PathVariable(value = "task_id") Long task_id)
			throws JsonProcessingException {
		Task myTask = taskDAO.findOne(task_id);
		return JSONResponseHelper.createResponse(myTask.getComments(), true);
	}

}
