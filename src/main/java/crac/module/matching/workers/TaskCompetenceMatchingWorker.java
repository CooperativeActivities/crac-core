package crac.module.matching.workers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import crac.module.matching.superclass.NLPWorker;
import crac.models.db.entities.Competence;
import crac.models.db.entities.Task;
import edu.stanford.nlp.io.EncodingPrintWriter.out;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class TaskCompetenceMatchingWorker extends NLPWorker {
	
	private Task task;
	
	public TaskCompetenceMatchingWorker(Task task){
		super();
		this.task = task;
	}

	@Override
	public ArrayList<Competence> run() {
		// out.println(".... Task name: " + task.getName() + ", task desc: " + task.getDescription());
		Annotation text = new Annotation(task.getName() + " " + task.getDescription()); 
		
		StanfordCoreNLP pipeline = getWf().getPipeline();
		
		pipeline.annotate(text);
		
		Set<String> compAnn = new HashSet<String>();
		
		CoreMapExpressionExtractor<MatchedExpression> extractor = getWf().getAnnotationExtractor();
		
		List<CoreMap> sentences = text.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && !sentences.isEmpty()){
			for (CoreMap sentence : sentences){
				
				extractor.extractExpressions(sentence);
			
				for (CoreLabel token: sentence.get(TokensAnnotation.class)){
					String word = token.get(CoreAnnotations.TextAnnotation.class);
			          String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
			          String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
			          String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
			          String normalized = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
			          String cat = token.get(CoreAnnotations.CategoryAnnotation.class);
			          String parse = token.get(CoreAnnotations.CategoryAnnotation.class);
			          //String mention = token.get(CoreAnnotations.MentionsAnnotation.class);
			         // out.println("  Matched token: " + "word="+word + ", lemma="+lemma + ", pos=" + pos + ", ne=" + ne + ", normalized=" + normalized);
			         //  out.println("      cat="+cat + ", parse="+parse + ", mention=" + parse + ", ne=" + ne + ", normalized=" + normalized);
					if (token.get(CompetenceAnnotation.class) != null){
						// out.println("token: " + token.originalText() + ", competence: " + token.get(CompetenceAnnotation.class));
						compAnn.add(token.get(CompetenceAnnotation.class));
					}
				}
			}
		}
		Set<Competence> competences = new HashSet<Competence>();
		for (String cAnn: compAnn){
			competences.addAll(getWf().getCompetences4Annotation(cAnn));
		}		
		out.println("competences found: " + competences.size());
		return new ArrayList<Competence>(competences);
	}
	
	
	/**
	   * The CoreMap key identifying the annotation's text.
	   *
	   * Note that this key is intended to be used with many different kinds of
	   * annotations - documents, sentences and tokens all have their own text.
	   */
	  public static class CompetenceAnnotation implements CoreAnnotation<String> {
	    @Override
	    public Class<String> getType() {
	      return String.class;
	    }
	  }
}
