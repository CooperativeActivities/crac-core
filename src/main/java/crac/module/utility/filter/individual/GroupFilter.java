package crac.module.utility.filter.individual;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import crac.exception.InvalidParameterException;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.GroupDAO;
import crac.models.db.entities.CracGroup;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.input.PersonalizedFilters.PersonalizedFilter.InputParameters;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

public class GroupFilter extends ConcreteFilter {

	public GroupFilter() {
		super("Group-Filter");
		System.out.println("created");
	}

	@Override
	public void apply(FilterParameters fp) {

		List<InputParameters> params = super.getPf().getParams();
		List<CracGroup> l = new ArrayList<>();
		GroupDAO groupDAO = super.getCff().getGroupDAO();

		for (InputParameters pm : params) {
			CracGroup g;
			try {
				g = groupDAO.findByIdAndName((int) pm.getValue(), pm.getName());
			} catch (Exception e) {
				throw new InvalidParameterException("Wrong parameters");
			}
			l.add(g);
		}

		List<Task> result = new ArrayList<>();

		for (Task t : fp.getTasksPool()) {
			boolean fin = false;
			for (CracGroup g : l) {
				if (t.getRestrictingGroups().contains(g)) {
					fin = true;
					break;
				}
			}
			if (fin) {
				result.add(t);
			}
		}

		fp.setTasksPool(result);

	}

}
