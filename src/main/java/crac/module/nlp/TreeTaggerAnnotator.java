package crac.module.nlp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import java.util.List;

import java.util.Properties;
import java.util.Set;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;

public class TreeTaggerAnnotator implements Annotator {
	
	private final String treetaggerHome;

	public TreeTaggerAnnotator(Properties props){
		treetaggerHome = props.getProperty("treetagger.home");
	}
	
	@Override
	public void annotate(Annotation annotation) {
		   TreeTaggerWrapper<String> tt = new TreeTaggerWrapper<String>();
		  
		   List<CoreLabel> tokenList = annotation.get(CoreAnnotations.TokensAnnotation.class);
		   String[] tokens = new String[tokenList.size()];
		   for(int i=0; i<tokenList.size(); i++){
			   tokens[i] = tokenList.get(i).word();
		   }
		   try {
			   System.setProperty("treetagger.home", treetaggerHome);
			   tt.setModel(treetaggerHome+"/lib/german.par");
			   tt.setHandler(new TokenHandler<String>(){
				   int i = 0;
				   public void token(String token, String pos, String lemma ){
					   CoreLabel t = tokenList.get(i);
					   t.set(CoreAnnotations.LemmaAnnotation.class, lemma);
					   i++;
				   }
			   });
			   tt.process(tokens);
		   } catch (TreeTaggerException e) {
			  e.printStackTrace();
		   } catch (IOException e) {
			e.printStackTrace();
		   }
	}
	
	@Override
	public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {		
		return Collections.singleton(CoreAnnotations.LemmaAnnotation.class);
	}

	@Override
	public Set<Class<? extends CoreAnnotation>> requires() {
		return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(CoreAnnotations.TextAnnotation.class,
				                                                        CoreAnnotations.TokensAnnotation.class,
				                                                        CoreAnnotations.SentencesAnnotation.class)));
	}
	
}
