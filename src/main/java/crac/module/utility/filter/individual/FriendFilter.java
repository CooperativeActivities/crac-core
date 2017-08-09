package crac.module.utility.filter.individual;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import crac.models.db.daos.CracUserDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.utility.ParamterDummy;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

public class FriendFilter extends ConcreteFilter {

	public FriendFilter() {
		super("Friend-Filter");
		System.out.println("created");
	}

	@Override
	public void apply(FilterParameters fp) {

		List<ParamterDummy> params = super.getPf().getParams();
		List<CracUser> l = new ArrayList<>();
		CracUserDAO userDAO = super.getCff().getUserDAO();

		String firstName;
		String lastName;
		String name;

		for (ParamterDummy pm : params) {
			HashMap<String, String> v = (HashMap<String, String>) pm.getValue();

			name = (pm.getName() != null) ? pm.getName() : "";
			firstName = (v.get("firstName") != null) ? v.get("firstName") : "";
			lastName = (v.get("lastName") != null) ? v.get("lastName") : "";

			List<CracUser> c = userDAO.queryByNameOrFullname(name, firstName, lastName);
			System.out.println("Name: " + name + " FirstName: " + firstName + " LastName: " + lastName);

			l.addAll(c);
		}

		List<Task> result = new ArrayList<>();

		for (Task t : fp.getTasksPool()) {
			boolean fin = false;
			for (CracUser u : l) {
				if (t.getUserRelationships().contains(u)) {
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
