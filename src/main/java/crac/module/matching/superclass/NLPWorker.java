package crac.module.matching.superclass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import crac.module.nlp.NLPProcessing;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import lombok.Getter;
import lombok.Setter;

public abstract class NLPWorker extends Worker {
		
		@Getter
		@Setter
		private NLPProcessing nlpProcessing;
		
		protected CoreMapExpressionExtractor<MatchedExpression> annotationExtractor;
		
		protected Annotation text; 

		protected Set<String> extractCompetenceAnnotations(Annotation annotation){
			nlpProcessing.getPipeline().annotate(annotation);
			
			Set<String> compAnn = new HashSet<String>();
			
			List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
			if (sentences != null && !sentences.isEmpty()){
				for (CoreMap sentence : sentences){		
					annotationExtractor.extractExpressions(sentence);
					for (CoreLabel token: sentence.get(TokensAnnotation.class)){
						if (token.get(CompetenceAnnotation.class) != null)
							compAnn.add(token.get(CompetenceAnnotation.class));
					}
				}
			}
			return compAnn;
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
