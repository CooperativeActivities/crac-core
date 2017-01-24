package crac.decider.workers.config;

import java.util.ArrayList;

import crac.decider.core.CracFilter;
import crac.decider.core.MatrixFilterConfiguration;
import crac.models.storage.MatrixField;

public class GlobalMatrixConfig {
	
	private MatrixFilterConfiguration config = new MatrixFilterConfiguration();
	
	private static GlobalMatrixConfig instance = new GlobalMatrixConfig();

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
	
	public static MatrixFilterConfiguration cloneConfiguration(){
		return instance.config.clone();
	}

}
