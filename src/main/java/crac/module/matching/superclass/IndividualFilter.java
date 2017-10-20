package crac.module.matching.superclass;

import crac.module.factories.CracFilterFactory;
import crac.module.matching.helpers.FilterParameters;

/**
 * Further subclass of the ConcreteFilter for handling a separate (user-configured) branch of filters in the system
 * @author David Hondl
 *
 */
public abstract class IndividualFilter extends ConcreteFilter {

	public IndividualFilter(String name) {
		super(name);
	}

	/**
	 * The overwritten apply()-method, introduced by the Crac-Filter-interface
	 */
	@Override
	public abstract void apply(FilterParameters fp);
	
	/**
	 * A method that allows for conversion from the name of the filter (as string) to the actual object (created by the factory)
	 * @param cff
	 */
	public abstract void convert(CracFilterFactory cff);

}
