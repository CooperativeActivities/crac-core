package crac.controllers;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
import crac.module.matching.configuration.MatchingConfiguration;
import crac.module.matching.configuration.MatrixFilterParameters;
import crac.module.matching.configuration.PostMatchingConfiguration;
import crac.module.matching.configuration.PreMatchingConfiguration;
import crac.module.matching.factories.CracFilterFactory;
import crac.module.matching.filter.matching.ImportancyLevelFilter;
import crac.module.matching.filter.matching.LikeLevelFilter;
import crac.module.matching.filter.matching.ProficiencyLevelFilter;
import crac.module.matching.filter.matching.UserRelationFilter;
import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.utility.JSONResponseHelper;
import lombok.Getter;

@RestController
@RequestMapping("/filter")
public class FilterConfigurationController {

	@Getter
	@Value("${crac.filters.prematching}")
	private String preMatchingPath;

	@Getter
	@Value("${crac.filters.matching}")
	private String matchingPath;

	@Getter
	@Value("${crac.filters.postmatching}")
	private String postMatchingPath;

	@Autowired
	private MatchingConfiguration matchingConfig;

	@Autowired
	private PreMatchingConfiguration preMatchingConfig;

	@Autowired
	private PostMatchingConfiguration postMatchingConfig;

	@Autowired
	private CracFilterFactory cf;

	/**
	 * Adds multiple filters to the filter-configuration, based on the list of
	 * filters in the posted JSon-file
	 * 
	 * @param json
	 * @return ResponseEntity
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@RequestMapping(value = { "/{matching_type}/add",
			"/{matching_type}/add/" }, method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	@ResponseBody
	public ResponseEntity<String> addFiltersByJson(@RequestBody String json,
			@PathVariable(value = "matching_type") String matchingType)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		MatrixFilterParameters mfp = null;
		mfp = mapper.readValue(json, MatrixFilterParameters.class);

		FilterConfiguration conf = null;
		String path = "";

		if (mfp != null) {

			if (matchingType.equals("prematching")) {
				conf = preMatchingConfig;
				path = preMatchingPath;
			} else if (matchingType.equals("matching")) {
				conf = matchingConfig;
				path = matchingPath;
			} else if (matchingType.equals("postmatching")) {
				conf = postMatchingConfig;
				path = postMatchingPath;
			}

			matchingConfig.clearFilters();

			if (!mfp.apply(cf, conf, path)) {

				conf.restore();

				return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.NOT_FOUND);
			}

			return JSONResponseHelper.successfullyUpdated(mfp);

		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.EMPTY_DATA);
		}

	}

	/**
	 * Returns a list of all active filters
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{matching_type}/print",
			"/{matching_type}/print/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> printFilter(@PathVariable(value = "matching_type") String matchingType) {

		FilterConfiguration conf = null;

		if (matchingType.equals("prematching")) {
			conf = preMatchingConfig;
		} else if (matchingType.equals("matching")) {
			conf = matchingConfig;
		} else if (matchingType.equals("postmatching")) {
			conf = postMatchingConfig;
		}

		return JSONResponseHelper.createResponse(conf.filtersToString(), true);
	}

	/**
	 * Clears the list of active filters
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{matching_type}/clear",
			"/{matching_type}/clear/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> clearFilter(@PathVariable(value = "matching_type") String matchingType) {
		
		FilterConfiguration conf = null;

		if (matchingType.equals("prematching")) {
			conf = preMatchingConfig;
		} else if (matchingType.equals("matching")) {
			conf = matchingConfig;
		} else if (matchingType.equals("postmatching")) {
			conf = postMatchingConfig;
		}
		
		conf.clearFilters();
		HashMap<String, Object> meta = new HashMap<>();
		meta.put(matchingType+ "-filters", "CLEARED");
		return JSONResponseHelper.createResponse(true, meta);
	}

	/**
	 * Restores the standard state of the filter-configuration
	 * 
	 * @return ResponseEntity
	 */
	@RequestMapping(value = { "/{matching_type}/restore",
			"/{matching_type}/restore/" }, method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> restoreFilter(@PathVariable(value = "matching_type") String matchingType) {
		
		FilterConfiguration conf = null;

		if (matchingType.equals("prematching")) {
			conf = preMatchingConfig;
		} else if (matchingType.equals("matching")) {
			conf = matchingConfig;
		} else if (matchingType.equals("postmatching")) {
			conf = postMatchingConfig;
		}
		
		conf.restore();

		HashMap<String, Object> meta = new HashMap<>();
		meta.put(matchingType+ "-filters", "RESTORED");
		return JSONResponseHelper.createResponse(true, meta);
	}

}
