package crac.module.matching.configuration;

import java.util.List;

import crac.module.matching.factories.MatchingFilterFactory;
import crac.module.matching.superclass.CracMatchingFilter;

public class MatrixFilterParameters {

	private List<String> parameters;
	private MatchingFilterFactory mff;

	public MatrixFilterParameters() {
	}

	public boolean apply(MatchingConfiguration matchingConfig) {
		int count = 0;
		for (String p : parameters) {
			CracMatchingFilter f = mff.createMatchingFilter(p);
			if (f != null) {
				matchingConfig.addFilter(f);
			}
			count++;
		}
		if (count == 0) {
			return false;
		} else {
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
