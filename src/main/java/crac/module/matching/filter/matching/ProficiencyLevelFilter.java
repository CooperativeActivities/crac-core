package crac.module.matching.filter.matching;

import java.util.HashMap;

import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.helpers.MatrixField;
import crac.module.matching.superclass.ConcreteFilter;

public class ProficiencyLevelFilter extends ConcreteFilter{

	public ProficiencyLevelFilter() {
		super("ProficiencyLevelFilter");
	}

	@Override
	public void apply(FilterParameters fp) {
		MatrixField m = fp.getM();

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
		

		m.setVal(newVal);
		
	}

}
