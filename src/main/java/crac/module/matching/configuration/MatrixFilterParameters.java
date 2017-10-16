package crac.module.matching.configuration;

import java.util.ArrayList;
import java.util.List;

import crac.module.factories.CracFilterFactory;
import crac.module.matching.interfaces.CracFilter;
import crac.module.matching.interfaces.FilterConfiguration;
import crac.module.matching.superclass.ConcreteFilter;
import lombok.Getter;
import lombok.Setter;
import crac.module.matching.filter.*;

public class MatrixFilterParameters {

	@Getter
	@Setter
	private List<String> parameters;
	
	public MatrixFilterParameters() {
		parameters = new ArrayList<>();
	}

	public boolean apply(CracFilterFactory mf, FilterConfiguration matchingConfig, String path) {
		int count = 0;
		for (String p : parameters) {
			ConcreteFilter f = mf.createMatchingFilterFromString(p, path);
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
}
