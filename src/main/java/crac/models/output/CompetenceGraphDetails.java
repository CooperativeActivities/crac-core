package crac.models.output;

import crac.models.db.entities.Competence;
import crac.module.matching.helpers.AugmentedSimpleCompetence;
import lombok.Data;

@Data
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
