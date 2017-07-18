package crac.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

import crac.components.matching.Decider;
import crac.components.notifier.NotificationHelper;
import crac.components.notifier.notifications.EvaluationNotification;
import crac.components.utility.JSONResponseHelper;
import crac.components.utility.UpdateEntitiesHelper;
import crac.enums.ErrorCause;
import crac.enums.TaskParticipationType;
import crac.enums.TaskRepetitionState;
import crac.enums.TaskState;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.EvaluationDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import crac.models.db.relation.UserRelationship;
import crac.models.db.relation.UserTaskRel;
import crac.models.output.OpenEvaluation;

@RestController
@RequestMapping("/evaluation")
public class EvaluationController {

	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private EvaluationDAO evaluationDAO;

	@Autowired
	private UserTaskRelDAO userTaskRelDAO;

	@Autowired
	UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	UserRelationshipDAO userRelationshipDAO;

	@Value("${crac.eval.decreaseValues}")
	private int decreaseValuesFactor;

	/**
	 * Creates an evaluation (notification + entity) for the logged in user for
	 * target task
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/task/{task_id}/self",
			"/task/{task_id}/self/" }, method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> createSelfEvaluation(@PathVariable(value = "task_id") Long taskId) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		Task task = taskDAO.findOne(taskId);

		if (task != null) {
			UserTaskRel utr = userTaskRelDAO.findByUserAndTaskAndParticipationType(user, task,
					TaskParticipationType.PARTICIPATING);

			if (utr != null) {
				if (task.getTaskState() == TaskState.COMPLETED) {
					if (!utr.isEvalTriggered()) {
						return JSONResponseHelper.successfullyCreated(utr.triggerEval());
					} else {
						return JSONResponseHelper.createResponse(false, "bad_request",
								ErrorCause.DATASETS_ALREADY_EXISTS);
					}
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.STATE_NOT_AVAILABLE);
				}
			}
		}

		return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);

	}

	/**
	 * Creates an evaluation (notification + entity) for every user,
	 * participating in target task
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/task/{task_id}/all",
			"/task/{task_id}/all/" }, method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> createTaskEvaluations(@PathVariable(value = "task_id") Long taskId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Task task = taskDAO.findOne(taskId);

		if (task != null) {
			if (user.hasTaskPermissions(task)) {
				if (task.getTaskState() == TaskState.COMPLETED) {
					boolean allTriggered = true;
					for (UserTaskRel utr : task.getUserRelationships()) {
						if (utr.getParticipationType() == TaskParticipationType.PARTICIPATING
								&& !utr.isEvalTriggered()) {
							allTriggered = false;
							utr.triggerEval();
						}
					}
					if(allTriggered){
						return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ALL_EVALS_TRIGGERED);
					}
					return JSONResponseHelper.successfullyUpdated(task);
				} else {
					return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.WRONG_TYPE);
				}
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}
	}

	/**
	 * Returns all evaluations of the logged in user
	 * 
	 * @return
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getEvaluations() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		
		Set<UserTaskRel> rels = userTaskRelDAO.blub(user);
		HashSet<OpenEvaluation> evals = new HashSet<>();
		
		if(rels != null){
			for(UserTaskRel rel : rels){
				evals.add(new OpenEvaluation(rel));
			}
		}
		
		return JSONResponseHelper.createResponse(evals, true);
	}

	/**
	 * Creates an evaluation (notification + entity) for target user,
	 * participating in target task
	 * 
	 * @param userId
	 * @param taskId
	 * @return ResponseEntity
	 */
	/*
	 * @RequestMapping(value = { "/task/{task_id}/user/{user_id}",
	 * "/task/{task_id}/user/{user_id}/" }, method = RequestMethod.GET, produces
	 * = "application/json")
	 * 
	 * @ResponseBody public ResponseEntity<String>
	 * createUserEvaluation(@PathVariable(value = "user_id") Long userId,
	 * 
	 * @PathVariable(value = "task_id") Long taskId) { CracUser user =
	 * userDAO.findOne(userId); Task task = taskDAO.findOne(taskId);
	 * 
	 * if (user != null && task != null &&
	 * userTaskRelDAO.findByUserAndTaskAndParticipationTypeNot(user, task,
	 * TaskParticipationType.LEADING) != null && task.getTaskState() ==
	 * TaskState.COMPLETED) { Evaluation e = new Evaluation(user, task);
	 * EvaluationNotification es = new EvaluationNotification(user.getId(),
	 * task.getId(), e.getId()); NotificationHelper.createNotification(es);
	 * e.setNotificationId(es.getNotificationId()); evaluationDAO.save(e);
	 * es.setEvaluationIdy(e.getId()); return
	 * JSONResponseHelper.successfullyCreated(e); } else { return
	 * JSONResponseHelper.createResponse(false, "bad_request",
	 * ErrorCause.ID_NOT_FOUND); } }
	 */

	/**
	 * Resolves the evaluation. Updates the empty evaluation with sent data and
	 * deletes the notification.
	 * 
	 * @param json
	 * @param evaluationId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{evaluation_id}",
			"/{evaluation_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> evaluateTask(@RequestBody String json,
			@PathVariable(value = "evaluation_id") Long evaluationId) {

		Evaluation originalEval = evaluationDAO.findOne(evaluationId);
		if (originalEval != null) {

			if (originalEval.isFilled()) {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ALREADY_FILLED);
			}

			ObjectMapper mapper = new ObjectMapper();
			Evaluation newEval;
			try {
				newEval = mapper.readValue(json, Evaluation.class);
			} catch (JsonMappingException e) {
				System.out.println(e.toString());
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
			} catch (IOException e) {
				System.out.println(e.toString());
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
			}

			String notificationId = originalEval.getNotificationId();
			UpdateEntitiesHelper.checkAndUpdateEvaluation(originalEval, newEval);

			Decider unit = new Decider();

			unit.evaluateUsers(originalEval);
			unit.evaluateTask(originalEval);

			originalEval.setNotificationId("deleted");
			UserTaskRel utr = userTaskRelDAO.findByUserAndTaskAndParticipationType(
					originalEval.getUserTaskRel().getUser(), originalEval.getUserTaskRel().getTask(),
					TaskParticipationType.PARTICIPATING);
			originalEval.setFilled(true);
			evaluationDAO.save(originalEval);
			NotificationHelper.deleteNotification(notificationId);
			return JSONResponseHelper.successfullyCreated(originalEval);

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	// TODO redo when search is done
	private void postProcessEvaluation(Evaluation e) {
		Task task = e.getUserTaskRel().getTask();

		for (CompetenceTaskRel c : task.getMappedCompetences()) {
			UserCompetenceRel uc = userCompetenceRelDAO.findByUserAndCompetence(e.getUserTaskRel().getUser(),
					c.getCompetence());

			if (uc != null) {
				// uc.setLikeValue(uc.getLikeValue() *
				// adjustValues(e.getLikeValTask()));
			}

			userCompetenceRelDAO.save(uc);

		}

		for (UserTaskRel utr : task.getUserRelationships()) {
			UserRelationship ur = userRelationshipDAO.findByC1AndC2(e.getUserTaskRel().getUser(), utr.getUser());

			if (ur != null) {
				ur.setLikeValue(ur.getLikeValue() * adjustValues(e.getLikeValOthers()));
			} else {
				ur = new UserRelationship();
				ur.setC1(e.getUserTaskRel().getUser());
				ur.setC2(utr.getUser());
				ur.setFriends(false);
				ur.setLikeValue(adjustValues(e.getLikeValOthers()));
			}

			userRelationshipDAO.save(ur);

		}

	}

	private double adjustValues(double val) {
		double result = val;

		for (int i = 0; i < decreaseValuesFactor; i++) {
			result = (result + 1) / 2;
		}

		return result;
	}

}
