package crac.components.matching.filter;

import crac.components.matching.CracFilter;
import crac.models.storage.MatrixField;

public class LikeLevelFilter extends CracFilter{

	public LikeLevelFilter() {
		super("LikeLevelFilter");
	}

	@Override
	public double apply(MatrixField m) {
		
		double value = m.getVal();
		int likeValue = m.getUserRelation().getLikeValue();
		
		double newVal = value * (1 + (1 - value) * (double) likeValue / 100);
		
		if (newVal > 1) {
			newVal = 1;
		} else if (newVal < 0) {
			newVal = 0;
		}

		return newVal;
		
	}

}
