package crac.components.matching.filter;

import crac.components.matching.CracFilter;
import crac.models.storage.MatrixField;

public class ProficiencyLevelFilter extends CracFilter{

	public ProficiencyLevelFilter() {
		super("ProficiencyLevelFilter");
	}

	@Override
	public double apply(MatrixField m) {

		double value = m.getVal();
		int neededProficiency = m.getTaskRelation().getNeededProficiencyLevel();
		int proficiencyValue = m.getUserRelation().getProficiencyValue();
		
		double newVal = value;
		if (proficiencyValue < neededProficiency) {
			newVal = value * ((double) 1 - (((double) neededProficiency / 100) - ((double) proficiencyValue / 100)));
		}
		
		return newVal;
		
	}

}
