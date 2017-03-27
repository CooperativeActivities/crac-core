package crac.controllers;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import crac.decider.core.MatrixFilterParameters;
import crac.decider.filter.ImportancyLevelFilter;
import crac.decider.filter.LikeLevelFilter;
import crac.decider.filter.ProficiencyLevelFilter;
import crac.decider.filter.UserRelationFilter;
import crac.decider.workers.config.GlobalMatrixFilterConfig;
import crac.enums.ErrorCause;
import crac.models.db.entities.CracUser;
import crac.utility.JSonResponseHelper;

@RestController
@RequestMapping("/configuration")
public class FilterConfigurationController {

	/**
	 * Adds a filter to the filter-configuration, based on it's name
	 * @param filterName
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/filter/add/{filter_name}",
			"/filter/add/likeLevelFilter/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> addlikeLevelFilter(@PathVariable(value = "filter_name") String filterName) {

		String name = "";

		if (filterName.equals("LikeLevelFilter")) {
			GlobalMatrixFilterConfig.addFilter(new LikeLevelFilter());
			name = filterName;

		} else if (filterName.equals("ImportancyLevelFilter")) {
			GlobalMatrixFilterConfig.addFilter(new ImportancyLevelFilter());
			name = filterName;

		} else if (filterName.equals("ProficiencyLevelFilter")) {
			GlobalMatrixFilterConfig.addFilter(new ProficiencyLevelFilter());
			name = filterName;

		} else if (filterName.equals("UserRelationFilter")) {
			GlobalMatrixFilterConfig.addFilter(new UserRelationFilter());
			name = filterName;

		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.NOT_FOUND);
		}
		
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("filter", name);
		return JSonResponseHelper.createResponse(true, meta);
	}

	/**
	 * Adds multiple filters to the filter-configuration, based on the list of filters in the posted JSon-file
	 * @param json
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/filter/add",
			"/filter/add/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addFiltersByJson(@RequestBody String json) {
		ObjectMapper mapper = new ObjectMapper();
		MatrixFilterParameters mfp = null;
		try {
			mfp = mapper.readValue(json, MatrixFilterParameters.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mfp != null) {

			GlobalMatrixFilterConfig.clearFilters();

			if (!mfp.apply()) {

				restoreStandard();

				return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.NOT_FOUND);
			}	

			return JSonResponseHelper.successfullyUpdated(mfp);

		} else {
			return JSonResponseHelper.createResponse(false, "bad_request", ErrorCause.EMPTY_DATA);
		}

	}

	/**
	 * Returns a list of all active filters
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/filter/print",
			"/filter/print/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> printFilter() {
		return JSonResponseHelper.createResponse(GlobalMatrixFilterConfig.filtersToString(), true);
	}

	/**
	 * Clears the list of active filters
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/filter/clear",
			"/filter/clear/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> clearFilter() {
		GlobalMatrixFilterConfig.clearFilters();
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("filters", "CLEARED");
		return JSonResponseHelper.createResponse(true, meta);
	}

	/**
	 * Restores the standard state of the filter-configuration
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/filter/restore",
			"/filter/restore/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> restoreFilter() {
		restoreStandard();

		HashMap<String, Object> meta = new HashMap<>();
		meta.put("filters", "RESTORED");
		return JSonResponseHelper.createResponse(true, meta);
	}

	public void restoreStandard() {
		GlobalMatrixFilterConfig.clearFilters();
		GlobalMatrixFilterConfig.addFilter(new ProficiencyLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new LikeLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new ImportancyLevelFilter());
	}

}
