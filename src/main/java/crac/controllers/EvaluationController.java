package crac.controllers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.enums.ErrorCause;
import crac.enums.TaskParticipationType;
import crac.enums.TaskState;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.EvaluationDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;
import crac.models.output.OpenEvaluation;
import crac.module.matching.Decider;
import crac.module.notifier.factory.NotificationFactory;
import crac.module.utility.JSONResponseHelper;

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

	@Autowired
	private Decider decider;

	@Autowired
	private NotificationFactory nf;

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
						return JSONResponseHelper.successfullyCreated(utr.triggerEval(nf));
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
							utr.triggerEval(nf);
						}
					}
					if (allTriggered) {
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

		Set<UserTaskRel> rels = userTaskRelDAO.selectRelByNotFilled(user);
		HashSet<OpenEvaluation> evals = new HashSet<>();

		if (rels != null) {
			for (UserTaskRel rel : rels) {
				evals.add(new OpenEvaluation(rel));
			}
		}

		return JSONResponseHelper.createResponse(evals, true);
	}

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

		Evaluation eval = evaluationDAO.findOne(evaluationId);
		if (eval != null) {

			UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
					.getContext().getAuthentication();
			CracUser user = userDAO.findByName(userDetails.getName());

			if (eval.getUserTaskRel().getUser() != user && !user.confirmRole("ADMIN")) {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.PERMISSIONS_NOT_SUFFICIENT);
			}
			
			if (eval.isFilled()) {
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

			String notificationId = eval.getNotificationId();
			eval.update(newEval);

			decider.evaluate(eval);

			eval.setNotificationId("deleted");
			eval.setFilled(true);
			evaluationDAO.save(eval);

			nf.deleteNotificationById(notificationId);
			return JSONResponseHelper.successfullyCreated(eval);

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

}
