package crac.module.matching.filter.matching;

import crac.module.matching.helpers.MatrixField;
import crac.module.matching.superclass.CracMatchingFilter;

public class LikeLevelFilter extends CracMatchingFilter{

	public LikeLevelFilter() {
		super("LikeLevelFilter");
	}

	@Override
	public Double apply(MatrixField m) {
		
		double value = m.getVal();
		int likeValue = m.getUserRelation().getLikeValue();
		
		double newVal = value * (1 + (1 - value) * (double) likeValue / 100);
		
		if (newVal > 1) {
			newVal = 1;
		} else if (newVal < 0) {
			newVal = 0;
		}
		System.out.println("Applied: "+super.speakString());
		

		return newVal;
		
	}

}
