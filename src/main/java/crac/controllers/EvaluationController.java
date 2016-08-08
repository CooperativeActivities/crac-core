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

import crac.daos.CracUserDAO;
import crac.daos.EvaluationDAO;
import crac.daos.TaskDAO;
import crac.daos.UserTaskRelDAO;
import crac.enums.TaskState;
import crac.models.CracUser;
import crac.models.Evaluation;
import crac.models.Task;
import crac.notifier.NotificationHelper;
import crac.notifier.notifications.SelfEvaluation;
import crac.utility.JSonResponseHelper;

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

	@RequestMapping(value = { "/task/{task_id}",
			"/task/{task_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> createSelfEvaluation(@PathVariable(value = "task_id") Long taskId) {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());
		Task task = taskDAO.findOne(taskId);

		if (user != null && task != null && userTaskRelDAO.findByUserAndTask(user, task) != null
				&& task.getTaskState() == TaskState.COMPLETED) {
			Evaluation e = new Evaluation(user);
			SelfEvaluation es = NotificationHelper.createSelfEvaluation(user, task, e);
			e.setNotificationId(es.getNotificationId());
			evaluationDAO.save(e);
			es.setEvaluationIdy(e.getId());
			return JSonResponseHelper.successFullyCreated(e);
		} else {
			return JSonResponseHelper.idNotFound();
		}
	}

	@RequestMapping(value = { "/{evaluation_id}",
			"/{evaluation_id}/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> selfEvaluate(@RequestBody String json,
			@PathVariable(value = "evaluation_id") Long evaluationId) {

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

		Evaluation originalEval = evaluationDAO.findOne(evaluationId);
		if (originalEval != null) {
			String notificationId = originalEval.getNotificationId();
			originalEval.setFeedback(newEval.getFeedback());
			originalEval.setFilled(true);
			originalEval.setGrade(newEval.getGrade());
			originalEval.setLikeVal(newEval.getLikeVal());
			originalEval.setNotificationId("deleted");
			evaluationDAO.save(originalEval);
			NotificationHelper.deleteNotification(notificationId);
			return JSonResponseHelper.successfullEvaluation();
		} else {
			return JSonResponseHelper.idNotFound();
		}

	}

}
