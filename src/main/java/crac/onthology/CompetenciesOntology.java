package crac.onthology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CompetenciesOntology {

	public static final String NS = "http://www.fhhagenberg.at/crac/compentencies";
	private OntModel model;
	private JSONConverter converter = new JSONConverter();
	private ElasticSearchAdapter adapter = new ElasticSearchAdapter();
	//private Neo4JConnector connector = new Neo4JConnector();
	
	public CompetenciesOntology() {
		//connector.init("C:/CompentenceNeo4JDB");
	}
	
	
	
	public void initializeCompetencies(){
		intializeTBox();
		intializeABox(model);
	}
	
	
	private void intializeTBox(){
		model = ModelFactory.createOntologyModel();
		InputStream is = FileManager.get().open("./resources/competencies.owl");
		model.read(is, NS);

		//connector.initializeTBox(model);
		
		//intializeABox(model);
	}
	

	
	private void intializeABox(OntModel model){
		
	}
	
	
	public void addPerson(String personId) {
		OntClass person = model.getOntClass(NS + "#Person");
		// create ABox for User
		Individual i = person.createIndividual(NS + "#" + personId);
		JSONObject jsonPers = converter.convertIndividual(i, model);
		//System.out.println(jsonPers);
		adapter.addPerson(jsonPers,personId);
	}
	
	public void addBasicCompetency(String competencyId){
		OntClass compentency = model.getOntClass(NS + "#Competency");
		compentency.createIndividual(NS + "#" + competencyId);
	}
	
	public void addSubCompentency(String newCompentencyId, String superCompentencyId){
		
	}
	
	public void addCompetencyToPerson(String personId, String competencyId, int profLevel){
		Individual person = model.getIndividual(NS + "#"+personId);
		Individual competence = model.getIndividual(NS+"#"+ competencyId);
		ObjectProperty isCompetency = model.getObjectProperty(NS + "#isCompetency");
		ObjectProperty hasCompetency = model.getObjectProperty(NS + "#hasCompetency");
				
		//createBlank Node
		Resource blankNode = model.createResource();
		DatatypeProperty level = model.getDatatypeProperty(NS + "#level");
		blankNode.addProperty(level, Integer.toString(profLevel));
		blankNode.addProperty(isCompetency, competence);
		
		person.addProperty(hasCompetency, blankNode);		
		
		JSONObject jsonPers = converter.convertIndividual(person,model);
		adapter.addPerson(jsonPers,personId);
	}



	public void print() {
		model.write(System.out, "RDF/XML");		
	}

}
