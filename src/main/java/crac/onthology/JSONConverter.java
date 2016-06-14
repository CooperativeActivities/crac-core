package crac.onthology;

import java.util.ArrayList;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONConverter {
	
	@SuppressWarnings("unchecked")
	public JSONObject convertIndividual(Individual i, OntModel model){
		JSONObject person = new JSONObject();
		person.put("id", i.getURI());
		//Resource node = i.getPropertyResourceValue(model.getProperty(CompetenciesOntology.NS + "#hasCompetency"));
		//System.out.println(node);
		StmtIterator iter = i.listProperties();
		
		ArrayList<JSONObject> competencies = new ArrayList<JSONObject>();
		while(iter.hasNext()){
			Statement stat = iter.next();
			if(stat.getPredicate().equals(model.getProperty(CompetenciesOntology.NS + "#hasCompetency"))){
				RDFNode blankNode = stat.getObject();
				//System.out.println(blankNode);
				Resource competency = blankNode.asResource().getPropertyResourceValue(model.getProperty(CompetenciesOntology.NS + "#isCompetency"));
				Statement level = blankNode.asResource().getProperty(model.getProperty(CompetenciesOntology.NS + "#level"));
				//System.out.println(competency+":"+level.getLiteral().toString());
				JSONObject c = new JSONObject();
				c.put("competency", competency.getURI());
				c.put("level", Integer.parseInt(level.getLiteral().toString()));
				competencies.add(c);
			}
		}
		person.put("competencies", competencies);
		//System.out.println(person);
		return person;
	}
	
	public JSONObject decodeJSON(String json) throws ParseException{
		JSONParser parser = new JSONParser();
		return (JSONObject)parser.parse(json);
	}

}
