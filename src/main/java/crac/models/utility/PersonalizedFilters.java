package crac.models.utility;

import crac.module.matching.factories.CracFilterFactory;
import crac.module.matching.superclass.ConcreteFilter;
import lombok.Getter;
import lombok.Setter;

public class PersonalizedFilters {

	@Getter
	@Setter
	private String query;

	@Getter
	@Setter
	private PersonalizedFilter[] filters;

	public PersonalizedFilters() {
		query = "";
	}

	public void convert(CracFilterFactory mf) {

		for (PersonalizedFilter f : filters) {
			Class<?> c = null;
			try {
				c = Class.forName("crac.module.utility.filter.individual." + f.getName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if (c != null) {
				ConcreteFilter cf = mf.createMatchingFilter((Class<ConcreteFilter>) c);
				cf.setPf(f);
				f.setCf(cf);
			}
		}
	}

}
