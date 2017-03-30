package crac.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

import crac.enums.ErrorCause;
import crac.models.db.daos.CompetenceAreaDAO;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CompetenceRelationshipDAO;
import crac.models.db.daos.CompetenceRelationshipTypeDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CompetenceArea;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserCompetenceRel;
import crac.models.input.PostOptions;
import crac.models.output.CompetenceGraphDetails;
import crac.models.storage.AugmentedSimpleCompetence;
import crac.storage.CompetenceStorage;
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

	@Autowired
	UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	CompetenceAreaDAO competenceAreaDAO;

	/**
	 * Returns all competences
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/all", "/all/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() {
		return JSonResponseHelper.createResponse(competenceDAO.findAll(), true);
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
		return JSonResponseHelper.createResponse(competenceDAO.findOne(id), true);
	}

	/**
	 * Returns the competences of the currently logged in user, wrapped in the
	 * relationship-object	 
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
			return JSonResponseHelper.createResponse(competenceRels, true);

		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.EMPTY_DATA);
		}
	}

	/**
	 * Returns all competences that are related to target competence, ordered by it's relation-value
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{competence_id}/related",
			"/{competence_id}/related/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> showRelated(@PathVariable(value = "competence_id") Long id) {
		Competence c = competenceDAO.findOne(id);
		ArrayList<CompetenceGraphDetails> crd = new ArrayList<CompetenceGraphDetails>();
		for (AugmentedSimpleCompetence ac : CompetenceStorage.getCollection(c).getAugmented()) {
			if (ac.getConcreteComp().getId() != c.getId()) {
				crd.add(new CompetenceGraphDetails(ac));
			}
		}
		Collections.sort(crd);
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("competences", crd);
		return JSonResponseHelper.createResponse(c, true, meta);
	}

	/**
	 * Returns target area and its mapped competences
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

		return JSonResponseHelper.createResponse(a, true, meta);

	}

	/**
	 * Returns all competence-topic-areas
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/area", "/area/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody 
	public ResponseEntity<String> getArea() {
		return JSonResponseHelper.createResponse(competenceAreaDAO.findAll(), true);
	}

	@RequestMapping(value = { "/userrels", "/userrels/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> userrels() {
		return JSonResponseHelper.createResponse(userCompetenceRelDAO.findAll(), true);
	}

	/**
	 * Add a competence with given ID to the currently logged in user, likeValue
	 * and proficiencyValue are mandatory
	 * 
	 * @param competenceId
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{competence_id}/add",
			"/{competence_id}/add/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addCompetence(@PathVariable(value = "competence_id") Long competenceId,
			@RequestBody String json) {

		ObjectMapper mapper = new ObjectMapper();

		PostOptions po;

		try {
			po = mapper.readValue(json, PostOptions.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		UserCompetenceRel rel = new UserCompetenceRel();

		Competence competence = competenceDAO.findOne(competenceId);

		if (competence != null) {
			UserCompetenceRel ucr = userCompetenceRelDAO.findByUserAndCompetence(user, competence);
			if (ucr != null) {
				return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.DATASETS_ALREADY_EXISTS);
			} else {
				rel.setUser(user);
				rel.setCompetence(competence);
				rel.setLikeValue(po.getLikeValue());
				rel.setProficiencyValue(po.getProficiencyValue());
				user.getCompetenceRelationships().add(rel);
				ResponseEntity<String> v = JSonResponseHelper.successfullyCreated(rel);
				userDAO.save(user);
				return v;
			}
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	/**
	 * Adjust the values of a user-competence connection
	 * 
	 * @param competenceId
	 * @param likeValue
	 * @param proficiencyValue
	 * @return
	 */
	@RequestMapping(value = { "/{competence_id}/adjust",
			"/{competence_id}/adjust/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> adjustCompetence(@PathVariable(value = "competence_id") Long competenceId,
			@RequestBody String json) {

		ObjectMapper mapper = new ObjectMapper();

		PostOptions po;

		try {
			po = mapper.readValue(json, PostOptions.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Competence competence = competenceDAO.findOne(competenceId);

		if (competence != null) {
			UserCompetenceRel ucr = userCompetenceRelDAO.findByUserAndCompetence(user, competence);

			if (ucr != null) {
				ucr.setLikeValue(po.getLikeValue());
				ucr.setProficiencyValue(po.getProficiencyValue());
				ResponseEntity<String> v = JSonResponseHelper.successfullyUpdated(ucr);
				userCompetenceRelDAO.save(ucr);
				return v;
			} else {
				return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
			}
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
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
				ResponseEntity<String> v = JSonResponseHelper.successfullyDeleted(rel);
				user.getCompetenceRelationships().remove(rel);
				competence.getUserRelationships().remove(rel);
				userCompetenceRelDAO.delete(rel);
				return v;
			} else {
				return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
			}
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.ID_NOT_FOUND);
		}

	}

	@RequestMapping(value = { "/available", "/available/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getAvailableCompetences() {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Iterable<Competence> competenceList = competenceDAO.findAll();

		Set<UserCompetenceRel> competenceRels = user.getCompetenceRelationships();

		ArrayList<Competence> found = new ArrayList<Competence>();

		if (competenceList != null) {
			for (Competence c : competenceList) {
				boolean in = false;
				for (UserCompetenceRel ucr : competenceRels) {
					if (c.getId() == ucr.getCompetence().getId()) {
						in = true;
					}
				}
				if (!in) {
					found.add(c);
				}
			}
		}

		if (found.size() != 0) {
			return JSonResponseHelper.createResponse(found, true);
		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.EMPTY_DATA);
		}

	}

}
