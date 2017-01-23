package crac.decider.filter;

import crac.decider.core.CracFilter;
import crac.models.storage.MatrixField;

public class LikeLevelFilter extends CracFilter{

	public LikeLevelFilter() {
		super("LikeLevelFilter");
	}

	@Override
	public double apply(MatrixField m) {
		
		double value = m.getVal();
		int likeValue = m.getUserRelation().getLikeValue();
		
		double newVal = value * (1 + (1 - value / 2) * (double) likeValue / 100);
		System.out.println(newVal);

		if (newVal > 1) {
			newVal = 1;
		} else if (newVal < 0) {
			newVal = 0;
		}

		return newVal;
		
	}

}
