package crac.decider.workers.config;

import java.util.ArrayList;

import crac.decider.core.CracFilter;
import crac.models.storage.MatrixField;

public class MatchingMatrixConfig {
	
	private ArrayList<CracFilter> filters = new ArrayList<>();
	
	private static MatchingMatrixConfig instance = new MatchingMatrixConfig();

	public static void applyFilters(MatrixField m){
		for(CracFilter filter : instance.filters){
			m.setVal(filter.apply(m));
		}
	}
	
	public static void addFilter(CracFilter filter){
		filter.addSpeak();
		instance.filters.add(filter);
	}
	
	public static void clearFilters(){
		instance.filters.clear();
	}
	
	public static void printFilters(){
		for(CracFilter filter : instance.filters){
			filter.speak();
		}
	}
	
	public static String filtersToString(){
		String s = "";
		for(CracFilter filter : instance.filters){
			s += filter.speakString() + " ";
		}
		return s;
	}

}
