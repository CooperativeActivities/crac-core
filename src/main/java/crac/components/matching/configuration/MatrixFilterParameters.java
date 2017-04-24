package crac.components.matching.configuration;

import java.util.List;

import crac.components.matching.filter.ImportancyLevelFilter;
import crac.components.matching.filter.LikeLevelFilter;
import crac.components.matching.filter.ProficiencyLevelFilter;
import crac.components.matching.filter.UserRelationFilter;

public class MatrixFilterParameters {

	private List<String> parameters;

	public MatrixFilterParameters() {
	}

	public boolean apply() {
		int count = 0;
		for (String p : parameters) {
			if (p.equals("LikeLevelFilter")) {
				GlobalMatrixFilterConfig.addFilter(new LikeLevelFilter());
				count++;
			} else if (p.equals("ImportancyLevelFilter")) {
				GlobalMatrixFilterConfig.addFilter(new ImportancyLevelFilter());
				count++;

			} else if (p.equals("ProficiencyLevelFilter")) {
				GlobalMatrixFilterConfig.addFilter(new ProficiencyLevelFilter());
				count++;

			} else if (p.equals("UserRelationFilter")) {
				GlobalMatrixFilterConfig.addFilter(new UserRelationFilter());
				count++;
			}
		}
		if(count == 0){
			return false;
		}else{
			return true;
		}
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

}
