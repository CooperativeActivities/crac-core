package crac.module.utility.filter.individual;

import java.util.ArrayList;
import java.util.List;

import crac.exception.InvalidParameterException;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.input.PersonalizedFilters.PersonalizedFilter.InputParameters;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

/**
 * The Competence-Filter requires arbitrary competence-ids to filter tasks for these competences
 * @author David Hondl
 *
 */
public class CompetenceFilter extends ConcreteFilter {

	public CompetenceFilter() {
		super("Competence-Filter");
		System.out.println("created");
	}

	@Override
	public void apply(FilterParameters fp) {

		List<InputParameters> params = super.getPf().getParams();
		List<Competence> l = new ArrayList<>();
		CompetenceDAO competenceDAO = super.getCff().getCompetenceDAO();

		for (InputParameters pm : params) {
			Competence c;
			try {
				c = competenceDAO.findOne(new Long((int) pm.getValue()));
			} catch (Exception e) {
				e.printStackTrace();
				throw new InvalidParameterException("Wrong parameters");
			}
			l.add(c);
		}

		List<Task> result = new ArrayList<>();

		for (Task t : fp.getTasksPool()) {
			boolean fin = false;
			for (Competence c : l) {
				for (CompetenceTaskRel ctr : t.getMappedCompetences()) {
					if (ctr.getCompetence() == c) {
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
