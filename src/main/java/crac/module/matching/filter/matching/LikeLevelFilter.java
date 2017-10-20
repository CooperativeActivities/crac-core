package crac.module.matching.filter.matching;

import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.helpers.MatrixField;
import crac.module.matching.superclass.ConcreteFilter;

/**
 * This matching-filter modifies the matching score by adjusting competence-similarities based on the like-value of every competence to the user it's connected to
 * @author David Hondl
 *
 */
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
