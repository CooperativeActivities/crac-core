package crac.module.utility.filter.individual;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import crac.exception.WrongParameterException;
import crac.models.db.entities.Task;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.superclass.ConcreteFilter;

public class DateFilter extends ConcreteFilter {

	public DateFilter() {
		super("Date-Filter");
		System.out.println("created");
	}

	@Override
	public void apply(FilterParameters fp) {
		
		Long startDateMin;
		Long startDateMax;
		Long endDateMin;
		Long endDateMax;

		try {
			startDateMin = (Long) super.getPf().getParam("startDateMin");
			startDateMax = (Long) super.getPf().getParam("startDateMax");
			endDateMin = (Long) super.getPf().getParam("endDateMin");
			endDateMax = (Long) super.getPf().getParam("endDateMax");
		} catch (Exception e) {
			throw new WrongParameterException("Wrong parameters");
		}

		boolean minSD = startDateMin != null;
		boolean maxSD = startDateMax != null;
		boolean minED = endDateMin != null;
		boolean maxED = endDateMax != null;

		List<Task> result = new ArrayList<>();

		for (Task t : fp.getTasksPool()) {

			boolean minSDcheck = true;
			boolean maxSDcheck = true;
			boolean minEDcheck = true;
			boolean maxEDcheck = true;

			if (minSD) {
				minSDcheck = startDateMin <= t.getStartTime().getTimeInMillis();
			}

			if (maxSD) {
				minSDcheck = startDateMax >= t.getStartTime().getTimeInMillis();
			}

			if (minED) {
				minSDcheck = endDateMin <= t.getEndTime().getTimeInMillis();
			}

			if (maxED) {
				minSDcheck = endDateMax >= t.getEndTime().getTimeInMillis();
			}

			if (minSDcheck && maxSDcheck && minEDcheck && maxEDcheck) {
				result.add(t);
			}

		}
		fp.setTasksPool(result);

	}

}
