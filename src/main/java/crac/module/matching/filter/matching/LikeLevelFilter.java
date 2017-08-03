package crac.module.matching.filter.matching;

import java.util.HashMap;

import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.helpers.MatrixField;
import crac.module.matching.superclass.ConcreteFilter;

public class LikeLevelFilter extends ConcreteFilter{

	public LikeLevelFilter() {
		super("LikeLevelFilter");
	}

	@Override
	public void apply(FilterParameters fp) {
		MatrixField m = fp.getM();
	
		double value = m.getVal();
		int likeValue = m.getUserRelation().getLikeValue();
		
		double newVal = value * (1 + (1 - value) * (double) likeValue / 100);
		
		if (newVal > 1) {
			newVal = 1;
		} else if (newVal < 0) {
			newVal = 0;
		}
		System.out.println("Applied: "+super.speakString());
		

		m.setVal(newVal);
		
	}

}
