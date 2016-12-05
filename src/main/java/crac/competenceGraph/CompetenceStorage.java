package crac.competenceGraph;

import java.util.HashMap;

import crac.daos.CompetenceDAO;
import crac.daos.CompetenceRelationshipDAO;
import crac.models.Competence;
import crac.relationmodels.CompetenceRelationship;

public class CompetenceStorage {
	
	private boolean synced = false;
	
	private HashMap<Long, SimpleCompetence> competences = new HashMap<Long, SimpleCompetence>();
	
	private static CompetenceStorage instance = new CompetenceStorage();

	public static String sync(CompetenceDAO competenceDAO, CompetenceRelationshipDAO compRelDAO){
				
		for(Competence c : competenceDAO.findAll()){
			instance.competences.put(c.getId(), new SimpleCompetence(c));
		}
		
		for(CompetenceRelationship cr : compRelDAO.findAll()){
			SimpleCompetence c1 = instance.competences.get(cr.getCompetence1().getId());
			SimpleCompetence c2 = instance.competences.get(cr.getCompetence2().getId());
			c1.addRelation(new SimpleCompetenceRelation(c2, cr.getType().getDistanceVal()));
			c2.addRelation(new SimpleCompetenceRelation(c1, cr.getType().getDistanceVal()));
		}
		
		instance.synced = true;
		return "success";
	}
	
	private CompetenceStorage() {
	}

	public static HashMap<Long, SimpleCompetence> getCompetences() {
		if(instance.synced){
			return instance.competences;
		}else{
			return null;
		}
	}
	
	public static SimpleCompetence getCompetence(Long key){
		if(instance.synced){
			return instance.competences.get(key);
		}else{
			return null;
		}
	}

	public static boolean isSynced(){
		return instance.synced;
	}
	
}
