package crac.module.matching.superclass;

import crac.module.factories.CracFilterFactory;
import crac.module.matching.helpers.FilterParameters;

public abstract class IndividualFilter extends ConcreteFilter {

	public IndividualFilter(String name) {
		super(name);
	}

	@Override
	public abstract void apply(FilterParameters fp);
	
	public abstract void convert(CracFilterFactory cff);

}
