package crac.module.matching.filter.matching;

import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.helpers.MatrixField;
import crac.module.matching.superclass.ConcreteFilter;

/**
 * This matching-filter modifies the matching score by adjusting competence-similarities based on the importance of every competence to the task it's connected to
 * @author David Hondl
 *
 */
public class ImportancyLevelFilter extends ConcreteFilter{

	public ImportancyLevelFilter() {
		super("ImportancyLevelFilter");
	}

	@Override
	public void apply(FilterParameters fp) {
		MatrixField m = fp.getM();
		double value = m.getVal();
		int importancyValue = m.getTaskRelation().getImportanceLevel();
		
		double newVal = value;

		// do only if the value is not 1, since 1 means that the user possesses
		// the competence
		if (value != 1) {
			newVal = value * (1 - ((double) importancyValue / 300));
		}
		System.out.println("Applied: "+super.speakString());
		
		m.setVal(newVal);
		
	}

}
