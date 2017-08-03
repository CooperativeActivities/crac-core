package crac.module.matching.configuration;

import java.util.List;

import crac.module.matching.factories.CracFilterFactory;
import crac.module.matching.interfaces.CracFilter;
import crac.module.matching.superclass.ConcreteFilter;

public class MatrixFilterParameters {

	private List<Class<CracFilter>> parameters;
	private CracFilterFactory mff;

	public MatrixFilterParameters() {
	}

	public boolean apply(MatchingConfiguration matchingConfig) {
		int count = 0;
		for (Class<CracFilter> p : parameters) {
			ConcreteFilter f = (ConcreteFilter) mff.createMatchingFilter(p);
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

	public List<Class<CracFilter>> getParameters() {
		return parameters;
	}

	public void setParameters(List<Class<CracFilter>> parameters) {
		this.parameters = parameters;
	}

	public CracFilterFactory getMff() {
		return mff;
	}

	public void setMff(CracFilterFactory mff) {
		this.mff = mff;
	}

}
