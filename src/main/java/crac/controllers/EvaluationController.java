package crac.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import crac.daos.CracUserDAO;
import crac.daos.EvaluationDAO;
import crac.daos.TaskDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.daos.UserRelationshipDAO;
import crac.daos.UserTaskRelDAO;
import crac.enums.TaskParticipationType;
import crac.enums.TaskRepetitionState;
import crac.enums.TaskState;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Evaluation;
import crac.models.Task;
import crac.notifier.NotificationHelper;
import crac.notifier.notifications.EvaluationNotification;
import crac.relationmodels.CompetenceTaskRel;
import crac.relationmodels.UserCompetenceRel;
import crac.relationmodels.UserRelationship;
import crac.relationmodels.UserTaskRel;
import crac.utility.JSonResponseHelper;
import crac.utility.UpdateEntitiesHelper;

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
			"/task/{task_id}/self/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> createSelfEvaluation(@PathVariable(value = "task_id") Long taskId) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());
		Task task = taskDAO.findOne(taskId);

		if (user != null && task != null && userTaskRelDAO.findByUserAndTask(user, task) != null
				&& task.getTaskState() == TaskState.COMPLETED) {
			Evaluation e = new Evaluation(user, task);
			EvaluationNotification es = NotificationHelper.createEvaluation(user.getId(), task.getId(), e.getId());
			e.setNotificationId(es.getNotificationId());
			evaluationDAO.save(e);
			es.setEvaluationIdy(e.getId());
			return JSonResponseHelper.successFullyCreated(e);
		} else {
			return JSonResponseHelper.idNotFound();
		}
	}

	/**
	 * Creates an evaluation (notification + entity) for every user,
	 * participating in target task
	 * 
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/task/{task_id}/all",
			"/task/{task_id}/all/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> createTaskEvaluations(@PathVariable(value = "task_id") Long taskId) {

		Task task = taskDAO.findOne(taskId);

		if (task != null) {
			if (task.getTaskRepetitionState() == TaskRepetitionState.PERIODIC
					|| task.getTaskState() == TaskState.COMPLETED) {
				CracUser user = null;
				EvaluationNotification es = null;

				for (UserTaskRel utr : task.getUserRelationships()) {
					if (utr.getParticipationType() == TaskParticipationType.PARTICIPATING) {
						user = utr.getUser();
						Evaluation e = new Evaluation(user, task);
						es = NotificationHelper.createEvaluation(user.getId(), task.getId(), e.getId());
						e.setNotificationId(es.getNotificationId());
						evaluationDAO.save(e);
						es.setEvaluationIdy(e.getId());
					}
				}
				return JSonResponseHelper.successfullySent();
			} else {
				return JSonResponseHelper.actionNotPossible("wrong type of task");
			}
		} else {
			return JSonResponseHelper.idNotFound();
		}
	}

	/**
	 * Creates an evaluation (notification + entity) for target user,
	 * participating in target task
	 * 
	 * @param userId
	 * @param taskId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/task/{task_id}/user/{user_id}",
			"/task/{task_id}/user/{user_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> createUserEvaluation(@PathVariable(value = "user_id") Long userId,
			@PathVariable(value = "task_id") Long taskId) {
		CracUser user = userDAO.findOne(userId);
		Task task = taskDAO.findOne(taskId);

		if (user != null && task != null && userTaskRelDAO.findByUserAndTask(user, task) != null
				&& task.getTaskState() == TaskState.COMPLETED) {
			Evaluation e = new Evaluation(user, task);
			EvaluationNotification es = NotificationHelper.createEvaluation(user.getId(), task.getId(), e.getId());
			e.setNotificationId(es.getNotificationId());
			evaluationDAO.save(e);
			es.setEvaluationIdy(e.getId());
			return JSonResponseHelper.successFullyCreated(e);
		} else {
			return JSonResponseHelper.idNotFound();
		}
	}

	/**
	 * Resolves the evaluation. Updates the empty evaluation with sent data and
	 * deletes the notification.
	 * 
	 * @param json
	 * @param evaluationId
	 * @return
	 */
	@RequestMapping(value = { "/{evaluation_id}",
			"/{evaluation_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> selfEvaluate(@RequestBody String json,
			@PathVariable(value = "evaluation_id") Long evaluationId) {

		Evaluation originalEval = evaluationDAO.findOne(evaluationId);
		if (originalEval != null) {
			
			if(originalEval.isFilled()){
				return JSonResponseHelper.actionNotPossible("this evaluation is already filled");
			}

			ObjectMapper mapper = new ObjectMapper();
			Evaluation newEval;
			try {
				newEval = mapper.readValue(json, Evaluation.class);
			} catch (JsonMappingException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.jsonMapError();
			} catch (IOException e) {
				System.out.println(e.toString());
				return JSonResponseHelper.jsonReadError();
			}

			String notificationId = originalEval.getNotificationId();
			UpdateEntitiesHelper.checkAndUpdateEvaluation(originalEval, newEval);
			originalEval.setFilled(true);
			postProcessEvaluation(originalEval);
			originalEval.setNotificationId("deleted");
			evaluationDAO.save(originalEval);
			NotificationHelper.deleteNotification(notificationId);
			return JSonResponseHelper.successfullEvaluation();

		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

	//TODO redo when search is done
	private void postProcessEvaluation(Evaluation e) {
		Task task = e.getTask();

		for (CompetenceTaskRel c : task.getMappedCompetences()) {
			UserCompetenceRel uc = userCompetenceRelDAO.findByUserAndCompetence(e.getUser(), c.getCompetence());

			if (uc != null) {
				//uc.setLikeValue(uc.getLikeValue() * adjustValues(e.getLikeValTask()));
			}

			userCompetenceRelDAO.save(uc);

		}

		for (UserTaskRel utr : task.getUserRelationships()) {
			UserRelationship ur = userRelationshipDAO.findByC1AndC2(e.getUser(), utr.getUser());

			if (ur != null) {
				ur.setLikeValue(ur.getLikeValue() * adjustValues(e.getLikeValOthers()));
			} else {
				ur = new UserRelationship();
				ur.setC1(e.getUser());
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
