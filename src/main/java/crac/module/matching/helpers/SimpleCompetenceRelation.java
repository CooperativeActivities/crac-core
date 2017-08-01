package crac.module.matching.helpers;

public class SimpleCompetenceRelation {
	
	private SimpleCompetence related;
	private double distance;
	public SimpleCompetenceRelation(SimpleCompetence related, double distance) {
		this.related = related;
		this.distance = distance;
	}
	public SimpleCompetence getRelated() {
		return related;
	}
	public void setRelated(SimpleCompetence related) {
		this.related = related;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}

}
