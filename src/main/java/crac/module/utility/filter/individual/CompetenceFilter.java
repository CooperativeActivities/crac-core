package crac.module.utility.filter.individual;

import java.util.ArrayList;
import java.util.List;

import crac.exception.WrongParameterException;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.utility.ParamterDummy;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

public class CompetenceFilter extends ConcreteFilter {

	public CompetenceFilter() {
		super("Competence-Filter");
		System.out.println("created");
	}

	@Override
	public void apply(FilterParameters fp) {

		List<ParamterDummy> params = super.getPf().getParams();
		List<Competence> l = new ArrayList<>();
		CompetenceDAO competenceDAO = super.getCff().getCompetenceDAO();

		for (ParamterDummy pm : params) {
			Competence c;
			try {
				c = competenceDAO.findOne(new Long((int) pm.getValue()));
			} catch (Exception e) {
				e.printStackTrace();
				throw new WrongParameterException("Wrong parameters");
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
