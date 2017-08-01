package crac.components.matching.filter.matching;

import crac.components.matching.CracMatchingFilter;
import crac.models.storage.MatrixField;

public class ProficiencyLevelFilter extends CracMatchingFilter{

	public ProficiencyLevelFilter() {
		super("ProficiencyLevelFilter");
	}

	@Override
	public Double apply(MatrixField m) {

		double value = m.getVal();
		int neededProficiency = m.getTaskRelation().getNeededProficiencyLevel();
		int proficiencyValue = m.getUserRelation().getProficiencyValue();
		
		double newVal = value;
		if (proficiencyValue < neededProficiency) {
			newVal = value * ((double) 1 - (((double) neededProficiency / 100) - ((double) proficiencyValue / 100)));
		}
		
		if (newVal > 1) {
			newVal = 1;
		} else if (newVal < 0) {
			newVal = 0;
		}
		System.out.println("Applied: "+super.speakString());
		

		return newVal;
		
	}

}
