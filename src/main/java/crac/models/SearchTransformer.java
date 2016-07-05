package crac.models;

import java.util.HashSet;

import org.springframework.stereotype.Service;

import crac.elastic.ElasticCompetence;
import crac.elastic.ElasticUser;
import crac.elastic.ElasticTask;

@Service
public class SearchTransformer {
	
	public ElasticTask transformTask(Task originalTask){
		return transformTaskInternDirectly(originalTask);
	}
	
	public ElasticUser transformUser(CracUser originalUser){
		return transformUserInternDirectly(originalUser);
	}

	private ElasticUser transformUserInternDirectly(CracUser c){
		
		ElasticUser p = new ElasticUser(c.getId(), c.getName());
		
		HashSet<ElasticCompetence> cSet = new HashSet<ElasticCompetence>();
				
		HashSet<ElasticCompetence> cSetAug = new HashSet<ElasticCompetence>();
		
		for(UserCompetenceRel co : c.getCompetenceRelationships()){
			Competence comp = co.getCompetence();
			cSet.add(new ElasticCompetence(comp.getId(), comp.getName(), comp.getDescription()));
		}
		
		//TODO - add augmenter
		
		p.setSetCompetences(cSet);
		
		return p;
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
