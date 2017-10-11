package crac.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.enums.ErrorCode;
import crac.models.db.daos.CompetenceAreaDAO;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CompetenceArea;
import crac.models.db.entities.CracUser;
import crac.models.db.relation.UserCompetenceRel;
import crac.models.input.PostOptions;
import crac.models.output.CompetenceGraphDetails;
import crac.module.storage.CompetenceStorage;
import crac.module.utility.JSONResponseHelper;

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
	private UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	private CompetenceAreaDAO competenceAreaDAO;

	@Autowired
	private CompetenceStorage cs;

	/**
	 * Returns all competences
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/all", "/all/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() {
		return JSONResponseHelper.createResponse(competenceDAO.findByDeprecatedNot(true), true);
	}

	/**
	 * Get target competence with given id
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{competence_id}",
			"/{competence_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "competence_id") Long id) {
		return JSONResponseHelper.createResponse(competenceDAO.findOne(id), true);
	}

	/**
	 * Returns the competences of the currently logged in user, wrapped in the
	 * relationship-object
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getUserCompetences() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Set<UserCompetenceRel> competenceRels = userCompetenceRelDAO.findByUser(user);

		if (competenceRels.size() != 0) {
			return JSONResponseHelper.createResponse(competenceRels, true);

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.EMPTY_DATA);
		}
	}

	/**
	 * Returns all competences that are related to target competence, ordered by
	 * it's relation-value
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{competence_id}/related",
			"/{competence_id}/related/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> showRelated(@PathVariable(value = "competence_id") Long id) {
		Competence c = competenceDAO.findOne(id);
		List<CompetenceGraphDetails> crd = cs.getCollection(c).getAugmented().stream()
				.filter(ac -> !ac.getConcreteComp().equals(c))
				.map(ac -> new CompetenceGraphDetails(ac))
				.sorted()
				.collect(Collectors.toList());
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("competences", crd);
		return JSONResponseHelper.createResponse(c, true, meta);
	}

	/**
	 * Returns target area and its mapped competences
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/area/{area_id}",
			"/area/{area_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getAreaCompetences(@PathVariable(value = "area_id") Long id) {

		CompetenceArea a = competenceAreaDAO.findOne(id);

		HashMap<String, Object> meta = new HashMap<>();
		meta.put("competences", a.getMappedCompetences());

		return JSONResponseHelper.createResponse(a, true, meta);

	}

	/**
	 * Returns all competence-topic-areas
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/area", "/area/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getArea() {
		return JSONResponseHelper.createResponse(competenceAreaDAO.findByDeprecatedNot(true), true);
	}

	@RequestMapping(value = { "/userrels", "/userrels/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> userrels() {
		return JSONResponseHelper.createResponse(userCompetenceRelDAO.findAll(), true);
	}

	/**
	 * Add a competence with given ID to the currently logged in user, likeValue
	 * and proficiencyValue are mandatory
	 * 
	 * @param competenceId
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/{competence_id}/add",
			"/{competence_id}/add/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addCompetence(@PathVariable(value = "competence_id") Long competenceId,
			@RequestBody String json) throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		PostOptions po;

		po = mapper.readValue(json, PostOptions.class);

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		UserCompetenceRel rel = new UserCompetenceRel();

		Competence competence = competenceDAO.findOne(competenceId);

		if (competence != null) {
			UserCompetenceRel ucr = userCompetenceRelDAO.findByUserAndCompetence(user, competence);
			if (ucr != null) {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.DATASETS_ALREADY_EXISTS);
			} else {
				rel.setUser(user);
				rel.setCompetence(competence);
				rel.setLikeValue(po.getLikeValue());
				rel.setProficiencyValue(po.getProficiencyValue());
				user.getCompetenceRelationships().add(rel);
				ResponseEntity<String> v = JSONResponseHelper.successfullyCreated(rel);
				userDAO.save(user);
				return v;
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Adjust the values of a user-competence connection
	 * 
	 * @param competenceId
	 * @param likeValue
	 * @param proficiencyValue
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/{competence_id}/adjust",
			"/{competence_id}/adjust/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> adjustCompetence(@PathVariable(value = "competence_id") Long competenceId,
			@RequestBody String json) throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		PostOptions po;

		po = mapper.readValue(json, PostOptions.class);

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Competence competence = competenceDAO.findOne(competenceId);

		if (competence != null) {
			UserCompetenceRel ucr = userCompetenceRelDAO.findByUserAndCompetence(user, competence);

			if (ucr != null) {
				ucr.setLikeValue(po.getLikeValue());
				ucr.setProficiencyValue(po.getProficiencyValue());
				ResponseEntity<String> v = JSONResponseHelper.successfullyUpdated(ucr);
				userCompetenceRelDAO.save(ucr);
				return v;
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Removes target competence from the currently logged-in user
	 * 
	 * @param competenceId
	 * @return ResponseEntity
	 */

	@RequestMapping(value = { "/{competence_id}/remove",
			"/{competence_id}/remove/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeCompetence(@PathVariable(value = "competence_id") Long competenceId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Competence competence = competenceDAO.findOne(competenceId);

		if (competence != null) {
			UserCompetenceRel rel = userCompetenceRelDAO.findByUserAndCompetence(user, competence);
			if (rel != null) {
				ResponseEntity<String> v = JSONResponseHelper.successfullyDeleted(rel);
				user.getCompetenceRelationships().remove(rel);
				competence.getUserRelationships().remove(rel);
				userCompetenceRelDAO.delete(rel);
				return v;
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	@RequestMapping(value = { "/available", "/available/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getAvailableCompetences() {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		List<Competence> comps = StreamSupport.stream(competenceDAO.findByDeprecatedNot(true).spliterator(), false)
		.filter(c -> user.getCompetenceRelationships().stream()
				.map(UserCompetenceRel::getCompetence)
				.noneMatch(uc -> uc.equals(c)) )
		.collect(Collectors.toList());

		if (comps.size() != 0) {
			return JSONResponseHelper.createResponse(comps, true);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.EMPTY_DATA);
		}

	}

}
