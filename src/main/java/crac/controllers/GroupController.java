package crac.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

import crac.enums.ErrorCause;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.GroupDAO;
import crac.models.db.entities.CracUser;
import crac.models.input.PostOptions;
import crac.module.utility.JSONResponseHelper;
import crac.models.db.entities.CracGroup;

/**
 * REST controller for managing groups.
 */
@RestController
@RequestMapping("/group")
public class GroupController {
	@Autowired
	private GroupDAO groupDAO;

	@Autowired
	private CracUserDAO userDAO;

	/**
	 * Get all groups
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() {
		return JSONResponseHelper.createResponse(groupDAO.findAll(), true);
	}

	/**
	 * Get the group with given ID
	 */
	@RequestMapping(value = "/{group_id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "group_id") Long id) {
		return JSONResponseHelper.createResponse(groupDAO.findOne(id), true);
	}

	/**
	 * Create a new group, creator is the logged-in user
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/",
			"" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> create(@RequestBody String json) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		ObjectMapper mapper = new ObjectMapper();
		CracGroup myGroup;
		try {
			myGroup = mapper.readValue(json, CracGroup.class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}
		myGroup.setCreator(user);
		groupDAO.save(myGroup);

		return JSONResponseHelper.successfullyCreated(myGroup);

	}

	/**
	 * Delete the group with given ID
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/{group_id}",
			"/{group_id}/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> destroy(@PathVariable(value = "group_id") Long id) {
		CracGroup deleteGroup = groupDAO.findOne(id);
		ResponseEntity<String> v = JSONResponseHelper.successfullyDeleted(deleteGroup);
		groupDAO.delete(deleteGroup);
		return v;
	}

	/**
	 * Update the group with given ID
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/{group_id}",
			"/{group_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> update(@RequestBody String json, @PathVariable(value = "group_id") Long id)
			throws JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		CracGroup updatedGroup = mapper.readValue(json, CracGroup.class);
		CracGroup oldGroup = groupDAO.findOne(id);
		oldGroup = updatedGroup;

		groupDAO.save(oldGroup);

		return JSONResponseHelper.successfullyUpdated(oldGroup);

	}

	/**
	 * Add multiple users to a group
	 * @param json
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/{group_id}/add/multiple",
			"/{group_id}/add/multiple/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addMultiple(@RequestBody String json, @PathVariable(value = "group_id") Long id) {
		ObjectMapper mapper = new ObjectMapper();
		PostOptions[] mappings = null;
		try {
			mappings = mapper.readValue(json, PostOptions[].class);
		} catch (JsonMappingException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_MAP_ERROR);
		} catch (IOException e) {
			System.out.println(e.toString());
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.JSON_READ_ERROR);
		}
		CracGroup g = groupDAO.findOne(id);

		for (PostOptions p : mappings) {
			CracUser u = userDAO.findByIdAndName(p.getId(), p.getName());
			if (u != null) {
				g.addUser(u);
			}
		}

		groupDAO.save(g);

		return JSONResponseHelper.successfullyUpdated(g);

	}

}
