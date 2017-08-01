package crac.controllers;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import crac.enums.ErrorCause;
import crac.module.matching.configuration.MatchingConfiguration;
import crac.module.matching.configuration.MatrixFilterParameters;
import crac.module.matching.filter.matching.ImportancyLevelFilter;
import crac.module.matching.filter.matching.LikeLevelFilter;
import crac.module.matching.filter.matching.ProficiencyLevelFilter;
import crac.module.matching.filter.matching.UserRelationFilter;
import crac.module.utility.JSONResponseHelper;

@RestController
@RequestMapping("/configuration")
public class FilterConfigurationController {
	
	@Autowired
	private MatchingConfiguration matchingConfig;

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
			matchingConfig.addFilter(new LikeLevelFilter());
			name = filterName;

		} else if (filterName.equals("ImportancyLevelFilter")) {
			matchingConfig.addFilter(new ImportancyLevelFilter());
			name = filterName;

		} else if (filterName.equals("ProficiencyLevelFilter")) {
			matchingConfig.addFilter(new ProficiencyLevelFilter());
			name = filterName;

		} else if (filterName.equals("UserRelationFilter")) {
			matchingConfig.addFilter(new UserRelationFilter());
			name = filterName;

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.NOT_FOUND);
		}
		
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("filter", name);
		return JSONResponseHelper.createResponse(true, meta);
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

			matchingConfig.clearFilters();

			if (!mfp.apply(matchingConfig)) {

				restoreStandard();

				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.NOT_FOUND);
			}	

			return JSONResponseHelper.successfullyUpdated(mfp);

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCause.EMPTY_DATA);
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
		return JSONResponseHelper.createResponse(matchingConfig.filtersToString(), true);
	}

	/**
	 * Clears the list of active filters
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/filter/clear",
			"/filter/clear/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> clearFilter() {
		matchingConfig.clearFilters();
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("filters", "CLEARED");
		return JSONResponseHelper.createResponse(true, meta);
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
		return JSONResponseHelper.createResponse(true, meta);
	}

	public void restoreStandard() {
		matchingConfig.clearFilters();
		matchingConfig.addFilter(new ProficiencyLevelFilter());
		matchingConfig.addFilter(new LikeLevelFilter());
		matchingConfig.addFilter(new ImportancyLevelFilter());
		matchingConfig.addFilter(new UserRelationFilter());
	}

}
