package crac.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.GroupDAO;
import crac.models.db.entities.CracGroup;
import crac.models.db.entities.CracUser;
import crac.models.input.PostOptions;
import crac.module.utility.JSONResponseHelper;

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
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/",
			"" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> create(@RequestBody String json)
			throws JsonParseException, JsonMappingException, IOException {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		ObjectMapper mapper = new ObjectMapper();
		CracGroup myGroup;
		myGroup = mapper.readValue(json, CracGroup.class);
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
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/{group_id}",
			"/{group_id}/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> update(@RequestBody String json, @PathVariable(value = "group_id") Long id)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		CracGroup updatedGroup;
		updatedGroup = mapper.readValue(json, CracGroup.class);
		CracGroup oldGroup = groupDAO.findOne(id);
		oldGroup.update(updatedGroup);

		groupDAO.save(oldGroup);

		return JSONResponseHelper.successfullyUpdated(oldGroup);

	}

	/**
	 * Add multiple users to a group
	 * 
	 * @param json
	 * @param id
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/{group_id}/add/multiple",
			"/{group_id}/add/multiple/" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addMultiple(@RequestBody String json, @PathVariable(value = "group_id") Long id)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		PostOptions[] mappings = null;
		mappings = mapper.readValue(json, PostOptions[].class);
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

	/**
	 * Add target user to a group
	 * 
	 * @param groupId
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/{group_id}/add/user/{user_id}",
			"/{group_id}/add/user/{user_id}/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addUser(@PathVariable(value = "group_id") Long groupId,
			@PathVariable(value = "user_id") Long userId) {

		CracGroup g = groupDAO.findOne(groupId);
		CracUser user = userDAO.findOne(userId);
		g.addUser(user);
		groupDAO.save(g);
		return JSONResponseHelper.successfullyUpdated(user);
	}

	/**
	 * Removes target user from a group
	 * 
	 * @param groupId
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/{group_id}/remove/user/{user_id}",
			"/{group_id}/remove/user/{user_id}/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> leaveGroup(@PathVariable(value = "group_id") Long groupId,
			@PathVariable(value = "user_id") Long userId) {

		CracGroup g = groupDAO.findOne(groupId);
		CracUser user = userDAO.findOne(userId);
		g.removeUser(user);
		groupDAO.save(g);
		return JSONResponseHelper.successfullyUpdated(user);
	}

}
