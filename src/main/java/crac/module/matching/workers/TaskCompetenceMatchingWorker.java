package crac.module.matching.workers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import crac.module.matching.superclass.NLPWorker;
import crac.models.db.entities.Competence;
import crac.models.db.entities.Task;
import edu.stanford.nlp.pipeline.Annotation;

public class TaskCompetenceMatchingWorker extends NLPWorker {
	
	private Task task;
	
	@Override
	public void injectParam(Object param) {
		this.task = (Task) param;
	}

	@Override
	public ArrayList<Competence> run() {
	    
		Set<String> compAnn = extractCompetenceAnnotations(new Annotation(task.getName() + " " + task.getDescription()));
		
		Set<Competence> competences = new HashSet<Competence>();
		for (String cAnn: compAnn){
			competences.addAll(getWf().getCompetences(cAnn));
		}		
		return new ArrayList<Competence>(competences);
	}
}
