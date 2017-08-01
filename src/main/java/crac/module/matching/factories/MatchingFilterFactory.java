package crac.module.matching.factories;

import crac.module.matching.filter.matching.ImportancyLevelFilter;
import crac.module.matching.filter.matching.LikeLevelFilter;
import crac.module.matching.filter.matching.ProficiencyLevelFilter;
import crac.module.matching.filter.matching.UserRelationFilter;
import crac.module.matching.superclass.CracMatchingFilter;

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
