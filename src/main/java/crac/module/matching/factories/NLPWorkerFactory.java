package crac.module.matching.factories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.models.db.entities.CompetenceArea;
import crac.models.db.daos.CompetenceAreaDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.Task;
import crac.module.matching.superclass.NLPWorker;
import crac.module.matching.workers.TaskCompetenceMatchingWorker;
import crac.module.storage.CompetenceStorage;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.Getter;
import lombok.Setter;

@Component
@Scope("prototype")
public class NLPWorkerFactory{
	
    @Value("${crac.nlp.taggerDirectory}") String taggerDirectory;
	
	@Autowired
	private CompetenceAreaDAO competenceAreaDAO;
	
	@Autowired
	private CompetenceStorage cs;
	
	private StanfordCoreNLP pipeline;
	
	private CoreMapExpressionExtractor<MatchedExpression> annotationExtractor;
	
	private HashMap<CompetenceArea, Set<Competence>> area2Competences;
	
	private HashMap<String, Set<CompetenceArea>> ann2CompetenceAreas;
	
	private boolean annotationsMapped;

	
	private void buildNLPPipeline(String taggerDir){
		Properties props = new Properties();
		props.setProperty("customAnnotatorClass.german.lemma", "crac.module.nlp.TreeTaggerAnnotator");
		props.setProperty("annotators", "tokenize, ssplit, pos, german.lemma, ner, regexner"); // XXX parse out vor regexner
		props.setProperty("tokenize.language", "de"); 
		
		props.setProperty("pos.model", "crac/module/nlp/resources/german-hgc.tagger");
		props.setProperty("treetagger.home", taggerDir);
		props.setProperty("ner.model", "crac/module/nlp/resources/german.conll.hgc_175m_600.crf.ser.gz");
		props.setProperty("parse.model", "crac/module/nlp/resources/germanFactored.ser.gz");
		props.setProperty("regexner.mapping", "crac/module/nlp/resources/gaz_WDS.txt");
		props.setProperty("ner.useSUTime", "0");
						
		pipeline = new StanfordCoreNLP(props);
		System.out.println("-------------------------------");
		System.out.println(" ||||NLP APPLICATION BUILT||||");
		System.out.println("-------------------------------");
	}
	
	@PostConstruct
	private void init(){
		
		buildNLPPipeline(taggerDirectory);
		
		annotationsMapped = false;
			
		// ann2CompetenceAreas = new HashMap<String, Set<CompetenceArea>>();
		// area2Competences = new HashMap<CompetenceArea, Set<Competence>>();
		// setupAnn2CompetencesMapping();
		if (!annotationsMapped)
			setupAnn2CompetencesMapping();
	}

	public <T extends NLPWorker> NLPWorker createWorker(Class<T> type, HashMap<String, Object> params) {
		if (!annotationsMapped)
			setupAnn2CompetencesMapping();
		annotationExtractor = CoreMapExpressionExtractor.createExtractorFromFile(TokenSequencePattern.getNewEnv(), "crac/module/nlp/resources/competence_extraction_rules.txt" ); 
		TaskCompetenceMatchingWorker w = new TaskCompetenceMatchingWorker((Task)params.get("task"));
		w.setWf(this);
		return w;
	}
	
	public StanfordCoreNLP getPipeline(){
		return pipeline;
	}
	
	public CoreMapExpressionExtractor<MatchedExpression> getAnnotationExtractor(){
		return annotationExtractor;
	}

	private void setupAnn2CompetencesMapping(){
		ann2CompetenceAreas = new HashMap<String, Set<CompetenceArea>>();
		area2Competences = new HashMap<CompetenceArea, Set<Competence>>();
		
		// set up hashmap annotation to compentence areas
		Iterable<CompetenceArea> compAreaIt = competenceAreaDAO.findAll();
		for (CompetenceArea ca: compAreaIt){
			if (!ann2CompetenceAreas.containsKey(ca.getName()))
				ann2CompetenceAreas.put(ca.getName(), new HashSet<CompetenceArea>());
			ann2CompetenceAreas.get(ca.getName()).add(ca);
		}
		
		// set up hashmap competence area to competences
		Iterable<Competence> compIt = cs.getCompetenceDAO().findAll();
		for (Competence c: compIt){
			Set<CompetenceArea> cas = c.getCompetenceAreas();
			for (CompetenceArea ca: cas){
				if (!area2Competences.containsKey(ca))
					area2Competences.put(ca, new HashSet<Competence>());
				area2Competences.get(ca).add(c);
			}
		}
		annotationsMapped = true;
		printArea2Competences();
	}
	
	private void printArea2Competences(){
		System.out.println("******************* Annotation 2 Competence Areas ***********************");
		Set<String> annKeys = ann2CompetenceAreas.keySet();
		for (String ann: annKeys){
			System.out.println("Ann: " + ann);
			ArrayList<CompetenceArea> cas = getCompetenceAreas4Annotation(ann);
			for (CompetenceArea ca: cas){
				System.out.println(".... " + ca.getName());
			}
		}
		
		System.out.println("******************* Compeptence Areas 2 Competences ***********************");
	    Set<CompetenceArea> ks = area2Competences.keySet();
	    for (CompetenceArea ca: ks){
	    	System.out.println(" competence area: " + ca.getName());
	    	ArrayList<Competence> cs = getCompetences4Annotation(ca.getName());
	    	for (Competence c: cs){
	    		System.out.println("............. competence: " + c.getName() + " ... " + c.getDescription());
	    	}
	    }
	}
	
	public ArrayList<Competence> getCompetences4Annotation(String compAnn){
		if (area2Competences.isEmpty())
			setupAnn2CompetencesMapping();
		Set<CompetenceArea> ks = area2Competences.keySet();
	    for (CompetenceArea ca: ks){
	    	if (ca.getName().equals(compAnn))
	    		return new ArrayList<Competence>(area2Competences.get(ca));
	    }
	    return new ArrayList<Competence>();
	}
	
	public ArrayList<CompetenceArea> getCompetenceAreas4Annotation(String compAnn){
		if (ann2CompetenceAreas.isEmpty())
			setupAnn2CompetencesMapping();
		return new ArrayList<CompetenceArea>(ann2CompetenceAreas.get(compAnn));
	}
	
}
