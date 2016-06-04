package crac.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CompetenceDAO;
import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;

@RestController
@RequestMapping("/news")
public class FeedController {

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private CompetenceDAO competenceDAO;

	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() throws JsonProcessingException {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		List<Task> taskList = taskDAO.findByNeededCompetencesIn(myUser.getCompetences());
		for (Task thisTask : taskList ){
			for(Competence thisComp : thisTask.getNeededCompetences()){
				if(!myUser.getCompetences().contains(thisComp)){
					taskList.remove(thisTask);
				}
			}
		}
		for (Task thisTask : myUser.getOpenTasks()) {
			taskList.remove(thisTask);
		}
		for (Task thisTask : myUser.getResponsibleForTasks()) {
			taskList.remove(thisTask);
		}
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(taskList));
	}

}
