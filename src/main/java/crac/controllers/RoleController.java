package crac.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.enums.ErrorCause;
import crac.models.db.daos.RoleDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Role;
import crac.notifier.NotificationHelper;
import crac.utility.JSonResponseHelper;

@RestController
@RequestMapping("/role")
public class RoleController {

	@Autowired
	private RoleDAO roleDAO;

	/**
	 * Returns all possible roles
	 * @return ResponseEntity
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> index() throws JsonProcessingException {
		return JSonResponseHelper.createResponse(roleDAO.findAll(), true);
	}

}
