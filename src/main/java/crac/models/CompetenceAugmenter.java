package crac.models;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import crac.daos.CompetenceDAO;
import crac.daos.CompetenceRelationshipDAO;

public final class CompetenceAugmenter {

	@Autowired
	private CompetenceDAO competenceDAO;
	
	@Autowired
	private CompetenceRelationshipDAO relationDAO;
	
	public void augmentWithNumber(Competence c, int numberOfSteps, Set<Competence> relatedCompetences){
		
		List<CompetenceRelationship> list1 = relationDAO.findByCompetence1In(c);
		List<CompetenceRelationship> list2 = relationDAO.findByCompetence2In(c);
		
		for(CompetenceRelationship cr : list1){
			Competence targetC = cr.getCompetence2();
			relatedCompetences.add(targetC);
			if(relatedCompetences.contains(targetC)){
				//do nothing, since no distance is forwarded
			}else{
				relatedCompetences.add(targetC);
				if(numberOfSteps > 0){
					augmentWithNumber(targetC, numberOfSteps - 1, relatedCompetences);
				}
			}
		}
		
		for(CompetenceRelationship cr : list2){
			Competence targetC = cr.getCompetence1();
			if(relatedCompetences.contains(targetC)){
				//do nothing, since no distance is forwarded
			}else{
				relatedCompetences.add(targetC);
				if(numberOfSteps > 0){
					augmentWithNumber(targetC, numberOfSteps - 1, relatedCompetences);
				}
			}
		}
				
	}
	
	public void augmentWithDistance(Competence c, double distanceToGo, Set<Competence> relatedCompetences){
		
		List<CompetenceRelationship> list1 = relationDAO.findByCompetence1In(c);
		List<CompetenceRelationship> list2 = relationDAO.findByCompetence2In(c);
		
		for(CompetenceRelationship cr : list1){
			Competence targetC = cr.getCompetence2();
			relatedCompetences.add(targetC);
			if(relatedCompetences.contains(targetC)){
				//TODO
			}else if(distanceToGo -  cr.getDistance() > 0){
				relatedCompetences.add(targetC);
				if(distanceToGo > 0){
					augmentWithDistance(targetC, distanceToGo - cr.getDistance(), relatedCompetences);
				}
			}
		}
		
		for(CompetenceRelationship cr : list2){
			Competence targetC = cr.getCompetence1();
			if(relatedCompetences.contains(targetC)){
				//TODO
			}else if(distanceToGo -  cr.getDistance() > 0){
				relatedCompetences.add(targetC);
				if(distanceToGo > 0){
					augmentWithDistance(targetC, distanceToGo -  cr.getDistance(), relatedCompetences);
				}
			}
		}
		
		
	}
	
	
}
