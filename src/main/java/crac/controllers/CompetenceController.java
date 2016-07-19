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
	 * GET / or blank -> get all competences.
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() throws JsonProcessingException {
		Iterable<Competence> competenceList = competenceDAO.findAll();
		ObjectMapper mapper = new ObjectMapper();
		return ResponseEntity.ok().body(mapper.writeValueAsString(competenceList));
	}

	/**
	 * GET /{competence_id} -> get the competence with given ID.
	 */
	@RequestMapping(value = "/{competence_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "competence_id") Long id) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Competence myCompetence = competenceDAO.findOne(id);
		return ResponseEntity.ok().body(mapper.writeValueAsString(myCompetence));
	}

	/**
	 * POST / or blank -> create a new competence, creator is the logged-in user.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/", "" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> create(@RequestBody String json) throws JsonMappingException, IOException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());		
		ObjectMapper mapper = new ObjectMapper();
		Competence myCompetence = mapper.readValue(json, Competence.class);
		myCompetence.setCreator(myUser);
		competenceDAO.save(myCompetence);

		return ResponseEntity.ok().body("{\"created\":\"true\"}");

	}

	/**
	 * DELETE /{competence_id} -> delete the competence with given ID.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = "/{competence_id}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> destroy(@PathVariable(value = "competence_id") Long id) {
		Competence deleteCompetence = competenceDAO.findOne(id);
		competenceDAO.delete(deleteCompetence);
		return ResponseEntity.ok().body("{\"deleted\":\"true\"}");

	}

	/**
	 * PUT /{competence_id} -> update the competence with given ID.
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = "/{competence_id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> update(@RequestBody String json, @PathVariable(value = "competence_id") Long id)
			throws JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Competence updatedCompetence = mapper.readValue(json, Competence.class);
		Competence oldCompetence = competenceDAO.findOne(id);

		if (updatedCompetence.getName() != null) {
			oldCompetence.setName(updatedCompetence.getName());
		}

		competenceDAO.save(oldCompetence);

		return ResponseEntity.ok().body("{\"updated\":\"true\"}");

	}
	
	@RequestMapping(value = "/addType", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> conntect(@RequestBody String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		CompetenceRelationshipType t = mapper.readValue(json, CompetenceRelationshipType.class);
		typeDAO.save(t);
		return ResponseEntity.ok().body("{\"type_created\":\"true\", \"name\":\""+t.getName()+"\"}");
	}

	
	@RequestMapping(value = "/{competence1_id}/conntect/{competence2_id}/type/{type_id}", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> conntect(@RequestBody String json, @PathVariable(value = "competence1_id") Long competence1_id, @PathVariable(value = "competence2_id") Long competence2_id, @PathVariable(value = "type_id") Long type_id) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		CompetenceRelationship cr = mapper.readValue(json, CompetenceRelationship.class);
		Competence c1 = competenceDAO.findOne(competence1_id);
		Competence c2 = competenceDAO.findOne(competence2_id);
		CompetenceRelationshipType crt = typeDAO.findOne(type_id);
		cr.setCompetence1(c1);
		cr.setCompetence2(c2);
		cr.setType(crt);
		relationDAO.save(cr);
		return ResponseEntity.ok().body("{\"relationship_created\":\"true\", \"competence1\":\""+c1.getName()+"\", \"competence2\":\""+c2.getName()+"\", \"type\":\""+crt.getName()+"\"}");
	}

}
