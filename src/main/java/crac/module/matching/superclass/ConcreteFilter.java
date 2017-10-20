package crac.module.matching.superclass;

import com.fasterxml.jackson.annotation.JsonIgnore;

import crac.models.input.PersonalizedFilters.PersonalizedFilter;
import crac.module.factories.CracFilterFactory;
import crac.module.matching.helpers.FilterParameters;
import crac.module.matching.interfaces.CracFilter;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class that implements the CracFilter-interface, defining the basic structure for all filters in this framework
 * @author David Hondl
 *
 */
public abstract class ConcreteFilter implements CracFilter {
	
	private String name;
	
	@Getter
	@Setter
	@JsonIgnore
	private PersonalizedFilter pf;
	
	@Getter
	@Setter
	@JsonIgnore
	private CracFilterFactory cff;
	
	public ConcreteFilter(String name) {
		this.name = name;
		System.out.println(name+" ready!");
}

	/**
	 * Prints that the filter has been added
	 */
	public void addSpeak(){
		System.out.println(name + " has been added!");
	}
	
	/**
	 * Prints, that the filter is available
	 */
	public void speak(){
		System.out.println(name + " is available!");
	}
	
	/**
	 * Returns, that the filter is available as string
	 * @return String
	 */
	public String speakString(){
		return name + " is available!";
	}
	
	/**
	 * The overwritten apply()-method, introduced by the Crac-Filter-interface
	 */
	@Override
	public abstract void apply(FilterParameters fp);
		
}
