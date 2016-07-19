package crac.utility;

import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import crac.elastic.ElasticCompetence;
import crac.elastic.ElasticUser;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.relationmodels.UserCompetenceRel;
import crac.elastic.ElasticTask;

@Service
public class SearchTransformer {
	
	@Autowired
	private CompetenceAugmenter caug;
	
	public ElasticTask transformTask(Task originalTask){
		return transformTaskInternDirectly(originalTask);
	}
	
	public ElasticUser transformUser(CracUser originalUser){
		return transformUserInternDirectly(originalUser);
	}

	private ElasticUser transformUserInternDirectly(CracUser c){
		
		ElasticUser eu = new ElasticUser(c.getId(), c.getName());
		
		HashSet<ElasticCompetence> cSet = new HashSet<ElasticCompetence>();
				
		HashSet<ElasticCompetence> cSetAug = new HashSet<ElasticCompetence>();
		
		for(UserCompetenceRel co : c.getCompetenceRelationships()){
			Competence comp = co.getCompetence();
			cSet.add(new ElasticCompetence(comp.getId(), comp.getName(), comp.getDescription()));
		}
		
		for(UserCompetenceRel r : c.getCompetenceRelationships()){
			caug.augmentWithDistance(r.getCompetence(), 8, 8, cSetAug);
		}
		//TODO - add augmenter
		
		eu.setSetCompetences(cSet);
		
		eu.setRelatedCompetences(cSetAug);
		
		return eu;
	}
	
	private ElasticTask transformTaskInternDirectly(Task t){
		
		ElasticTask et = new ElasticTask(""+t.getId(), t.getName(), t.getDescription());
		
		HashSet<ElasticCompetence> cSet = new HashSet<ElasticCompetence>();
		
		for(Competence c: t.getNeededCompetences()){
			cSet.add(new ElasticCompetence(c.getId(), c.getName(), c.getDescription()));
		}
		
		et.setNeededCompetences(cSet);
		
		return et;
	}

}
