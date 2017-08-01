package crac.module.matching.filter.matching;

import crac.models.storage.MatrixField;
import crac.module.matching.superclass.CracMatchingFilter;

public class ImportancyLevelFilter extends CracMatchingFilter{

	public ImportancyLevelFilter() {
		super("ImportancyLevelFilter");
	}

	@Override
	public Double apply(MatrixField m) {
		double value = m.getVal();
		int importancyValue = m.getTaskRelation().getImportanceLevel();
		
		double newVal = value;

		// do only if the value is not 1, since 1 means that the user possesses
		// the competence
		if (value != 1) {
			newVal = value * (1 - ((double) importancyValue / 300));
		}
		System.out.println("Applied: "+super.speakString());
		
		return newVal;
		
	}

}
