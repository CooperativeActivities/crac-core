package crac.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import crac.daos.TaskDAO;
import crac.daos.UserDAO;
import crac.models.Task;
import crac.models.User;

@Controller
@RequestMapping("/task")
public class TaskController {
	
	  @Autowired
	  private TaskDAO taskDAO;
	  
	  @Autowired
	  private UserDAO userDAO;

	  @RequestMapping("/create")
	  @ResponseBody
	  public String create(String name) {
		User myUser = userDAO.findOne(1L);
	    Task myTask = new Task(name);
	    
	    myTask.setCreator(myUser);
	    myUser.getCreated_tasks().add(myTask);
	    
	    taskDAO.save(myTask);
	    userDAO.save(myUser);
	    
	    return myTask.getName();
	  }

}
