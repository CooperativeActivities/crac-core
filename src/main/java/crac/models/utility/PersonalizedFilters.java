package crac.models.utility;

import java.util.ArrayList;

import crac.module.matching.superclass.CracPreMatchingFilter;

public class PersonalizedFilters {

	private String query;

	private String[] filters;

	private ArrayList<Class<CracPreMatchingFilter>> filtersClass;

	private ArrayList<CracPreMatchingFilter> filtersObj;

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
				filtersClass.add((Class<CracPreMatchingFilter>) c);
			}
		}
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void addFilter(CracPreMatchingFilter filter) {
		filtersObj.add(filter);
	}

	public String[] getFilters() {
		return filters;
	}

	public void setFilters(String[] filters) {
		this.filters = filters;
		convert();
	}

	public ArrayList<Class<CracPreMatchingFilter>> getFiltersClass() {
		return filtersClass;
	}

	public void setFiltersClass(ArrayList<Class<CracPreMatchingFilter>> filtersClass) {
		this.filtersClass = filtersClass;
	}

	public ArrayList<CracPreMatchingFilter> getFiltersObj() {
		return filtersObj;
	}

	public void setFiltersObj(ArrayList<CracPreMatchingFilter> filtersObj) {
		this.filtersObj = filtersObj;
	}

}
