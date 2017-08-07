package crac.module.utility.filter.individual;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

		Long startDateMin = (Long) super.getPf().getParam("startDateMin");
		Long startDateMax = (Long) super.getPf().getParam("startDateMax");
		Long endDateMin = (Long) super.getPf().getParam("endDateMin");
		Long endDateMax = (Long) super.getPf().getParam("endDateMax");
		
		boolean minSD = startDateMin != null;
		boolean maxSD = startDateMax != null;
		boolean minED = endDateMin != null;
		boolean maxED = endDateMax != null;
		
		List<Task> result = new ArrayList<>();
		
		for(Task t : fp.getTasksPool()){
			
			System.out.println("entered for task: "+t.getName());
			
			boolean minSDcheck = true;
			boolean maxSDcheck = true;
			boolean minEDcheck = true;
			boolean maxEDcheck = true;
			
			if(minSD){
				minSDcheck = startDateMin <= t.getStartTime().getTimeInMillis();
			}
			
			if(maxSD){
				minSDcheck = startDateMax >= t.getStartTime().getTimeInMillis();
			}
			
			if(minED){
				minSDcheck = endDateMin <= t.getEndTime().getTimeInMillis();
			}
			
			if(maxED){
				minSDcheck = endDateMax >= t.getEndTime().getTimeInMillis();
			}

			if(minSDcheck && maxSDcheck && minEDcheck && maxEDcheck){
				result.add(t);
			}
			
		}
		fp.setTasksPool(result);

	}

}
