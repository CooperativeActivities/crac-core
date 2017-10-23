package crac.module.matching.workers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import crac.module.matching.superclass.NLPWorker;
import crac.models.db.entities.Competence;
import crac.models.db.entities.Task;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.pipeline.Annotation;

public class TaskCompetenceMatchingWorker extends NLPWorker {
	
	private Task task;
	
	@Override
	public void injectParam(Object param) {
		HashMap<String, Object> p = (HashMap<String, Object>) param;
		this.task = (Task) p.get("task");
		annotationExtractor = CoreMapExpressionExtractor.createExtractorFromFile(TokenSequencePattern.getNewEnv(), (String) p.get("rules"));
	}

	@Override
	public ArrayList<Competence> run() {
	    
		Set<String> compAnn = extractCompetenceAnnotations(new Annotation(task.getName() + " " + task.getDescription()));
		
		Set<Competence> competences = new HashSet<Competence>();
		for (String cAnn: compAnn){
			competences.addAll(getNlpProcessing().getCompetences(cAnn));
		}		
		return new ArrayList<Competence>(competences);
	}
}
