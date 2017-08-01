package crac.models.output;

import crac.models.db.entities.Competence;
import crac.module.matching.helpers.AugmentedSimpleCompetence;

public class CompetenceGraphDetails implements Comparable<Object>{
	
	private long id;

	private String name;

	private String description;

	private double similarity;
	
	public CompetenceGraphDetails(AugmentedSimpleCompetence ac){
		Competence c = ac.getConcreteComp();
		this.id = c.getId();
		this.name = c.getName();
		this.description = c.getDescription();
		this.similarity = ac.getSimilarity();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	@Override
	public int compareTo(Object obj) {
		if(((CompetenceGraphDetails) obj).getSimilarity() > this.getSimilarity()){
			return 1;
		}else if(((CompetenceGraphDetails) obj).getSimilarity() == this.getSimilarity()){
			return 0;
		}else{
			return -1;
		}
	}

}
