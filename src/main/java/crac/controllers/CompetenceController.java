package crac.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import crac.daos.CompetenceDAO;
import crac.daos.UserDAO;
import crac.models.Competence;
import crac.models.User;

@Controller
@RequestMapping("/competence")
public class CompetenceController {
	 @Autowired
	  private CompetenceDAO competenceDAO;
	 
	 @Autowired
	  private UserDAO userDAO;

	  @RequestMapping("/create")
	  @ResponseBody
	  public String create(String name) {
		  User myUser = userDAO.findOne(1L);
		  Competence myCompetence = new Competence(name);
		  
		  myCompetence.setCreator(myUser);
		  myUser.getCreated_competences().add(myCompetence);
		  
		  competenceDAO.save(myCompetence);
		  userDAO.save(myUser);
		  
		  return myCompetence.getName();
	  }
}
