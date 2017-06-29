package crac.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.components.matching.Decider;
import crac.components.matching.configuration.UserFilterParameters;
import crac.components.notifier.NotificationHelper;
import crac.components.notifier.notifications.LeadNomination;
import crac.components.notifier.notifications.TaskDoneNotification;
import crac.components.notifier.notifications.TaskInvitation;
import crac.components.utility.DataAccess;
import crac.components.utility.JSONResponseHelper;
import crac.components.utility.UpdateEntitiesHelper;
import crac.enums.ErrorCause;
import crac.enums.RESTAction;
import crac.enums.TaskParticipationType;
import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.models.db.daos.CommentDAO;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CompetenceTaskRelDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.MaterialDAO;
import crac.models.db.daos.RepetitionDateDAO;
import crac.models.db.daos.RoleDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserMaterialSubscriptionDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Comment;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Material;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import crac.models.db.relation.UserMaterialSubscription;
import crac.models.db.relation.UserTaskRel;
import crac.models.input.CompetenceTaskMapping;
import crac.models.input.MaterialMapping;
import crac.models.input.PostOptions;
import crac.models.output.ArchiveTask;
import crac.models.output.TaskDetails;
import crac.models.output.TaskShort;
import crac.models.utility.EvaluatedTask;

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
	private MaterialDAO materialDAO;

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
		return JSONResponseHelper.createResponse(taskDAO.findAll(), true);
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
		Iterable<Task> taskList = taskDAO.findAll();

		for (Task t : taskList) {
			t.start();
		}

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
			if (task.checkStartAllowance()) {
				task.start();
			}
			return JSONResponseHelper.createResponse(new TaskDetails(task, user), true);
		}

		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);

	}

	/**
	 * Adds target task to the open-tasks of the logged-in user or changes it's
	 * state
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/add/{state_name}",
			"/task/{task_id}/{state_name}/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addToUser(@PathVariable(value = "state_name") String stateName,
			@PathVariable(value = "task_id") Long taskId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task task = taskDAO.findOne(taskId);

		if (task != null) {
			TaskParticipationType state = TaskParticipationType.PARTICIPATING;
			if (stateName.equals("participate")) {
				if (task.isJoinable()) {
					if (!task.isFull()) {
						state = TaskParticipationType.PARTICIPATING;
					} else {
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_IS_FULL);
					}
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_NOT_JOINABLE);
				}
			} else if (stateName.equals("follow")) {
				if (task.isFollowable()) {
					state = TaskParticipationType.FOLLOWING;
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_NOT_JOINABLE);
				}
			} else {
				HashMap<String, Object> meta = new HashMap<>();
				meta.put("state", stateName);
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.STATE_NOT_AVAILABLE, meta);
			}

			UserTaskRel rel = userTaskRelDAO.findByUserAndTaskAndParticipationTypeNot(user, task,
					TaskParticipationType.LEADING);

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

			return JSONResponseHelper.successfullyAssigned(task);

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

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
			if (!task.inConduction()) {
				userTaskRelDAO.delete(userTaskRelDAO.findByUserAndTaskAndParticipationTypeNot(user, task,
						TaskParticipationType.LEADING));
				return JSONResponseHelper.successfullyDeleted(task);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_ALREADY_IN_PROCESS);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Returns all tasks of logged in user, divided in the
	 * TaskParticipationTypes
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

		Set<TaskShort> taskListFollow = new HashSet<TaskShort>();
		Set<TaskShort> taskListPart = new HashSet<TaskShort>();
		Set<TaskShort> taskListLead = new HashSet<TaskShort>();

		HashMap<String, Object> meta = new HashMap<>();
		meta.put("leading", taskListLead);
		meta.put("following", taskListFollow);
		meta.put("participating", taskListPart);

		if (taskRels.size() != 0) {

			for (UserTaskRel utr : taskRels) {
				if (utr.getParticipationType() == TaskParticipationType.FOLLOWING) {
					taskListFollow.add(new TaskShort(utr.getTask()));
				} else if (utr.getParticipationType() == TaskParticipationType.PARTICIPATING) {
					taskListPart.add(new TaskShort(utr.getTask()));
				} else if (utr.getParticipationType() == TaskParticipationType.LEADING) {
					taskListLead.add(new TaskShort(utr.getTask()));
				}
			}
		}

		return JSONResponseHelper.createResponse(user, true, meta);

	}

	/**
	 * Returns all completed tasks of a user by participationType
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
		for(UserTaskRel tr : trels){
			if(tr.getTask().getTaskState() == TaskState.COMPLETED){
				tcomp.add(new ArchiveTask(tr));
			}
		}
		return JSONResponseHelper.createResponse(tcomp, true);

	}

	/**
	 * Returns all completed projects
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/completed/all",
			"/completed/all/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getAllCompletedTasks() {
		return JSONResponseHelper.createResponse(taskDAO.findBySuperTaskNullAndTaskState(TaskState.COMPLETED), true);
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
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.EMPTY_DATA);
		}

	}

	/**
	 * Updates target task
	 * 
	 * @param json
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}",
			"/{task_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateTask(@RequestBody String json, @PathVariable(value = "task_id") Long id) {
		Task oldTask = taskDAO.findOne(id);

		if (oldTask.getTaskState() != TaskState.COMPLETED) {
			UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
					.getContext().getAuthentication();
			CracUser user = userDAO.findByName(userDetails.getName());

			if (oldTask != null) {
				if (user.hasTaskPermissions(oldTask)) {
					ObjectMapper mapper = new ObjectMapper();
					Task updatedTask;
					try {
						updatedTask = mapper.readValue(json, Task.class);
					} catch (JsonMappingException e) {
						System.out.println(e.toString());
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
					} catch (IOException e) {
						System.out.println(e.toString());
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
					}

					int c = checkAmountOfVolunteers(oldTask, updatedTask.getMaxAmountOfVolunteers(), true);

					// System.out.println("answer: " + c);

					if (c == 0) {
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.QUANTITY_TOO_HIGH);
					} else if (c > 0) {
						HashMap<String, Object> meta = new HashMap<>();
						meta.put("lowest", 1 + "");
						meta.put("highest", c + "");
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.QUANTITY_INCORRECT,
								meta);
					}
					/*
					 * if (oldTask.getSuperTask() != null) { if
					 * (oldTask.getSuperTask().getAmountOfVolunteers() != 0) {
					 * int availableNumber =
					 * oldTask.getSuperTask().possibleNumberOfVolunteers() +
					 * oldTask.getAmountOfVolunteers(); if
					 * (updatedTask.getAmountOfVolunteers() > availableNumber) {
					 * if (availableNumber == 0) { return JSonResponseHelper.
					 * actionNotPossible("Amount of volunteers is full"); } else
					 * { return JSonResponseHelper.actionNotPossible(
					 * "Amount of volunteers has to be between 1 and " +
					 * availableNumber); } } } }
					 */

					// UpdateEntitiesHelper.checkAndUpdateTask(oldTask,
					// updatedTask);
					oldTask.update(updatedTask);
					taskDAO.save(oldTask);
					oldTask.updateReadyStatus();
					DataAccess.getConnector(Task.class).indexOrUpdate("" + oldTask.getId(), oldTask);
					return JSONResponseHelper.successfullyUpdated(oldTask);
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request",
							ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
				}
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
			}

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_ALREADY_IN_PROCESS);
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
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/{supertask_id}/extend",
			"/{supertask_id}/extend/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> extendTask(@RequestBody String json,
			@PathVariable(value = "supertask_id") Long supertask_id) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task st = taskDAO.findOne(supertask_id);

		if (st != null) {
			if (user.hasTaskPermissions(st)) {

				if (st.isExtendable()) {
					return persistTask(st, user, json);
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_NOT_EXTENDABLE);
				}

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
									ErrorCause.TASK_HAS_OPEN_AMOUNT);
						}
					}

					t.setMaxAmountOfVolunteers(val);
					taskDAO.save(t);
					return JSONResponseHelper.successfullyUpdated(t);

				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ALREADY_FILLED);
				}

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	private ResponseEntity<String> persistTask(Task st, CracUser u, String json) {

		ObjectMapper mapper = new ObjectMapper();
		Task t;
		try {
			t = mapper.readValue(json, Task.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		t.setSuperTask(st);

		int c = checkAmountOfVolunteers(t, t.getMaxAmountOfVolunteers(), false);

		if (st.getTaskType() == TaskType.ORGANISATIONAL && t.getTaskType() == TaskType.SHIFT) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ORGANISATIONAL_EXTENDS_SHIFT);
		}

		if (st.getTaskType() == TaskType.WORKABLE && t.getTaskType() == TaskType.ORGANISATIONAL) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.WORKABLE_EXTENDS_ORGANISATIONAL);
		}

		if (st.getTaskType() == TaskType.WORKABLE && t.getTaskType() == TaskType.WORKABLE) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.WORKABLE_EXTENDS_WORKABLE);
		}

		if (st.getTaskType() == TaskType.SHIFT) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.SHIFT_EXTENDS);
		}

		if (c == 0) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.QUANTITY_TOO_HIGH);
		} else if (c > 0) {
			HashMap<String, Object> meta = new HashMap<>();
			meta.put("lowest", 1 + "");
			meta.put("highest", c + "");
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.QUANTITY_INCORRECT, meta);
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
		DataAccess.getConnector(Task.class).indexOrUpdate("" + t.getId(), t);
		taskDAO.save(t);
		t.updateReadyStatus();

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

		Decider unit = new Decider();

		return JSONResponseHelper.createResponse(unit.findTasks(user, new UserFilterParameters()), true);

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

		Decider unit = new Decider();

		ArrayList<EvaluatedTask> tasks = new ArrayList<>();

		int count = 0;

		for (EvaluatedTask task : unit.findTasks(user, new UserFilterParameters())) {

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
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ACTION_NOT_VALID);
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
				try {
					mma = mapper.readValue(json, MaterialMapping[].class);
				} catch (JsonMappingException e) {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
				} catch (IOException e) {
					System.out.println(e.toString());
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
				}

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
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Add a material to target task
	 * 
	 * @param json
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/material/add",
			"/{task_id}/material/add/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addMaterial(@RequestBody String json, @PathVariable(value = "task_id") Long taskId) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task st = taskDAO.findOne(taskId);

		if (st != null) {
			if (user.hasTaskPermissions(st)) {
				ObjectMapper mapper = new ObjectMapper();
				Material m;

				try {
					m = mapper.readValue(json, Material.class);
				} catch (JsonMappingException e) {
					System.out.println(e.toString());
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
				} catch (IOException e) {
					System.out.println(e.toString());
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
				}

				m.setTask(st);
				st.addMaterial(m);
				materialDAO.save(m);
				taskDAO.save(st);

				HashMap<String, Object> meta = new HashMap<>();
				meta.put("task", st);
				return JSONResponseHelper.createResponse(m, true, meta, RESTAction.CREATE);

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Update fields of target material on target task
	 * 
	 * @param json
	 * @param taskId
	 * @param materialId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/material/{material_id}/update",
			"/{task_id}/material/{material_id}/update/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateMaterial(@RequestBody String json, @PathVariable(value = "task_id") Long taskId,
			@PathVariable(value = "material_id") Long materialId) {
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

					try {
						updated = mapper.readValue(json, Material.class);
					} catch (JsonMappingException e) {
						System.out.println(e.toString());
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
					} catch (IOException e) {
						System.out.println(e.toString());
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
					}

					UpdateEntitiesHelper.checkAndUpdateMaterial(old, updated);
					materialDAO.save(old);

					return JSONResponseHelper.successfullyUpdated(st);
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
				}

			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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

				ErrorCause cause = ErrorCause.QUANTITY_TOO_HIGH;

				if (status.equals("QUANTITY_TOO_SMALL")) {
					cause = ErrorCause.QUANTITY_TOO_SMALL;
				} else if (status.equals("QUANTITY_TOO_HIGH")) {
					cause = ErrorCause.QUANTITY_TOO_HIGH;
				}

				return JSONResponseHelper.createResponse(false, "bad_request", cause);

			}
		}

		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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

		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
	 */
	@RequestMapping(value = { "/{task_id}/copy",
			"/{task_id}/copy/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> copyTask(@PathVariable(value = "task_id") Long task_id) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task original = taskDAO.findOne(task_id);

		if (original.getSuperTask() != null) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.CANNOT_BE_COPIED);
		}

		Task copy = original.copy(null);
		copy.setCreator(user);

		taskDAO.save(copy);

		return JSONResponseHelper.successfullyCreated(copy);

	}

	/**
	 * Adds target competence to target task, it is mandatory to add the
	 * proficiency and importanceLvl
	 * 
	 * @param task_id
	 * @param competence_id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/competence/{competence_id}/require",
			"/{task_id}/competence/{competence_id}/require/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> requireCompetence(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "competence_id") Long competence_id, @RequestBody String json) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		ObjectMapper mapper = new ObjectMapper();

		PostOptions po;

		try {
			po = mapper.readValue(json, PostOptions.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		Task task = taskDAO.findOne(task_id);
		Competence competence = competenceDAO.findOne(competence_id);
		if (task != null && competence != null) {
			if (user.getCreatedTasks().contains(task) && task.getTaskState() == TaskState.NOT_PUBLISHED
					|| user.confirmRole("ADMIN") && task.getTaskState() == TaskState.NOT_PUBLISHED) {
				competenceTaskRelDAO.save(new CompetenceTaskRel(competence, task, po.getProficiencyValue(),
						po.getImportanceValue(), po.isMandatory()));
				return JSONResponseHelper.successfullyAssigned(competence);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.RESOURCE_UNCHANGEABLE);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
				try {
					m = mapper.readValue(json, CompetenceTaskMapping[].class);
				} catch (JsonMappingException e) {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
				} catch (IOException e) {
					System.out.println(e.toString());
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
				}

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
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
				try {
					m = mapper.readValue(json, CompetenceTaskMapping[].class);
				} catch (JsonMappingException e) {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
				} catch (IOException e) {
					System.out.println(e.toString());
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
				}

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
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
			if (user.getCreatedTasks().contains(task) && task.getTaskState() == TaskState.NOT_PUBLISHED
					|| user.confirmRole("ADMIN") && task.getTaskState() == TaskState.NOT_PUBLISHED) {
				task.getMappedCompetences().remove(ctr);
				competence.getCompetenceTaskRels().remove(ctr);
				competenceTaskRelDAO.delete(ctr);
				return JSONResponseHelper.successfullyDeleted(competence);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.RESOURCE_UNCHANGEABLE);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}
	}

	/**
	 * Adjust the values of a task-competence connection
	 * 
	 * @param taskId
	 * @param competenceId
	 * @param json
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/competence/{competence_id}/adjust",
			"/{competence_id}/adjust/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> adjustCompetence(@PathVariable(value = "task_id") Long taskId,
			@PathVariable(value = "competence_id") Long competenceId, @RequestBody String json) {

		ObjectMapper mapper = new ObjectMapper();

		PostOptions po;

		try {
			po = mapper.readValue(json, PostOptions.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

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
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
	 * Sets the TaskRepetitionState from once to periodic if possible, mandatory
	 * to add a date as json
	 * 
	 * @param task_id
	 * @return ResponseEntity
	 */
	/*
	 * @RequestMapping(value = { "/{task_id}/periodic/set",
	 * "/{task_id}/priodic/set/" }, method = RequestMethod.POST, produces =
	 * "application/json", consumes = "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String>
	 * setTaskPeriodical(@RequestBody String json,
	 * 
	 * @PathVariable(value = "task_id") Long task_id) {
	 * 
	 * Task toSet = taskDAO.findOne(task_id);
	 * 
	 * if (toSet != null) { if (toSet.getSuperTask() != null) { return
	 * JSonResponseHelper.
	 * actionNotPossible("child-tasks can't be set periodical"); } else {
	 * RepetitionDate nrd; ObjectMapper mapper = new ObjectMapper();
	 * 
	 * try { nrd = mapper.readValue(json, RepetitionDate.class); } catch
	 * (JsonMappingException e) { System.out.println(e.toString()); return
	 * JSonResponseHelper.createGeneralResponse(false, "bad_request",
	 * ErrorCause.JSON_MAP_ERROR); } catch (IOException e) {
	 * System.out.println(e.toString()); return
	 * JSonResponseHelper.createGeneralResponse(false, "bad_request",
	 * ErrorCause.JSON_READ_ERROR); }
	 * toSet.setTaskRepetitionState(TaskRepetitionState.PERIODIC);
	 * RepetitionDate ord = toSet.getRepetitionDate();
	 * repetitionDateDAO.save(nrd); toSet.setRepetitionDate(nrd);
	 * taskDAO.save(toSet); repetitionDateDAO.delete(ord); return
	 * JSonResponseHelper.successFullAction("task set to periodical"); } } else
	 * { return JSonResponseHelper.createGeneralResponse(false, "bad_request",
	 * ErrorCause.ID_NOT_FOUND); } }
	 */

	/**
	 * Sets the TaskRepetitionState from periodic to once
	 * 
	 * @param task_id
	 * @return ResponseEntity
	 */
	/*
	 * @RequestMapping(value = { "/{task_id}/periodic/undo",
	 * "/{task_id}/priodic/undo/" }, method = RequestMethod.GET, produces =
	 * "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String>
	 * undoTaskPeriodical(@PathVariable(value = "task_id") Long task_id) {
	 * 
	 * Task toSet = taskDAO.findOne(task_id);
	 * 
	 * if (toSet.getTaskRepetitionState() != TaskRepetitionState.PERIODIC) {
	 * return JSonResponseHelper.actionNotPossible("task is not periodic"); }
	 * else { toSet.setTaskRepetitionState(TaskRepetitionState.ONCE);
	 * RepetitionDate ord = toSet.getRepetitionDate();
	 * toSet.setRepetitionDate(null); repetitionDateDAO.delete(ord);
	 * taskDAO.save(toSet); return
	 * JSonResponseHelper.successFullAction("task set to once"); }
	 * 
	 * }
	 */

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

			if (task.getTaskState() == TaskState.STARTED) {

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
									ErrorCause.STATE_NOT_AVAILABLE, meta);
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
							TaskDoneNotification n = new TaskDoneNotification(task_id, user.getId());
							NotificationHelper.createNotification(n);
							// TaskDoneNotification(task_id, user.getId());
							// NotificationHelper.createTaskDone(task_id,
							// user.getId());
						}

						return JSONResponseHelper.successfullyUpdated(task);
					}
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.USER_NOT_PARTICIPATING);
				}
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_NOT_STARTED);
			}
		}

		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);

	}

	/**
	 * Change the state of target task, for each state different prerequisites
	 * have to be fullfilled:
	 * 
	 * What exactly they are can be read in the notes from 28.11.
	 * 
	 * @param task_id
	 * @param stateName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{task_id}/state/{state_name}",
			"/{task_id}/state/{state_name}/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> changeTaskState(@PathVariable(value = "task_id") Long task_id,
			@PathVariable(value = "state_name") String stateName) {

		Task task = taskDAO.findOne(task_id);

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		if (user.hasTaskPermissions(task)) {

			if (task != null) {

				TaskState oldState = task.getTaskState();

				TaskState state = TaskState.NOT_PUBLISHED;
				int s = 0;

				if (stateName.equals("publish")) {
					s = task.publish();
					switch (s) {
					case 1:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_NOT_READY);
					case 2:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.CHILDREN_NOT_READY);
					case 3:
						state = TaskState.PUBLISHED;
						break;
					default:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.UNDEFINED_ERROR);
					}

				} else if (stateName.equals("start")) {

					s = task.start();
					switch (s) {
					case 1:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_NOT_READY);
					case 2:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.START_NOT_ALLOWED);
					case 3:
						state = TaskState.STARTED;
						break;
					default:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.UNDEFINED_ERROR);
					}

				} else if (stateName.equals("complete")) {

					s = task.complete();
					switch (s) {
					case 1:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_NOT_READY);
					case 2:
						return JSONResponseHelper.createResponse(false, "bad_request",
								ErrorCause.NOT_COMPLETED_BY_USERS);
					case 3:
						state = TaskState.COMPLETED;
						break;
					default:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.UNDEFINED_ERROR);
					}

				} else if (stateName.equals("unpublish")) {

					s = task.unpublish();
					switch (s) {
					case 3:
						state = TaskState.NOT_PUBLISHED;
						break;
					default:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.UNDEFINED_ERROR);
					}

				} else if (stateName.equals("forceComplete")) {

					s = task.forceComplete();
					switch (s) {
					case 1:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.TASK_NOT_READY);
					case 2:
						return JSONResponseHelper.createResponse(false, "bad_request",
								ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
					case 3:
						state = TaskState.COMPLETED;
						break;
					default:
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.UNDEFINED_ERROR);
					}

				} else {
					HashMap<String, Object> meta = new HashMap<>();
					meta.put("state", stateName);
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.STATE_NOT_AVAILABLE,
							meta);
				}

				task.setTaskState(state);
				taskDAO.save(task);
				HashMap<String, Object> meta = new HashMap<>();
				meta.put("old_state", oldState.toString());
				meta.put("new_state", state.toString());
				return JSONResponseHelper.createResponse(true, meta);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
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

	/*
	 * private void adjustTaskTime(Task t, RepetitionDate repetitionTime) {
	 * Calendar start = t.getStartTime(); Calendar end = t.getEndTime();
	 * 
	 * while (start.getTimeInMillis() <
	 * Calendar.getInstance().getTimeInMillis()) { start.set(Calendar.YEAR,
	 * start.get(Calendar.YEAR) + repetitionTime.getYear());
	 * start.set(Calendar.MONTH, start.get(Calendar.MONTH) +
	 * repetitionTime.getMonth()); start.set(Calendar.DAY_OF_MONTH,
	 * start.get(Calendar.DAY_OF_MONTH) + repetitionTime.getDay());
	 * start.set(Calendar.HOUR, start.get(Calendar.HOUR) +
	 * repetitionTime.getHour()); start.set(Calendar.MINUTE,
	 * start.get(Calendar.MINUTE) + repetitionTime.getMinute());
	 * 
	 * end.set(Calendar.YEAR, end.get(Calendar.YEAR) +
	 * repetitionTime.getYear()); end.set(Calendar.MONTH,
	 * end.get(Calendar.MONTH) + repetitionTime.getMonth());
	 * end.set(Calendar.DAY_OF_MONTH, end.get(Calendar.DAY_OF_MONTH) +
	 * repetitionTime.getDay()); end.set(Calendar.HOUR, end.get(Calendar.HOUR) +
	 * repetitionTime.getHour()); end.set(Calendar.MINUTE,
	 * end.get(Calendar.MINUTE) + repetitionTime.getMinute()); }
	 * 
	 * taskDAO.save(t);
	 * 
	 * for (Task child : t.getChildTasks()) { adjustTaskTime(child,
	 * repetitionTime); }
	 * 
	 * }
	 */

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

				LeadNomination n = new LeadNomination(loggedU.getId(), targetU.getId(), task.getId());
				NotificationHelper.createNotification(n);
				// NotificationHelper.createLeadNomination(loggedU.getId(),
				// targetU.getId(), task.getId());
				return JSONResponseHelper.successfullyCreated(n);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.RESOURCE_UNCHANGEABLE);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser logged = userDAO.findByName(userDetails.getName());

		UserTaskRel utr = userTaskRelDAO.findByUserAndTaskAndParticipationTypeNot(logged, taskDAO.findOne(taskId),
				TaskParticipationType.LEADING);

		if (utr != null) {
			if (utr.getParticipationType() == TaskParticipationType.LEADING) {
				TaskInvitation n = new TaskInvitation(logged.getId(), userId, taskId);
				NotificationHelper.createNotification(n);
				// NotificationHelper.createTaskInvitation(logged.getId(),
				// userId, taskId);
				return JSONResponseHelper.successfullyCreated(n);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
		return JSONResponseHelper.createResponse(TaskState.values(), true);
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
		return JSONResponseHelper.createResponse(taskDAO.findBySuperTaskNullAndTaskStateNot(TaskState.NOT_PUBLISHED),
				true);
	}

	/**
	 * Fulltext-queries all tasks with Elasticsearch and returns the found ones.
	 * If bound to competence-system, compares if tasks are doable
	 * 
	 * @param json
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/elastic/query",
			"/elastic/query/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> queryES(@RequestBody String json) {

		ObjectMapper mapper = new ObjectMapper();

		PostOptions query;
		try {
			query = mapper.readValue(json, PostOptions.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		ArrayList<EvaluatedTask> et = DataAccess.getConnector(Task.class).query(query.getText(), taskDAO);

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Decider unit = new Decider();

		ArrayList<EvaluatedTask> doables = unit.findTasks(user, new UserFilterParameters());

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
	 * Looks up, if the task is allowed to be published
	 * 
	 * @param t
	 * @return boolean
	 */
	/*
	 * private boolean allowPublish(Task t) {
	 * 
	 * if (t.getAmountOfVolunteers() > 0 && !t.getDescription().equals("") &&
	 * t.getStartTime() != null && t.getEndTime() != null &&
	 * !t.getMappedCompetences().isEmpty() && !t.getLocation().equals("")) {
	 * return true; } return false;
	 * 
	 * }
	 */

	/**
	 * Looks up, if the task is allowed to be started
	 * 
	 * @param t
	 * @return boolean
	 */
	/*
	 * private boolean allowStart(Task t) {
	 * 
	 * Task parent = t.getSuperTask();
	 * 
	 * boolean startedParent = true;
	 * 
	 * if (parent != null) { startedParent = t.getSuperTask().getTaskState() ==
	 * TaskState.STARTED; }
	 * 
	 * if (t.getTaskType() == TaskType.SEQUENTIAL) { return previousTaskDone(t)
	 * && startedParent; } else { return startedParent; }
	 * 
	 * }
	 */
	/**
	 * Looks up, if the previous task is done, if there is one
	 * 
	 * @param t
	 * @return boolean
	 */
	/*
	 * private boolean previousTaskDone(Task t) { if (t.getTaskType() ==
	 * TaskType.SEQUENTIAL) { if (t.getPreviousTask().getTaskState() ==
	 * TaskState.COMPLETED) { return true; } } return false; }
	 */
	/**
	 * Looks up, if the child-tasks (if there are some) are all completed. If
	 * there are none, returns always true
	 * 
	 * @param t
	 * @return boolean
	 */
	/*
	 * private boolean childrenDone(Task t) { boolean childrenDone = true;
	 * 
	 * Set<Task> children = t.getChildTasks();
	 * 
	 * if (children != null) { for (Task ct : t.getChildTasks()) { if
	 * (ct.getTaskState() != TaskState.COMPLETED) { childrenDone = false; } }
	 * 
	 * return childrenDone; // return true; } else { return true; }
	 * 
	 * }
	 */
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
		myTask.getAttachments().remove(myComment);
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
