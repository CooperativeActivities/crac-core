package crac.module.nlp;

import java.util.ArrayList;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import crac.models.db.daos.CompetenceAreaDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CompetenceArea;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Component
@Scope("prototype")
public class NLPProcessing {
	
	 @Value("${crac.nlp.taggerDirectory}") String taggerDirectory;
		
		@Autowired
		@Getter
		@Setter
		private CompetenceAreaDAO competenceAreaDAO;
		
		@Getter
		@Setter
		private StanfordCoreNLP pipeline;
		
		private void buildNLPPipeline(String taggerDir){
			Properties props = new Properties();
			props.setProperty("customAnnotatorClass.german.lemma", "crac.module.nlp.TreeTaggerAnnotator");
			props.setProperty("annotators", "tokenize, ssplit, pos, german.lemma, ner, parse, regexner"); 
			props.setProperty("tokenize.language", "de"); 
			
			props.setProperty("pos.model", "nlp/german-hgc.tagger");
			props.setProperty("treetagger.home", taggerDir);
			props.setProperty("ner.model", "nlp/german.conll.hgc_175m_600.crf.ser.gz");
			props.setProperty("parse.model", "nlp/germanFactored.ser.gz");
			props.setProperty("regexner.mapping", "nlp/gaz_WDS.txt");
			props.setProperty("ner.useSUTime", "0");
							
			pipeline = new StanfordCoreNLP(props);
			System.out.println("-------------------------------");
			System.out.println(" ||||NLP APPLICATION BUILT||||");
			System.out.println("-------------------------------");
		}
		
		@PostConstruct
		private void init(){
			buildNLPPipeline(taggerDirectory);
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
