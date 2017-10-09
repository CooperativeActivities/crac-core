package crac.controllers;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.enums.ErrorCode;
import crac.enums.RESTAction;
import crac.exception.InvalidActionException;
import crac.models.db.daos.AttachmentDAO;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.GroupDAO;
import crac.models.db.daos.RoleDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.TokenDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Attachment;
import crac.models.db.entities.CracGroup;
import crac.models.db.entities.CracToken;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Role;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserRelationship;
import crac.models.input.PostOptions;
import crac.models.output.SimpleUserRelationship;
import crac.models.output.UserShort;
import crac.module.matching.Decider;
import crac.module.matching.configuration.UserFilterParameters;
import crac.module.notifier.Notification;
import crac.module.notifier.factory.NotificationFactory;
import crac.module.notifier.notifications.FriendRequest;
import crac.module.utility.CracUtility;
import crac.module.utility.JSONResponseHelper;

/**
 * REST controller for managing users.
 */

@RestController
@RequestMapping("/user")
public class CracUserController {

	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private GroupDAO groupDAO;

	@Autowired
	private UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	private UserTaskRelDAO userTaskRelDAO;

	@Autowired
	private UserRelationshipDAO userRelationshipDAO;

	@Autowired
	private RoleDAO roleDAO;

	@Autowired
	private TokenDAO tokenDAO;

	@Autowired
	private AttachmentDAO attachmentDAO;

	@Autowired
	private Decider decider;

	@Autowired
	private NotificationFactory nf;

	/**
	 * Returns all users
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/all/", "/all" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() {

		List<UserShort> list = new ArrayList<>();

		for (CracUser u : userDAO.findAll()) {
			list.add(new UserShort(u));
		}

		return JSONResponseHelper.createResponse(list, true);
	}

	/**
	 * Returns the user with given id
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{user_id}", "/{user_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> show(@PathVariable(value = "user_id") Long id) {
		CracUser user = userDAO.findOne(id);

		if (user != null) {
			return JSONResponseHelper.createResponse(user, true);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Returns the logged in user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> getLogged() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		return JSONResponseHelper.createResponse(userDAO.findByName(userDetails.getName()), true);
	}

	/**
	 * Update the currently logged in user
	 * 
	 * @param json
	 * @param id
	 * @return ResponseEntity
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@RequestMapping(value = { "/",
			"" }, method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> updateLogged(@RequestBody String json)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		CracUser updatedUser;
		updatedUser = mapper.readValue(json, CracUser.class);

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser oldUser = userDAO.findByName(userDetails.getName());

		if (oldUser != null) {
			oldUser.update(updatedUser);
			userDAO.save(oldUser);
			return JSONResponseHelper.successfullyUpdated(oldUser);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * returns a json if the logged in user is valid
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/check", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> checkLoginData() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		return JSONResponseHelper.createResponse(user, true);

	}

	/**
	 * Get a valid token for the system and confirm your user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> loginUser() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		CracToken t = tokenDAO.findByUserId(user.getId());
		if (t != null) {

			HashMap<String, Object> meta = new HashMap<>();
			meta.put("action", "CREATE_TOKEN");
			meta.put("issue", "TOKEN_ALREADY_CREATED");
			meta.put("user", user);
			meta.put("roles", user.getRoles());
			return JSONResponseHelper.createResponse(t, true, meta);
		} else {
			CracToken token = new CracToken();

			SecureRandom random = new SecureRandom();
			String code = new BigInteger(130, random).toString(32);

			token.setCode(code);
			token.setUserId(user.getId());
			tokenDAO.save(token);

			HashMap<String, Object> meta = new HashMap<>();
			meta.put("action", "CREATE_TOKEN");
			meta.put("user", user);
			meta.put("roles", user.getRoles());
			return JSONResponseHelper.createResponse(token, true, meta);
		}

	}

	/**
	 * Delete your token
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> logoutUser() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		CracToken t = tokenDAO.findByUserId(user.getId());
		if (t != null) {
			ResponseEntity<String> v = JSONResponseHelper.createResponse(t, true, RESTAction.DELETE);
			userDAO.save(user);
			tokenDAO.delete(t);
			return v;
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.NO_TOKEN, RESTAction.DELETE);
		}

	}

	/**
	 * Return a sorted list of elements with the best fitting users for the
	 * given task
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/find/{task_id}",
			"/find/{task_id}/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> findUsers(@PathVariable(value = "task_id") Long taskId) {
		return JSONResponseHelper.createResponse(decider.findUsers(taskDAO.findOne(taskId), new UserFilterParameters()),
				true);
	}

	/**
	 * Issues a friend-request-notification to target user
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{user_id}/friend",
			"/{user_id}/friend/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addFriend(@PathVariable(value = "user_id") Long id) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser sender = userDAO.findByName(userDetails.getName());
		CracUser target = userDAO.findOne(id);

		Notification n = nf.createNotification(FriendRequest.class, target, sender, null);

		return JSONResponseHelper.successfullyCreated(n);
	}

	/**
	 * Unfriends target user
	 * 
	 * @param id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{user_id}/unfriend",
			"/{user_id}/unfriend/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeFriend(@PathVariable(value = "user_id") Long id) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser me = userDAO.findByName(userDetails.getName());
		CracUser friend = userDAO.findOne(id);
		UserRelationship rel = userRelationshipDAO.findByC1AndC2(me, friend);
		if (rel != null) {
			if (rel.isFriends()) {
				rel.setLikeValue(0.5);
				rel.setFriends(false);
				userRelationshipDAO.save(rel);
				return JSONResponseHelper.successfullyDeleted(friend);
				// return JSonResponseHelper.successfullUnfriend(friend);
			} else {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.USERS_NOT_FRIENDS);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}
	}

	/**
	 * Shows the friends of the logged in user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/friends", "/friends/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> showFriends() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Set<CracUser> friends = new HashSet<CracUser>();

		for (UserRelationship ur : user.getUserRelationshipsAs1()) {
			if (ur.isFriends()) {
				friends.add(ur.getC2());
			}
		}

		for (UserRelationship ur : user.getUserRelationshipsAs2()) {
			if (ur.isFriends()) {
				friends.add(ur.getC1());
			}
		}

		return JSONResponseHelper.createResponse(friends, true);

	}

	/**
	 * Shows the relationships of the logged in user
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/relationships",
			"/relationships/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> showRelationships() {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());

		Set<SimpleUserRelationship> rels = new HashSet<SimpleUserRelationship>();

		for (UserRelationship ur : user.getUserRelationshipsAs1()) {
			rels.add(new SimpleUserRelationship(ur.getC2(), ur.getLikeValue(), ur.isFriends()));
		}

		for (UserRelationship ur : user.getUserRelationshipsAs2()) {
			rels.add(new SimpleUserRelationship(ur.getC1(), ur.getLikeValue(), ur.isFriends()));
		}

		return JSONResponseHelper.createResponse(rels, true);

	}

	/**
	 * Adds a role to the logged in User
	 * 
	 * @param roleId
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/role/{role_id}/add",
			"/role/{role_id}/add/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addRole(@PathVariable(value = "role_id") Long roleId) {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		Role role = roleDAO.findOne(roleId);

		if (role != null) {
			if (user.getRoles().contains(role)) {
				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ALREADY_ASSIGNED);
			} else {
				user.getRoles().add(role);
				userDAO.save(user);
				return JSONResponseHelper.successfullyAssigned(role);
			}
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}

	}

	/**
	 * Removes a role from the logged in user
	 * 
	 * @param roleId
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/role/{role_id}/remove",
			"/role/{role_id}/remove/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> removeRole(@PathVariable(value = "role_id") Long roleId) {
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser user = userDAO.findByName(userDetails.getName());
		Role role = roleDAO.findOne(roleId);

		if (user.getRoles().contains(role)) {
			user.getRoles().remove(role);
			userDAO.save(user);
			return JSONResponseHelper.successfullyDeleted(role);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}
	}

	/**
	 * Adds target group to the groups of the logged-in user
	 * 
	 * @param groupId
	 * @return ResponseEntity
	 */

	@RequestMapping(value = "/group/{group_id}/enter", method = RequestMethod.PUT, produces = "application/json")

	@ResponseBody
	public ResponseEntity<String> enterGroup(@PathVariable(value = "group_id") Long groupId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		CracGroup g = groupDAO.findOne(groupId);
		CracUser user = userDAO.findByName(userDetails.getUsername());
		g.addUser(user);
		groupDAO.save(g);
		return JSONResponseHelper.successfullyUpdated(user);
	}

	/**
	 * Removes target group from the groups of the logged-in user
	 * 
	 * @param groupId
	 * @return ResponseEntity
	 */

	@RequestMapping(value = "/group/{group_id}/leave", method = RequestMethod.DELETE, produces = "application/json")

	@ResponseBody
	public ResponseEntity<String> leaveGroup(@PathVariable(value = "group_id") Long groupId) {

		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		CracGroup g = groupDAO.findOne(groupId);
		CracUser user = userDAO.findByName(userDetails.getUsername());
		g.removeUser(user);
		groupDAO.save(g);
		return JSONResponseHelper.successfullyUpdated(user);
	}

	@RequestMapping(value = { "/search",
			"/search/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> searchUsers(@RequestBody String json)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		PostOptions mapping = null;
		mapping = mapper.readValue(json, PostOptions.class);

		List<CracUser> result = new ArrayList<>();

		String firstName = mapping.getFirstName();
		String lastName = mapping.getLastName();

		if (lastName.equals("") && firstName.equals("")) {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.EMPTY_DATA);
		} else if (lastName.equals("") && !firstName.equals("")) {
			result = userDAO.findByFirstName(firstName);
		} else if (!lastName.equals("") && mapping.getFirstName().equals("")) {
			result = userDAO.findByLastName(firstName);
		} else {
			result = userDAO.findByFirstNameAndLastName(firstName, lastName);
		}

		return JSONResponseHelper.createResponse(result, true);

	}

	/**
	 * Adds an image to logged in user
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws InvalidActionException
	 */
	@RequestMapping(value = { "/image",
			"/image/" }, method = RequestMethod.POST, headers = "content-type=multipart/*", produces = "application/json")

	@ResponseBody
	public ResponseEntity<String> addAttachment(@RequestParam("file") MultipartFile file)
			throws IOException, InvalidActionException {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser u = userDAO.findByName(userDetails.getName());

		Attachment a = u.getUserImage();

		if (a != null) {
			try {
				CracUtility.removeFile(a.getPath());
			} catch (InvalidActionException ex) {
				ex.printStackTrace();
			}
		} else {
			a = new Attachment();
		}

		String name = file.getOriginalFilename();

		String path = CracUtility.processUpload(file, "image/jpeg", "image/jpg", "image/png");
		a.setPath(path);
		a.setName(name);
		a.setUser(u);
		attachmentDAO.save(a);
		u.setUserImage(a);
		userDAO.save(u);
		return JSONResponseHelper.successfullyUpdated(u);

	}

	/**
	 * Get the image of the logged in user
	 * 
	 * @return
	 * @throws IOException
	 * @throws InvalidActionException
	 */
	@RequestMapping(value = { "/image",
			"/image/" }, method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public ResponseEntity<byte[]> getUserImage() throws IOException, InvalidActionException {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser u = userDAO.findByName(userDetails.getName());

		Attachment a = u.getUserImage();

		if (a != null) {

			byte[] img = CracUtility.getFile(a.getPath());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_JPEG);

			return ResponseEntity.ok().headers(headers).body(img);
		} else {
			throw new InvalidActionException(ErrorCode.NOT_FOUND);

		}
	}

	@RequestMapping(value = { "/{user_id}/image",
			"/{user_id}/image/" }, method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public ResponseEntity<byte[]> getUserImageById(@PathVariable(value = "user_id") Long id)
			throws IOException, InvalidActionException {

		CracUser u = userDAO.findOne(id);

		if (u != null) {

			Attachment a = u.getUserImage();

			if (a != null) {

				byte[] img = CracUtility.getFile(a.getPath());

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.IMAGE_JPEG);

				return ResponseEntity.ok().headers(headers).body(img);
			}
		}
		
		throw new InvalidActionException(ErrorCode.NOT_FOUND);

	}

}
