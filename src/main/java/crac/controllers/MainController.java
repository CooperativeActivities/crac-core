package crac.controllers;

import java.sql.Timestamp;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import crac.daos.CompetenceDAO;
import crac.daos.CracUserDAO;
import crac.daos.ProjectDAO;
import crac.daos.TaskDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.elastic.ElasticConnector;
import crac.elastic.ElasticUser;
import crac.elastic.ElasticTask;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Project;
import crac.models.Role;
import crac.models.SearchTransformer;
import crac.models.Task;
import crac.models.UserCompetenceRel;
import crac.models.UserTaskRel;

/**
 * The main-controller used for hello world and testing
 */
@Controller
public class MainController {
	
	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private TaskDAO taskDAO;
	
	@Autowired
	private ProjectDAO projectDAO;
	
	@Autowired
	private UserCompetenceRelDAO userCompetenceRelDAO;
	
	
	private ElasticConnector<ElasticUser> ESConnUser = new ElasticConnector<ElasticUser>("localhost", 9300, "crac_core", "elastic_user");
	private SearchTransformer ST = new SearchTransformer();
	private ElasticConnector<ElasticTask> ESConnTask = new ElasticConnector<ElasticTask>("localhost", 9300, "crac_core", "elastic_task");


	@Value("${custom.elasticUrl}")
    private String url;
	
	@Value("${custom.elasticPort}")
    private int port;
	
	@RequestMapping("/test")
	@ResponseBody
	public String index() {
		System.out.println("elastic url: "+url);
		System.out.println("elastic port: "+port);
		return "Hello World! This is just a test!";

	}

	@RequestMapping("/testMe")
	@ResponseBody
	public String indexWord() {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return "Logged in as " + userDetails.getUsername();
	}

	@SuppressWarnings("deprecation")
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/boot")
	@ResponseBody
	public ResponseEntity<String> boot() {
		
		if(userDAO.findByName("Webmaster") != null){
			return ResponseEntity.ok().body("{\"booted\":\"false\", \"exception\":\"already_booted\"}");
		}
		
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser myUser = userDAO.findByName(userDetails.getUsername());
		
		//Add competences
		
		Competence breathing = new Competence();
		breathing.setCreator(myUser);
		breathing.setDescription("Beeing to stay alive by inhaling air.");
		breathing.setName("breathing");
		
		Competence walking = new Competence();
		walking.setCreator(myUser);
		walking.setDescription("Getting slowly from one point to another using human legs.");
		walking.setName("walking");
		
		Competence swimming = new Competence();
		swimming.setCreator(myUser);
		swimming.setDescription("Not drowning while in water.");
		swimming.setName("swimming");
		
		Competence programming = new Competence();
		programming.setCreator(myUser);
		programming.setDescription("Beeing able to write computer programs.");
		programming.setName("programming");
		
		Competence javascriptProgramming = new Competence();
		javascriptProgramming.setCreator(myUser);
		javascriptProgramming.setDescription("Beeing able to write computer programs with/in JavaScript and it's libraries.");
		javascriptProgramming.setName("javascript-programming");
		
		Competence phpProgramming = new Competence();
		phpProgramming.setCreator(myUser);
		phpProgramming.setDescription("Beeing able to write computer programs with/in PHP and it's libraries.");
		phpProgramming.setName("php-programming");
		
		competenceDAO.save(breathing);
		competenceDAO.save(walking);
		competenceDAO.save(swimming);
		competenceDAO.save(programming);
		competenceDAO.save(javascriptProgramming);
		competenceDAO.save(phpProgramming);
		
		//Add projects
				
		Project waterFlowers = new Project();
		waterFlowers.setName("Water the flowers");
		waterFlowers.setDescription("All about watering the different flowers in the garden.");
		waterFlowers.setLocation("my garden");
		waterFlowers.setStartTime(new Timestamp(2016, 9, 10, 14, 30, 0, 0));
		waterFlowers.setEndTime(new Timestamp(2016, 9, 10, 17, 00, 0, 0));
		waterFlowers.setCreator(myUser);
		
		projectDAO.save(waterFlowers);
		
		//Add tasks
		
		Task waterRoses = new Task();
		waterRoses.setName("Water the roses");
		waterRoses.setDescription("Water the roses on the westside of the garden.");
		waterRoses.setLocation("my garden");
		waterRoses.setStartTime(new Timestamp(2016, 9, 10, 15, 00, 0, 0));
		waterRoses.setEndTime(new Timestamp(2016, 9, 10, 17, 00, 0, 0));
		waterRoses.setUrgency(5);
		waterRoses.setAmountOfVolunteers(2);
		waterRoses.setNeededCompetences(new HashSet<Competence>());
		waterRoses.getNeededCompetences().add(breathing);
		waterRoses.getNeededCompetences().add(walking);
		waterRoses.setCreator(myUser);
		waterRoses.setSuperProject(waterFlowers);
		
		Task waterLilies = new Task();
		waterLilies.setName("Water the lillies");
		waterLilies.setDescription("Water the lilies on the eastside of the garden.");
		waterLilies.setLocation("my garden");
		waterLilies.setStartTime(new Timestamp(2016, 9, 10, 14, 30, 0, 0));
		waterLilies.setEndTime(new Timestamp(2016, 9, 10, 16, 00, 0, 0));
		waterLilies.setUrgency(2);
		waterLilies.setAmountOfVolunteers(1);
		waterLilies.setNeededCompetences(new HashSet<Competence>());
		waterLilies.getNeededCompetences().add(breathing);
		waterLilies.getNeededCompetences().add(walking);
		waterLilies.setCreator(myUser);
		waterLilies.setSuperProject(waterFlowers);
		
		Task programWateringTool = new Task();
		programWateringTool.setName("Program a watering tool");
		programWateringTool.setDescription("Program a web-tool that makes watering flowers easier.");
		programWateringTool.setLocation("a desk in my garden");
		programWateringTool.setStartTime(new Timestamp(2016, 9, 10, 14, 30, 0, 0));
		programWateringTool.setEndTime(new Timestamp(2016, 9, 10, 17, 00, 0, 0));
		programWateringTool.setUrgency(10);
		programWateringTool.setAmountOfVolunteers(1);
		programWateringTool.setNeededCompetences(new HashSet<Competence>());
		programWateringTool.getNeededCompetences().add(breathing);
		programWateringTool.getNeededCompetences().add(walking);
		programWateringTool.getNeededCompetences().add(programming);
		programWateringTool.getNeededCompetences().add(phpProgramming);
		programWateringTool.getNeededCompetences().add(javascriptProgramming);
		programWateringTool.setCreator(myUser);
		programWateringTool.setSuperProject(waterFlowers);
		
		waterFlowers.setChildTasks(new HashSet<Task>());
		waterFlowers.getChildTasks().add(waterRoses);
		waterFlowers.getChildTasks().add(waterLilies);
		waterFlowers.getChildTasks().add(programWateringTool);
		
		projectDAO.save(waterFlowers);
		
		for(Task t : waterFlowers.getChildTasks()){
			ESConnTask.indexOrUpdate(""+t.getId(), ST.transformTask(t));
		}

		//Add users
		
		CracUser Webmaster = new CracUser();
		
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
		
		Webmaster.setName("Webmaster");
		Webmaster.setFirstName("Max");
		Webmaster.setLastName("Mustermann");
		Webmaster.setCompetenceRelationships(new HashSet<UserCompetenceRel>());
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, breathing));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, walking));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, swimming));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, programming));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, phpProgramming));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, javascriptProgramming));
		Webmaster.setPassword(bcryptEncoder.encode("noOneKnowsThisPassword!1!1"));
		Webmaster.setRole(Role.USER);
		Webmaster.setPhone("0987656789098");
		Webmaster.setEmail("Webmaster@internet.at");
		
		CracUser AverageHuman = new CracUser();
		
		AverageHuman.setName("AverageHuman");
		AverageHuman.setFirstName("Hans");
		AverageHuman.setLastName("Musterhans");
		AverageHuman.setCompetenceRelationships(new HashSet<UserCompetenceRel>());
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, breathing));
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, walking));
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, swimming));
		AverageHuman.setPassword(bcryptEncoder.encode("noOneKnowsThisPasswordAnyway!1!1"));
		AverageHuman.setRole(Role.USER);
		AverageHuman.setPhone("35678987654");
		AverageHuman.setEmail("AverageHuman@internet.at");
		
		userDAO.save(Webmaster);
		ESConnUser.indexOrUpdate(""+Webmaster.getId(), ST.transformUser(Webmaster));
		userDAO.save(AverageHuman);
		ESConnUser.indexOrUpdate(""+AverageHuman.getId(), ST.transformUser(AverageHuman));
		
		
		CracUser original1 = userDAO.findOne((long)1);
		CracUser original2 = userDAO.findOne((long)2);
		
		ESConnUser.indexOrUpdate(""+original1.getId(), ST.transformUser(original1));
		ESConnUser.indexOrUpdate(""+original2.getId(), ST.transformUser(original2));
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

		return ResponseEntity.ok().headers(headers).body("{\"booted\":\"true\"}");
	}

}
