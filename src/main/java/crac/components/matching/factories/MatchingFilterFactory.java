package crac.components.matching.factories;

import crac.components.matching.CracMatchingFilter;
import crac.components.matching.filter.matching.ImportancyLevelFilter;
import crac.components.matching.filter.matching.LikeLevelFilter;
import crac.components.matching.filter.matching.ProficiencyLevelFilter;
import crac.components.matching.filter.matching.UserRelationFilter;

public class MatchingFilterFactory {
	
	public CracMatchingFilter createMatchingFilter(String type){
		if (type.equals("LikeLevelFilter")) {
			return new LikeLevelFilter();
		} else if (type.equals("ImportancyLevelFilter")) {
			return new ImportancyLevelFilter();
		} else if (type.equals("ProficiencyLevelFilter")) {
			return new ProficiencyLevelFilter();
		} else if (type.equals("UserRelationFilter")) {
			return new UserRelationFilter();
		}else{
			return null;
		}

	}

}
