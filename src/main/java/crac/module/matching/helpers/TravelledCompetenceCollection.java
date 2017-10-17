package crac.module.matching.helpers;

import java.util.HashMap;

import lombok.Data;

@Data
public class TravelledCompetenceCollection {
	
	private Long mainId;
	private HashMap<Long, TravelledCompetence> stackedCompetences;

	public TravelledCompetenceCollection(Long mainId) {
		this.mainId = mainId;
		this.stackedCompetences = new HashMap<Long, TravelledCompetence>();
	}

	public TravelledCompetenceCollection(Long mainId, HashMap<Long, TravelledCompetence> stackedCompetences) {
		this.mainId = mainId;
		this.stackedCompetences = stackedCompetences;
	}

	public void add(TravelledCompetence c){
		this.stackedCompetences.put(c.getCompetence().getId(), c);
	}	
	
}
