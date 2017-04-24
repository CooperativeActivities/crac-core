package crac.components.matching.configuration;

import java.util.ArrayList;

import crac.components.matching.CracFilter;
import crac.models.storage.MatrixField;

public class GlobalMatrixFilterConfig {
	
	private FilterConfiguration config = new FilterConfiguration();
	
	private static GlobalMatrixFilterConfig instance = new GlobalMatrixFilterConfig();

	public static void addFilter(CracFilter filter){
		filter.addSpeak();
		instance.config.addFilter(filter);
	}
	
	public static void clearFilters(){
		instance.config.clearFilters();
	}
	
	public static void printFilters(){
		instance.config.printFilters();
	}
	
	public static String filtersToString(){
		return instance.config.filtersToString();
	}
	
	public static FilterConfiguration cloneConfiguration(){
		return instance.config.clone();
	}

}
