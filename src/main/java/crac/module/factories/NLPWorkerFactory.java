package crac.module.factories;

import java.util.ArrayList;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.models.db.entities.CompetenceArea;
import crac.models.db.daos.CompetenceAreaDAO;
import crac.models.db.entities.Competence;
import crac.module.matching.superclass.NLPWorker;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.Getter;
import lombok.Setter;

@Component
@Scope("prototype")
public class NLPWorkerFactory{
	
    @Value("${crac.nlp.taggerDirectory}") String taggerDirectory;
	
	@Autowired
	@Getter
	@Setter
	private CompetenceAreaDAO competenceAreaDAO;
	
	private StanfordCoreNLP pipeline;
	
	private void buildNLPPipeline(String taggerDir){
		Properties props = new Properties();
		props.setProperty("customAnnotatorClass.german.lemma", "crac.module.nlp.TreeTaggerAnnotator");
		props.setProperty("annotators", "tokenize, ssplit, pos, german.lemma, ner, parse, regexner"); 
		props.setProperty("tokenize.language", "de"); 
		
		props.setProperty("pos.model", "src/main/java/crac/module/nlp/resources/german-hgc.tagger");
		props.setProperty("treetagger.home", taggerDir);
		props.setProperty("ner.model", "src/main/java/crac/module/nlp/resources/german.conll.hgc_175m_600.crf.ser.gz");
		props.setProperty("parse.model", "src/main/java/crac/module/nlp/resources/germanFactored.ser.gz");
		props.setProperty("regexner.mapping", "src/main/java/crac/module/nlp/resources/gaz_WDS.txt");
		props.setProperty("ner.useSUTime", "0");
						
		pipeline = new StanfordCoreNLP(props);
		System.out.println("-------------------------------");
		System.out.println(" ||||NLP APPLICATION BUILT||||");
		System.out.println("-------------------------------");
	}
	
	@PostConstruct
	private void init(){
		buildNLPPipeline(taggerDirectory);
		//buildNLPPipeline("/opt/tree-tagger");
	}

	public <T extends NLPWorker> NLPWorker createWorker(Class<T> type, Object param) {
		NLPWorker w = BeanUtils.instantiate(type);    
        w.injectParam(param);
		w.setWf(this);
		return w;
	}
	
	public StanfordCoreNLP getPipeline(){
		return pipeline;
	}
	
	public ArrayList<CompetenceArea> getCompetenceAreas(String ann){
		ArrayList<CompetenceArea> compAreas = new ArrayList<CompetenceArea>();
		
		Iterable<CompetenceArea> compAreaIt = competenceAreaDAO.findAll();
		for (CompetenceArea ca: compAreaIt){
			if (ca.getName().equals(ann))
				compAreas.add(ca);
		}
		return compAreas;
	}
	
	public ArrayList<Competence> getCompetences(String ann){
		ArrayList<Competence> competences = new ArrayList<Competence>();
		
	    for(CompetenceArea ca: getCompetenceAreas(ann)){
	    	competences.addAll(new ArrayList<Competence>(ca.getMappedCompetences()));
	    }
	    return competences;
	}
	
}
