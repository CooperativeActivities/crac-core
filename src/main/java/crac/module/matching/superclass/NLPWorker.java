package crac.module.matching.superclass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import crac.module.factories.NLPWorkerFactory;
import crac.module.utility.CracUtility;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public abstract class NLPWorker {
	
		private String workerId;
		
		private NLPWorkerFactory wf;
		
		protected CoreMapExpressionExtractor<MatchedExpression> annotationExtractor;
		
		protected Annotation text; 
		
		public NLPWorker(){
			this.workerId = CracUtility.randomString(20);
			System.out.println("_______________________");
			System.out.println("NLPWorker with ID "+this.workerId+" running!");
			System.out.println("_______________________");
		}
		
		public abstract Object run();
		
		public abstract void injectParam(Object param);

		public void setWf(NLPWorkerFactory wf) {
			this.wf = wf;
		}

		public NLPWorkerFactory getWf() {
			return wf;
		}

		protected Set<String> extractCompetenceAnnotations(Annotation annotation){
			
			wf.getPipeline().annotate(annotation);
			
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
