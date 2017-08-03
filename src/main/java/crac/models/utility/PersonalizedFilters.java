package crac.models.utility;

import java.util.ArrayList;

import crac.module.matching.superclass.ConcreteFilter;

public class PersonalizedFilters {

	private String query;

	private String[] filters;

	private ArrayList<Class<ConcreteFilter>> filtersClass;

	private ArrayList<ConcreteFilter> filtersObj;

	public PersonalizedFilters() {
		query = "";
		filtersObj = new ArrayList<>();
		filtersClass = new ArrayList<>();
	}

	private void convert() {

		for (String s : filters) {
			Class<?> c = null;
			try {
				c = Class.forName("crac.module.matching.filter.prematching." + s);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if (c != null) {
				filtersClass.add((Class<ConcreteFilter>) c);
			}
		}
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void addFilter(ConcreteFilter filter) {
		filtersObj.add(filter);
	}

	public String[] getFilters() {
		return filters;
	}

	public void setFilters(String[] filters) {
		this.filters = filters;
		convert();
	}

	public ArrayList<Class<ConcreteFilter>> getFiltersClass() {
		return filtersClass;
	}

	public void setFiltersClass(ArrayList<Class<ConcreteFilter>> filtersClass) {
		this.filtersClass = filtersClass;
	}

	public ArrayList<ConcreteFilter> getFiltersObj() {
		return filtersObj;
	}

	public void setFiltersObj(ArrayList<ConcreteFilter> filtersObj) {
		this.filtersObj = filtersObj;
	}

}
