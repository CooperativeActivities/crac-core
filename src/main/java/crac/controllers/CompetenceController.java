package crac.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import crac.daos.CracUserDAO;
import crac.models.Competence;

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
	 * POST / -> create a new competence.
	 */
	@RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> create(@RequestBody String json) throws JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Competence myCompetence = mapper.readValue(json, Competence.class);
		competenceDAO.save(myCompetence);

		return ResponseEntity.ok().body("{\"created\":\"true\"}");

	}

	/**
	 * DELETE /{competence_id} -> delete the competence with given ID.
	 */
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

}
