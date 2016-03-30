package crac.controllers;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
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

import crac.models.Competence;
import crac.models.CompetenceDAO;
import crac.models.Task;
import crac.models.TaskDAO;
import crac.models.User;
import crac.models.UserDAO;

@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	  private UserDAO userDAO;
	 
	 @Autowired
	  private CompetenceDAO competenceDAO;
	 
	 @Autowired
	  private TaskDAO taskDAO;

	@RequestMapping(value = "/", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public String index() {
		Iterable<User> userList = new ArrayList<User>();
	    try {
	    	userList = userDAO.findAll();
	    }
	    catch (Exception ex) {
	      System.out.println("Error creating the user: " + ex.toString());
	    }
	    
	    String jsonInString = null;
	    
	    ObjectMapper mapper = new ObjectMapper();
	    try {
			jsonInString = mapper.writeValueAsString(userList);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return jsonInString;
	  }
	
	@RequestMapping(value = "/{user_id}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public String show(@PathVariable(value="user_id") Long id) {
		
		User myUser = null;
		ObjectMapper mapper = new ObjectMapper();		
		String jsonInString = null;
		
		try {
			myUser = userDAO.findOne(id);
	    }
	    catch (Exception ex) {
	      System.out.println("Error creating the user: " + ex.toString());
	    }
		
	    try {
			jsonInString = mapper.writeValueAsString(myUser);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonInString;
	}
	
	@RequestMapping(value = "/", method = RequestMethod.POST, produces="application/json", consumes="application/json")
	@ResponseBody
	public String create(@RequestBody String json) {
		ObjectMapper mapper = new ObjectMapper();
		User myUser = null;
		try {
			myUser = mapper.readValue(json, User.class);
			userDAO.save(myUser);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "{\"user\":\""+myUser.getName()+"\",\"created\":\"true\"}";
		
	}
	
	/*TODO:Rewrite when login is done
	 * @RequestMapping("/addCompetence")
	  @ResponseBody
	  public String addCompetence(long user, long competence){
		  
		  Competence myCompetence = null;
		  User myUser = null;
		  
		  try {
			  myCompetence = competenceDAO.findOne(competence);
			  myUser = userDAO.findOne(user);
			  myUser.getCompetencies().add(myCompetence);
			  userDAO.save(myUser);
		    }
		    catch (Exception ex) {
		      return "Error: " + ex.toString();
		    }
		    return "Competence "+myCompetence.getName()+" assigned to "+myUser.getName();
	  }
	  
	  @RequestMapping("/addTask")
	  @ResponseBody
	  public String addTask(long user, long task){
		  
		  Task myTask = null;
		  User myUser = null;
		  
		  try {
			  myTask = taskDAO.findOne(task);
			  myUser = userDAO.findOne(user);
			  myUser.getOpenTasks().add(myTask);
			  userDAO.save(myUser);
		    }
		    catch (Exception ex) {
		      return "Error: " + ex.toString();
		    }
		    return "Task "+myTask.getName()+" assigned to "+myUser.getName();
	  }
	  
	  @RequestMapping("/getTasks")
	  @ResponseBody
	  public String getTasks(long user){
		  
		  Set<Task> myTasks = null;
		  
		  try {
			  myTasks = userDAO.findOne(user).getOpenTasks();
		    }
		    catch (Exception ex) {
		      return "Error: " + ex.toString();
		    }
		  String print = "";
		  
			  for (Task s : myTasks) {
				    print += s.getName();
				}
			  
		 return print;
	  }
	  
	  @RequestMapping("/getCompetences")
	  @ResponseBody
	  public String getCompetences(long user){
		  
		  Set<Competence> myCompetencies = null;
		  
		  try {
			  myCompetencies = userDAO.findOne(user).getCompetencies();
		    }
		    catch (Exception ex) {
		      return "Error: " + ex.toString();
		    }
		  String print = "";
		  
			  for (Competence s : myCompetencies) {
				    print += s.getName();
				}
			  
		 return print;
	  }
	  
	  @RequestMapping("/getCreatedTasks")
	  @ResponseBody
	  public String getCreatedTasks(long user){
		  
		  Set<Task> myTasks = null;
		  
		  try {
			  myTasks = userDAO.findOne(user).getCreated_tasks();
		    }
		    catch (Exception ex) {
		      return "Error: " + ex.toString();
		    }
		  
		  String print = "";
		  
		  for (Task s : myTasks) {
			    print += s.getName();
			}
		  
		   return print;
	  }
	  
	  @RequestMapping("/getCreatedCompetences")
	  @ResponseBody
	  public String getCreatedCompetences(long user){
		  
		  Set<Competence> myCompetences = null;
		  
		  try {
			  myCompetences = userDAO.findOne(user).getCreated_competences();
		    }
		    catch (Exception ex) {
		      return "Error: " + ex.toString();
		    }
		  
		  String print = "";
		  
		  for (Competence s : myCompetences) {
			    print += s.getName();
			}
		  
		   return print;
	  }
	 */
	
}
