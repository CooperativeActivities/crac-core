package crac.module.matching.workers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import crac.module.matching.superclass.NLPWorker;
import crac.models.db.entities.CompetenceArea;
import crac.models.db.entities.Task;
import edu.stanford.nlp.pipeline.Annotation;

public class TaskCompetenceAreaMatchingWorker extends NLPWorker {
	
	private Task task;
	
	public TaskCompetenceAreaMatchingWorker(Task task){
		super();
		this.task = task;
	}

	@Override
	public ArrayList<CompetenceArea> run() {
	    
		Set<String> compAnn = extractCompetenceAnnotations(new Annotation(task.getName() + " " + task.getDescription()));
		
		Set<CompetenceArea> compAreas = new HashSet<CompetenceArea>();
		for (String cAnn: compAnn){
			compAreas.addAll(getWf().getCompetenceAreas(cAnn));
		}		
		return new ArrayList<CompetenceArea>(compAreas);
	}
}
