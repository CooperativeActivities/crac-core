package crac.decider.core;

import java.util.List;

import crac.decider.filter.ImportancyLevelFilter;
import crac.decider.filter.LikeLevelFilter;
import crac.decider.filter.ProficiencyLevelFilter;
import crac.decider.filter.UserRelationFilter;
import crac.decider.workers.config.GlobalMatrixFilterConfig;

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
