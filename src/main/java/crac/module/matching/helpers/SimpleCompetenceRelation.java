package crac.module.matching.helpers;

import lombok.Data;

@Data
public class SimpleCompetenceRelation {
	
	private SimpleCompetence related;
	private double distance;
	
	public SimpleCompetenceRelation(SimpleCompetence related, double distance) {
		this.related = related;
		this.distance = distance;
	}

}
