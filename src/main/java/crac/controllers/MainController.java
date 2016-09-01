package crac.controllers;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.daos.CompetenceDAO;
import crac.daos.CompetencePermissionTypeDAO;
import crac.daos.CompetenceRelationshipDAO;
import crac.daos.CompetenceRelationshipTypeDAO;
import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.daos.UserCompetenceRelDAO;
import crac.enums.Role;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.notifier.NotificationHelper;
import crac.relationmodels.CompetencePermissionType;
import crac.relationmodels.CompetenceRelationship;
import crac.relationmodels.UserCompetenceRel;
import crac.utility.CompetenceAugmenter;
import crac.utility.ElasticConnector;
import crac.utility.JSonResponseHelper;
import crac.utility.TaskSearchHelper;
import crac.utilityModels.TravelledCompetence;

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
	private UserCompetenceRelDAO userCompetenceRelDAO;
	
	@Autowired
	private CompetenceRelationshipTypeDAO competenceRelationshipTypeDAO;
	
	@Autowired
	private CompetenceRelationshipDAO competenceRelationshipDAO;

	@Autowired
	private CompetencePermissionTypeDAO competencePermissionTypeDAO;
	
	@Autowired
	private CompetenceAugmenter competenceAugmenter;
	
	@Autowired
	private TaskSearchHelper taskSearchHelper;
	
	private ElasticConnector<Task> ESConnTask = new ElasticConnector<Task>("localhost", 9300, "crac_core", "task");

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

	@RequestMapping("/testMe/{competence_id}/{distance}/go")
	@ResponseBody
	public ResponseEntity<String> indexWord(@PathVariable(value = "distance") Double distance, @PathVariable(value = "competence_id") Long competenceId) {
		
		HashMap<Long, TravelledCompetence> relatedCompetences = competenceAugmenter.augment(competenceDAO.findOne(competenceId), distance);
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(relatedCompetences));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
		
	}
	
	@RequestMapping("/testThat")
	@ResponseBody
	public ResponseEntity<String> testThat() {
		
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		CracUser user = userDAO.findByName(userDetails.getUsername());

		
		ObjectMapper mapper = new ObjectMapper();
		try {
			return ResponseEntity.ok().body(mapper.writeValueAsString(taskSearchHelper.findMatch(userDAO.findOne((long)3))));
		} catch (JsonProcessingException e) {
			System.out.println(e.toString());
			return JSonResponseHelper.jsonWriteError();
		}
		
	}

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
		
		CompetencePermissionType cPermType = new CompetencePermissionType();
		cPermType.setDescription("can be added by oneself, free of restrictions");
		cPermType.setName("restriction free");
		cPermType.setSelf(true);
		cPermType.setNeededRole(Role.USER);
		
		competencePermissionTypeDAO.save(cPermType);
		
		Competence basicHumanSkills = new Competence();
		basicHumanSkills.setCreator(myUser);
		basicHumanSkills.setDescription("The majority of people is able to do these things.");
		basicHumanSkills.setName("basic human skills");
		basicHumanSkills.setPermissionType(cPermType);
		
		Competence breathing = new Competence();
		breathing.setCreator(myUser);
		breathing.setDescription("Beeing to stay alive by inhaling air.");
		breathing.setName("breathing");
		breathing.setPermissionType(cPermType);

		CompetenceRelationship basic_breathing = new CompetenceRelationship();
		basic_breathing.setCompetence1(basicHumanSkills);
		basic_breathing.setCompetence2(breathing);
		basic_breathing.setType(competenceRelationshipTypeDAO.findOne((long) 1));

		Competence walking = new Competence();
		walking.setCreator(myUser);
		walking.setDescription("Getting slowly from one point to another using human legs.");
		walking.setName("walking");
		walking.setPermissionType(cPermType);

		CompetenceRelationship basic_walking = new CompetenceRelationship();
		basic_walking.setCompetence1(basicHumanSkills);
		basic_walking.setCompetence2(walking);
		basic_walking.setType(competenceRelationshipTypeDAO.findOne((long) 2));
		
		Competence swimming = new Competence();
		swimming.setCreator(myUser);
		swimming.setDescription("Not drowning while in water.");
		swimming.setName("swimming");
		swimming.setPermissionType(cPermType);

		CompetenceRelationship basic_swimming = new CompetenceRelationship();
		basic_swimming.setCompetence1(basicHumanSkills);
		basic_swimming.setCompetence2(swimming);
		basic_swimming.setType(competenceRelationshipTypeDAO.findOne((long) 3));
		
		Competence programming = new Competence();
		programming.setCreator(myUser);
		programming.setDescription("Beeing able to write computer programs.");
		programming.setName("programming");
		programming.setPermissionType(cPermType);

		Competence javascriptProgramming = new Competence();
		javascriptProgramming.setCreator(myUser);
		javascriptProgramming.setDescription("Beeing able to write computer programs with/in JavaScript and it's libraries.");
		javascriptProgramming.setName("javascript-programming");
		javascriptProgramming.setPermissionType(cPermType);

		CompetenceRelationship programming_javascriptProgramming = new CompetenceRelationship();
		programming_javascriptProgramming.setCompetence1(programming);
		programming_javascriptProgramming.setCompetence2(javascriptProgramming);
		programming_javascriptProgramming.setType(competenceRelationshipTypeDAO.findOne((long) 2));
		
		Competence phpProgramming = new Competence();
		phpProgramming.setCreator(myUser);
		phpProgramming.setDescription("Beeing able to write computer programs with/in PHP and it's libraries.");
		phpProgramming.setName("php-programming");
		phpProgramming.setPermissionType(cPermType);

		CompetenceRelationship programming_phpProgramming = new CompetenceRelationship();
		programming_phpProgramming.setCompetence1(programming);
		programming_phpProgramming.setCompetence2(phpProgramming);
		programming_phpProgramming.setType(competenceRelationshipTypeDAO.findOne((long) 2));
		
		competenceDAO.save(basicHumanSkills);
		competenceDAO.save(breathing);
		competenceDAO.save(walking);
		competenceDAO.save(swimming);
		competenceDAO.save(programming);
		competenceDAO.save(javascriptProgramming);
		competenceDAO.save(phpProgramming);
		
		competenceRelationshipDAO.save(basic_breathing);
		competenceRelationshipDAO.save(basic_walking);
		competenceRelationshipDAO.save(basic_swimming);
		competenceRelationshipDAO.save(programming_javascriptProgramming);
		competenceRelationshipDAO.save(programming_phpProgramming);
		
		//Add projects
		Calendar time = new GregorianCalendar();
		
		Task waterFlowers = new Task();
		waterFlowers.setName("Water the flowers");
		waterFlowers.setDescription("All about watering the different flowers in the garden.");
		waterFlowers.setLocation("my garden");
		time.set(2016, 9, 10, 14, 30, 00);
		waterFlowers.setStartTime(time);
		time.set(2016, 9, 10, 17, 00, 00);
		waterFlowers.setEndTime(time);
		waterFlowers.setCreator(myUser);
		
		taskDAO.save(waterFlowers);
		
		//Add tasks
		
		Task waterRoses = new Task();
		waterRoses.setName("Water the roses");
		waterRoses.setDescription("Water the roses on the westside of the garden.");
		waterRoses.setLocation("my garden");
		time.set(2016, 9, 10, 16, 30, 00);
		waterRoses.setStartTime(time);
		time.set(2016, 9, 10, 17, 00, 00);
		waterRoses.setEndTime(time);
		waterRoses.setUrgency(5);
		waterRoses.setAmountOfVolunteers(2);
		waterRoses.setNeededCompetences(new HashSet<Competence>());
		waterRoses.getNeededCompetences().add(breathing);
		waterRoses.getNeededCompetences().add(walking);
		waterRoses.setCreator(myUser);
		waterRoses.setSuperTask(waterFlowers);
		
		Task waterLilies = new Task();
		waterLilies.setName("Water the lillies");
		waterLilies.setDescription("Water the lilies on the eastside of the garden.");
		waterLilies.setLocation("my garden");
		time.set(2016, 9, 10, 14, 30, 00);
		waterLilies.setStartTime(time);
		time.set(2016, 9, 10, 16, 00, 00);
		waterLilies.setEndTime(time);
		waterLilies.setUrgency(2);
		waterLilies.setAmountOfVolunteers(1);
		waterLilies.setNeededCompetences(new HashSet<Competence>());
		waterLilies.getNeededCompetences().add(breathing);
		waterLilies.getNeededCompetences().add(walking);
		waterLilies.setCreator(myUser);
		waterLilies.setSuperTask(waterFlowers);
		
		Task programWateringTool = new Task();
		programWateringTool.setName("Program a watering tool");
		programWateringTool.setDescription("Program a web-tool that makes watering flowers easier.");
		programWateringTool.setLocation("a desk in my garden");
		time.set(2016, 9, 10, 14, 30, 00);
		programWateringTool.setStartTime(time);
		time.set(2016, 9, 10, 17, 00, 00);
		programWateringTool.setEndTime(time);
		programWateringTool.setUrgency(10);
		programWateringTool.setAmountOfVolunteers(1);
		programWateringTool.setNeededCompetences(new HashSet<Competence>());
		programWateringTool.getNeededCompetences().add(breathing);
		programWateringTool.getNeededCompetences().add(walking);
		programWateringTool.getNeededCompetences().add(programming);
		programWateringTool.getNeededCompetences().add(phpProgramming);
		programWateringTool.getNeededCompetences().add(javascriptProgramming);
		programWateringTool.setCreator(myUser);
		programWateringTool.setSuperTask(waterFlowers);
		
		waterFlowers.setChildTasks(new HashSet<Task>());
		waterFlowers.getChildTasks().add(waterRoses);
		waterFlowers.getChildTasks().add(waterLilies);
		waterFlowers.getChildTasks().add(programWateringTool);
		
		taskDAO.save(waterFlowers);
		
		ESConnTask.indexOrUpdate(""+waterFlowers.getId(),waterFlowers);
		
		for(Task t : waterFlowers.getChildTasks()){
			ESConnTask.indexOrUpdate(""+t.getId(),t);
		}

		//Add users
		
		CracUser Webmaster = new CracUser();
		
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
		
		Webmaster.setName("Webmaster");
		Webmaster.setFirstName("Max");
		Webmaster.setLastName("Mustermann");
		Webmaster.setCompetenceRelationships(new HashSet<UserCompetenceRel>());
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, breathing, 0.9));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, walking, 0.9));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, swimming, 0.9));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, programming, 0.9));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, phpProgramming, 0.9));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, javascriptProgramming, 0.9));
		Webmaster.setPassword(bcryptEncoder.encode("noOneKnowsThisPassword!1!1"));
		Webmaster.setRole(Role.USER);
		Webmaster.setPhone("0987656789098");
		Webmaster.setEmail("Webmaster@internet.at");
		
		CracUser AverageHuman = new CracUser();
		
		AverageHuman.setName("AverageHuman");
		AverageHuman.setFirstName("Hans");
		AverageHuman.setLastName("Musterhans");
		AverageHuman.setCompetenceRelationships(new HashSet<UserCompetenceRel>());
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, breathing, 0.9));
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, walking, 0.9));
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, swimming, 0.9));
		AverageHuman.setPassword(bcryptEncoder.encode("noOneKnowsThisPasswordAnyway!1!1"));
		AverageHuman.setRole(Role.USER);
		AverageHuman.setPhone("35678987654");
		AverageHuman.setEmail("AverageHuman@internet.at");
		userDAO.save(Webmaster);
		userDAO.save(AverageHuman);
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

		return ResponseEntity.ok().headers(headers).body("{\"booted\":\"true\"}");
	}

}
