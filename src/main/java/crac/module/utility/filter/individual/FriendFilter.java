package crac.module.utility.filter.individual;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import crac.exception.InvalidParameterException;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;
import crac.module.utility.filter.input.InputParameters;

/**
 * The Friend-Filter requires a username as name or both firstname and lastname to find a user and show tasks that this user is
 * @author David Hondl
 *
 */
public class FriendFilter extends ConcreteFilter {

	public FriendFilter() {
		super("Friend-Filter");
		System.out.println("created");
	}

	@Override
	public void apply(FilterParameters fp) {

		List<InputParameters> params = super.getPf().getParams();
		List<CracUser> l = new ArrayList<>();
		CracUserDAO userDAO = super.getCff().getUserDAO();

		String firstName = "";
		String lastName = "";
		String name = "";

		for (InputParameters pm : params) {

			try {
				HashMap<String, String> v = (HashMap<String, String>) pm.getValue();
				name = (pm.getName() != null) ? pm.getName() : "";
				firstName = (v.get("firstName") != null) ? v.get("firstName") : "";
				lastName = (v.get("lastName") != null) ? v.get("lastName") : "";
			} catch (Exception e) {
					throw new InvalidParameterException("Wrong parameters");
			}

			List<CracUser> c = userDAO.queryByNameOrFullname(name, firstName, lastName);
			System.out.println("Name: " + name + " FirstName: " + firstName + " LastName: " + lastName);
			l.addAll(c);
		}

		List<Task> result = new ArrayList<>();

		for (Task t : fp.getTasksPool()) {
			boolean fin = false;
			for (CracUser u : l) {
				for(UserTaskRel utr : t.getUserRelationships()){
					if (utr.getUser().getId() == u.getId()) {
						fin = true;
						break;
					}
				}
				
			}
			if (fin) {
				result.add(t);
			}
		}

		fp.setTasksPool(result);

	}

}
