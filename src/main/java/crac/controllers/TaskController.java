package crac.controllers;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

import crac.daos.TaskDAO;
import crac.daos.CracUserDAO;
import crac.models.Task;
import crac.models.CracUser;

@RestController
@RequestMapping("/task")
public class TaskController {
	
	  @Autowired
	  private TaskDAO taskDAO;
	  
	  @Autowired
	  private CracUserDAO userDAO;

		@RequestMapping(value = "/", method = RequestMethod.GET, produces="application/json")
		@ResponseBody
		public String index() {
			Iterable<Task> taskList = new ArrayList<Task>();
		    try {
		    	taskList = taskDAO.findAll();
		    }
		    catch (Exception ex) {
		      System.out.println("Error fetching the tasks: " + ex.toString());
		    }
		    
		    String jsonInString = null;
		    
		    ObjectMapper mapper = new ObjectMapper();
		    try {
				jsonInString = mapper.writeValueAsString(taskList);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    return jsonInString;
		  }
		
		@RequestMapping(value = "/{task_id}", method = RequestMethod.GET, produces="application/json")
		@ResponseBody
		public String show(@PathVariable(value="task_id") Long id) {
			
			Task myTask = null;
			ObjectMapper mapper = new ObjectMapper();		
			String jsonInString = null;
			
			try {
				myTask = taskDAO.findOne(id);
		    }
		    catch (Exception ex) {
		      System.out.println("Error fetching the task: " + ex.toString());
		    }
			
		    try {
				jsonInString = mapper.writeValueAsString(myTask);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return jsonInString;
		}
		
		@RequestMapping(value = "/", method = RequestMethod.POST, produces="application/json", consumes="application/json")
		@ResponseBody
		public String create(@RequestBody String json) {
			ObjectMapper mapper = new ObjectMapper();
			Task myTask = null;
			try {
				myTask = mapper.readValue(json, Task.class);
				taskDAO.save(myTask);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return "{\"created\":\"true\"}";
			
		}
		
		@RequestMapping(value = "/{task_id}", method = RequestMethod.DELETE, produces="application/json")
		@ResponseBody
		public String destroy(@PathVariable(value="task_id") Long id) {
			Task deleteTask = null;
			try {
				deleteTask = taskDAO.findOne(id);
				taskDAO.delete(deleteTask);
		    }
		    catch (Exception ex) {
		      System.out.println("Error deleting the task: " + ex.toString());
		    }
			
			return "{\"deleted\":\"true\"}";
			
		}

		@RequestMapping(value = "/{task_id}", method = RequestMethod.PUT, produces="application/json", consumes="application/json")
		@ResponseBody
		public String update(@RequestBody String json, @PathVariable(value="task_id") Long id) {
			ObjectMapper mapper = new ObjectMapper();
			Task updatedTask = null;
			Task oldTask = null;
			try {
				updatedTask = mapper.readValue(json, Task.class);
				oldTask = taskDAO.findOne(id);
				
				if(updatedTask.getName() != null){oldTask.setName(updatedTask.getName());}
				if(updatedTask.getDescription() != null){oldTask.setDescription(updatedTask.getDescription());}
				
				taskDAO.save(oldTask);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return "{\"updated\":\"true\"}";
			
		}


}
