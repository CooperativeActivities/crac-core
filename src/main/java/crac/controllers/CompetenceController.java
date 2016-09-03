package crac.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
import crac.daos.CompetenceRelationshipDAO;
import crac.daos.CompetenceRelationshipTypeDAO;
import crac.daos.CracUserDAO;
import crac.models.Competence;
import crac.models.CracUser;
import crac.relationmodels.CompetenceRelationship;
import crac.relationmodels.CompetenceRelationshipType;
import crac.utility.JSonResponseHelper;

/**
 * REST controller for managing competences.
 */
@RestController
@RequestMapping("/competence")
public class CompetenceController {
	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private CracUserDAO userDAO;
	
	@Autowired
	private CompetenceRelationshipTypeDAO typeDAO;

	@Autowired
	private CompetenceRelationshipDAO relationDAO;


	/**
	 * Returns all competences
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() {
		Iterable<Competence> competenceList = competenceDAO.findAll();
		ObjectMapper mapper = new ObjectMapper();
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(competenceList));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
	}

	/**
	 * Get target competence with given id
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/{competence_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "competence_id") Long id) {
		ObjectMapper mapper = new ObjectMapper();
		Competence myCompetence = competenceDAO.findOne(id);
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(myCompetence));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
	}

	/**
	 * Connects two competences via a type and additional values
	 * @param json
	 * @param competence1_id
	 * @param competence2_id
	 * @param type_id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/{competence1_id}/connect/{competence2_id}/type/{type_id}", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> conntect(@RequestBody String json, @PathVariable(value = "competence1_id") Long competence1_id, @PathVariable(value = "competence2_id") Long competence2_id, @PathVariable(value = "type_id") Long type_id) {
		ObjectMapper mapper = new ObjectMapper();
		CompetenceRelationship cr;
		try {
			cr = mapper.readValue(json, CompetenceRelationship.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonMapError();
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonReadError();
		}
		
		Competence c1 = competenceDAO.findOne(competence1_id);
		Competence c2 = competenceDAO.findOne(competence2_id);
		CompetenceRelationshipType crt = typeDAO.findOne(type_id);
		
		if(c1 != null && c2 != null && crt != null){
			cr.setCompetence1(c1);
			cr.setCompetence2(c2);
			cr.setType(crt);
			relationDAO.save(cr);
			return JSonResponseHelper.successFullyAssigned(crt);
		}else{
			return JSonResponseHelper.idNotFound();
		}
		
	}

}
