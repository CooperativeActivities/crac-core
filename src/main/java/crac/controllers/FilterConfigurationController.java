package crac.controllers;

import java.io.IOException;

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
import crac.decider.workers.config.GlobalMatrixConfig;
import crac.models.CracUser;
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
			GlobalMatrixConfig.addFilter(new LikeLevelFilter());
			name = filterName;

		} else if (filterName.equals("ImportancyLevelFilter")) {
			GlobalMatrixConfig.addFilter(new ImportancyLevelFilter());
			name = filterName;

		} else if (filterName.equals("ProficiencyLevelFilter")) {
			GlobalMatrixConfig.addFilter(new ProficiencyLevelFilter());
			name = filterName;

		} else if (filterName.equals("UserRelationFilter")) {
			GlobalMatrixConfig.addFilter(new UserRelationFilter());
			name = filterName;

		} else {
			return JSonResponseHelper.actionNotPossible("Given filter does not exist!");
		}

		return JSonResponseHelper.successFullAction(name + " added!");
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

			GlobalMatrixConfig.clearFilters();

			if (!mfp.apply()) {

				restoreStandard();

				return JSonResponseHelper.actionNotPossible("No valid filters, standard filters restored");
			}

			return JSonResponseHelper.successFullAction("Filters have been updated!");

		} else {
			return JSonResponseHelper.emptyData();
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
		return JSonResponseHelper.successFullAction(GlobalMatrixConfig.filtersToString());
	}

	/**
	 * Clears the list of active filters
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/filter/clear",
			"/filter/clear/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> clearFilter() {
		GlobalMatrixConfig.clearFilters();

		return JSonResponseHelper.successFullAction("Filters cleared!");
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

		return JSonResponseHelper.successFullAction("Filters restored!");
	}

	public void restoreStandard() {
		GlobalMatrixConfig.clearFilters();
		GlobalMatrixConfig.addFilter(new ProficiencyLevelFilter());
		GlobalMatrixConfig.addFilter(new LikeLevelFilter());
		GlobalMatrixConfig.addFilter(new ImportancyLevelFilter());
	}

}
