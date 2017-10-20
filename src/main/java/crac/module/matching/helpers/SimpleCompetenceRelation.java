package crac.module.matching.helpers;

import lombok.Data;

/**
 * Helperclass that represents a simplified version of the competence-relationship-class
 * @author David Hondl
 *
 */
@Data
public class SimpleCompetenceRelation {
	
	private SimpleCompetence related;
	private double distance;
	
	public SimpleCompetenceRelation(SimpleCompetence related, double distance) {
		this.related = related;
		this.distance = distance;
	}

}
