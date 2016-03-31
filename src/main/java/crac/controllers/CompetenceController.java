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

import crac.daos.CompetenceDAO;
import crac.daos.CracUserDAO;
import crac.models.Competence;
import crac.models.Task;
import crac.models.CracUser;

@RestController
@RequestMapping("/competence")
public class CompetenceController {
	 @Autowired
	  private CompetenceDAO competenceDAO;
	 
	 @Autowired
	  private CracUserDAO userDAO;

		@RequestMapping(value = "/", method = RequestMethod.GET, produces="application/json")
		@ResponseBody
		public String index() {
			Iterable<Competence> competenceList = new ArrayList<Competence>();
		    try {
		    	competenceList = competenceDAO.findAll();
		    }
		    catch (Exception ex) {
		      System.out.println("Error fetching the competences: " + ex.toString());
		    }
		    
		    String jsonInString = null;
		    
		    ObjectMapper mapper = new ObjectMapper();
		    try {
				jsonInString = mapper.writeValueAsString(competenceList);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    return jsonInString;
		  }
		
		@RequestMapping(value = "/{competence_id}", method = RequestMethod.GET, produces="application/json")
		@ResponseBody
		public String show(@PathVariable(value="competence_id") Long id) {
			
			Competence myCompetence = null;
			ObjectMapper mapper = new ObjectMapper();		
			String jsonInString = null;
			
			try {
				myCompetence = competenceDAO.findOne(id);
		    }
		    catch (Exception ex) {
		      System.out.println("Error fetching the competence: " + ex.toString());
		    }
			
		    try {
				jsonInString = mapper.writeValueAsString(myCompetence);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return jsonInString;
		}
		
		@RequestMapping(value = "/", method = RequestMethod.POST, produces="application/json", consumes="application/json")
		@ResponseBody
		public String create(@RequestBody String json) {
			ObjectMapper mapper = new ObjectMapper();
			Competence myCompetence = null;
			try {
				myCompetence = mapper.readValue(json, Competence.class);
				competenceDAO.save(myCompetence);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return "{\"created\":\"true\"}";
			
		}
		
		@RequestMapping(value = "/{competence_id}", method = RequestMethod.DELETE, produces="application/json")
		@ResponseBody
		public String destroy(@PathVariable(value="competence_id") Long id) {
			Competence deleteCompetence = null;
			try {
				deleteCompetence = competenceDAO.findOne(id);
				competenceDAO.delete(deleteCompetence);
		    }
		    catch (Exception ex) {
		      System.out.println("Error deleting the task: " + ex.toString());
		    }
			
			return "{\"deleted\":\"true\"}";
			
		}

		@RequestMapping(value = "/{competence_id}", method = RequestMethod.PUT, produces="application/json", consumes="application/json")
		@ResponseBody
		public String update(@RequestBody String json, @PathVariable(value="competence_id") Long id) {
			ObjectMapper mapper = new ObjectMapper();
			Competence updatedCompetence = null;
			Competence oldCompetence = null;
			try {
				updatedCompetence = mapper.readValue(json, Competence.class);
				oldCompetence = competenceDAO.findOne(id);
				
				if(updatedCompetence.getName() != null){oldCompetence.setName(updatedCompetence.getName());}
				
				competenceDAO.save(oldCompetence);
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
