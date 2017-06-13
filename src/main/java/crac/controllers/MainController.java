package crac.controllers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import crac.components.matching.Decider;
import crac.components.matching.configuration.GlobalMatrixFilterConfig;
import crac.components.matching.configuration.UserFilterParameters;
import crac.components.matching.filter.ImportancyLevelFilter;
import crac.components.matching.filter.LikeLevelFilter;
import crac.components.matching.filter.ProficiencyLevelFilter;
import crac.components.matching.filter.UserRelationFilter;
import crac.components.matching.workers.TaskMatchingWorker;
import crac.components.storage.AugmenterUnit;
import crac.components.storage.CompetenceStorage;
import crac.components.utility.DataAccess;
import crac.components.utility.ElasticConnector;
import crac.components.utility.JSONResponseHelper;
import crac.enums.TaskState;
import crac.enums.TaskType;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CompetencePermissionTypeDAO;
import crac.models.db.daos.CompetenceRelationshipDAO;
import crac.models.db.daos.CompetenceRelationshipTypeDAO;
import crac.models.db.daos.CompetenceTaskRelDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.RepetitionDateDAO;
import crac.models.db.daos.RoleDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Role;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetencePermissionType;
import crac.models.db.relation.CompetenceRelationship;
import crac.models.db.relation.CompetenceRelationshipType;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.RepetitionDate;
import crac.models.db.relation.UserCompetenceRel;
import crac.models.db.relation.UserRelationship;
import crac.models.komet.daos.TxExabiscompetencesDescriptorsTopicidMmDAO;
import crac.models.komet.daos.TxExabiscompetencesTopicDAO;
import crac.models.komet.entities.TxExabiscompetencesDescriptorsTopicidMm;
import crac.models.komet.entities.TxExabiscompetencesTopic;
import crac.models.storage.CompetenceCollectionMatrix;
import crac.models.storage.SimpleCompetence;
import crac.models.storage.SimpleCompetenceRelation;
import crac.models.utility.TravelledCompetence;

/**
 * The main-controller used for hello world and testing
 */
@Controller
public class MainController {
	
	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private RoleDAO roleDAO;
	
	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private RepetitionDateDAO repetitionDateDAO;

	@Autowired
	private CompetenceTaskRelDAO competenceTaskRelDAO;
	
	@Autowired
	private TaskDAO taskDAO;
	
	@Autowired
	private UserCompetenceRelDAO userCompetenceRelDAO;
	
	@Autowired
	private UserRelationshipDAO userRelationshipDAO;
	
	@Autowired
	private CompetenceRelationshipTypeDAO competenceRelationshipTypeDAO;
	
	@Autowired
	private CompetenceRelationshipDAO competenceRelationshipDAO;

	@Autowired
	private CompetencePermissionTypeDAO competencePermissionTypeDAO;
	
	@Autowired
	private TxExabiscompetencesTopicDAO txExabiscompetencesTopicDAO;
	
	@Autowired
	private TxExabiscompetencesDescriptorsTopicidMmDAO txExabiscompetencesDescriptorsTopicidMmDAO;

	
	@Value("${crac.elastic.url}")
    private String url;
	
	@Value("${crac.elastic.port}")
    private int port;
	
	@Value("${crac.boot.enable}")
    private boolean bootEnabled;
		
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/include")
	@ResponseBody
	public ResponseEntity<String> test() {
		
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
		
		if (roleDAO.findByName("USER") == null) {
			Role userRole = new Role();
			userRole.setName("USER");
			roleDAO.save(userRole);
		}
		if (roleDAO.findByName("EDITOR") == null) {
			Role editorRole = new Role();
			editorRole.setName("EDITOR");
			roleDAO.save(editorRole);
		}
		
		CompetenceRelationshipType t1 = new CompetenceRelationshipType();
		t1.setDescription("Competences are synonym to each other");
		t1.setDistanceVal(1.0);
		t1.setName("synonym");

		competenceRelationshipTypeDAO.save(t1);

		CompetenceRelationshipType t2 = new CompetenceRelationshipType();
		t2.setDescription("Competences are similar to each other");
		t2.setDistanceVal(0.8);
		t2.setName("similar");

		competenceRelationshipTypeDAO.save(t2);

		CompetenceRelationshipType t3 = new CompetenceRelationshipType();
		t3.setDescription("Competences are closely related to each other");
		t3.setDistanceVal(0.6);
		t3.setName("closeRelated");

		competenceRelationshipTypeDAO.save(t3);

		CompetenceRelationshipType t4 = new CompetenceRelationshipType();
		t4.setDescription("Competences are related to each other");
		t4.setDistanceVal(0.4);
		t4.setName("related");

		competenceRelationshipTypeDAO.save(t4);

		CompetenceRelationshipType t5 = new CompetenceRelationshipType();
		t5.setDescription("Competences are far related to each other");
		t5.setDistanceVal(0.2);
		t5.setName("farRelated");

		competenceRelationshipTypeDAO.save(t5);

		Competence c1 = new Competence();
		c1.setId(1);
		c1.setName("Elektronik installieren");
		c1.setDescription("Der Freiwillige hat Erfahrung im Einrichten von Elektronik");
		competenceDAO.save(c1);
		
		Competence c2 = new Competence();
		c2.setId(2);
		c2.setName("Wand ausmalen");
		c2.setDescription("Der Freiwillige hat Erfahrung im Ausmalen von Wänden");
		competenceDAO.save(c2);

		Competence c3 = new Competence();
		c3.setId(3);
		c3.setName("Kuchen backen");
		c3.setDescription("Der Freiwillige hat Erfahrung im Backen von Kuchen");
		competenceDAO.save(c3);

		Competence c4 = new Competence();
		c4.setId(4);
		c4.setName("Kalte Speisen zubereiten");
		c4.setDescription("Der Freiwillige hat Erfahrung im Zubereiten von kalte Speisen");
		competenceDAO.save(c4);

		Competence c5 = new Competence();
		c5.setId(5);
		c5.setName("Warme Speisen zubereiten");
		c5.setDescription("Der Freiwillige hat Erfahrung im bereitstellen und einrichten von Elektronik");
		competenceDAO.save(c5);

		Competence c6 = new Competence();
		c6.setId(6);
		c6.setName("Schwere Dinge tragen");
		c6.setDescription("Der Freiwillige hat Erfahrung im Tragen von mittelschweren bis schweren Dingen");
		competenceDAO.save(c6);

		Competence c7 = new Competence();
		c7.setId(7);
		c7.setName("Kasse bedienen");
		c7.setDescription("Der Freiwillige hat Erfahrung im Bedienen von Kassen");
		competenceDAO.save(c7);

		Competence c8 = new Competence();
		c8.setId(8);
		c8.setName("Kasse abrechnen");
		c8.setDescription("Der Freiwillige hat Erfahrung im Abrechnen von Kassen");
		competenceDAO.save(c8);

		Competence c9 = new Competence();
		c9.setId(9);
		c9.setName("In Gruppe arbeiten");
		c9.setDescription("Der Freiwillige hat Erfahrung im Arbeiten in Gruppen");
		competenceDAO.save(c9);

		Competence c10 = new Competence();
		c10.setId(10);
		c10.setName("Allein arbeiten");
		c10.setDescription("Der Freiwillige hat Erfahrung darin alleine zu arbeiten");
		competenceDAO.save(c10);

		Competence c11 = new Competence();
		c11.setId(11);
		c11.setName("Texte erfassen");
		c11.setDescription("Der Freiwillige hat Erfahrung im Erfassen von Texten zu unterschiedlichen Anlässen");
		competenceDAO.save(c11);

		Competence c12 = new Competence();
		c12.setId(12);
		c12.setName("Grafiken erstellen");
		c12.setDescription("Der Freiwillige hat Erfahrung im Erstellen von Grafiken zu unterschiedlichen Anlässen");
		competenceDAO.save(c12);

		Competence c13 = new Competence();
		c13.setId(13);
		c13.setName("Illusionisten-Ausbildung");
		c13.setDescription("Der Freiwillige hat eine abgeschlossenen Illusionisten-Ausbildung");
		competenceDAO.save(c13);

		Competence c14 = new Competence();
		c14.setId(14);
		c14.setName("Klassische Tänze");
		c14.setDescription("Der Freiwillige kann verschiedene klassische Tänze tanzen");
		competenceDAO.save(c14);

		Competence c15 = new Competence();
		c15.setId(15);
		c15.setName("Bedienen von Windows-Programmen");
		c15.setDescription("Der Freiwillige hat Erfahrung in der Bedienung von Programmen unter dem Betriebssystem Windows");
		competenceDAO.save(c15);

		Competence c16 = new Competence();
		c16.setId(16);
		c16.setName("VLC-Player bedienen");
		c16.setDescription("Der Freiwillige hat Erfahrung in der Bedienung von dem Programm VLC-Player");
		competenceDAO.save(c16);

		Competence c17 = new Competence();
		c17.setId(17);
		c17.setName("Kopfrechnen");
		c17.setDescription("Der Freiwillige hat Erfahrung im Kopfrechnen");
		competenceDAO.save(c17);
		
		CompetenceRelationship cr1 = new CompetenceRelationship();
		cr1.setCompetence1(c15);
		cr1.setCompetence2(c16);
		cr1.setType(t3);
		cr1.setId(1);
		competenceRelationshipDAO.save(cr1);
		
		CompetenceRelationship cr2 = new CompetenceRelationship();
		cr2.setCompetence1(c15);
		cr2.setCompetence2(c12);
		cr2.setType(t3);
		cr2.setId(2);
		competenceRelationshipDAO.save(cr2);

		CompetenceRelationship cr3 = new CompetenceRelationship();
		cr3.setCompetence1(c15);
		cr3.setCompetence2(c11);
		cr3.setType(t3);
		cr3.setId(3);
		competenceRelationshipDAO.save(cr3);

		CompetenceRelationship cr4 = new CompetenceRelationship();
		cr4.setCompetence1(c15);
		cr4.setCompetence2(c8);
		cr4.setType(t4);
		cr4.setId(4);
		competenceRelationshipDAO.save(cr4);

		CompetenceRelationship cr5 = new CompetenceRelationship();
		cr5.setCompetence1(c7);
		cr5.setCompetence2(c8);
		cr5.setType(t3);
		cr5.setId(5);
		competenceRelationshipDAO.save(cr5);

		CompetenceRelationship cr6 = new CompetenceRelationship();
		cr6.setCompetence1(c7);
		cr6.setCompetence2(c17);
		cr6.setType(t2);
		cr6.setId(6);
		competenceRelationshipDAO.save(cr6);
		
		CompetenceRelationship cr7 = new CompetenceRelationship();
		cr7.setCompetence1(c8);
		cr7.setCompetence2(c17);
		cr7.setType(t5);
		cr7.setId(7);
		competenceRelationshipDAO.save(cr7);

		CompetenceRelationship cr8 = new CompetenceRelationship();
		cr8.setCompetence1(c6);
		cr8.setCompetence2(c2);
		cr8.setType(t5);
		cr8.setId(8);
		competenceRelationshipDAO.save(cr8);

		CompetenceRelationship cr9 = new CompetenceRelationship();
		cr9.setCompetence1(c6);
		cr9.setCompetence2(c1);
		cr9.setType(t5);
		cr9.setId(9);
		competenceRelationshipDAO.save(cr9);
		
		CompetenceRelationship cr10 = new CompetenceRelationship();
		cr10.setCompetence1(c3);
		cr10.setCompetence2(c4);
		cr10.setType(t5);
		cr10.setId(10);
		competenceRelationshipDAO.save(cr10);

		CompetenceRelationship cr11 = new CompetenceRelationship();
		cr11.setCompetence1(c3);
		cr11.setCompetence2(c5);
		cr11.setType(t2);
		cr11.setId(1);
		competenceRelationshipDAO.save(cr11);

		CompetenceRelationship cr12 = new CompetenceRelationship();
		cr12.setCompetence1(c4);
		cr12.setCompetence2(c5);
		cr12.setType(t3);
		cr12.setId(12);
		competenceRelationshipDAO.save(cr12);


		CracUser HansGruber = new CracUser();

		HansGruber.setName("HansGruber");
		HansGruber.setFirstName("Hans");
		HansGruber.setLastName("Gruber");
		HansGruber.setPassword(bcryptEncoder.encode("default"));
		HansGruber.addRole(roleDAO.findByName("USER"));
		HansGruber.setPhone("35678987654");
		HansGruber.setEmail("Mustermail@internet.at");
		userDAO.save(HansGruber);
		
		UserCompetenceRel ur1 = new UserCompetenceRel();
		ur1.setUser(HansGruber);
		ur1.setCompetence(c6);
		ur1.setLikeValue(60);
		ur1.setProficiencyValue(100);
		userCompetenceRelDAO.save(ur1);
		
		UserCompetenceRel ur2 = new UserCompetenceRel();
		ur2.setUser(HansGruber);
		ur2.setCompetence(c14);
		ur2.setLikeValue(90);
		ur2.setProficiencyValue(70);
		userCompetenceRelDAO.save(ur2);
		
		UserCompetenceRel ur3 = new UserCompetenceRel();
		ur3.setUser(HansGruber);
		ur3.setCompetence(c17);
		ur3.setLikeValue(30);
		ur3.setProficiencyValue(50);
		userCompetenceRelDAO.save(ur3);

		CracUser SimonGruber = new CracUser();

		SimonGruber.setName("SimonGruber");
		SimonGruber.setFirstName("Simon");
		SimonGruber.setLastName("Gruber");
		SimonGruber.setPassword(bcryptEncoder.encode("default"));
		SimonGruber.addRole(roleDAO.findByName("USER"));
		SimonGruber.setPhone("35678987654");
		SimonGruber.setEmail("Mustermail@internet.at");
		userDAO.save(SimonGruber);
		
		UserCompetenceRel ur4 = new UserCompetenceRel();
		ur4.setUser(SimonGruber);
		ur4.setCompetence(c1);
		ur4.setLikeValue(100);
		ur4.setProficiencyValue(100);
		userCompetenceRelDAO.save(ur4);
		
		UserCompetenceRel ur5 = new UserCompetenceRel();
		ur5.setUser(SimonGruber);
		ur5.setCompetence(c4);
		ur5.setLikeValue(90);
		ur5.setProficiencyValue(70);
		userCompetenceRelDAO.save(ur5);
		
		UserCompetenceRel ur6 = new UserCompetenceRel();
		ur6.setUser(SimonGruber);
		ur6.setCompetence(c6);
		ur6.setLikeValue(50);
		ur6.setProficiencyValue(50);
		userCompetenceRelDAO.save(ur6);

		UserCompetenceRel ur7 = new UserCompetenceRel();
		ur7.setUser(SimonGruber);
		ur7.setCompetence(c13);
		ur7.setLikeValue(-50);
		ur7.setProficiencyValue(50);
		userCompetenceRelDAO.save(ur7);

		CracUser MichaelMayer = new CracUser();

		MichaelMayer.setName("MichaelMayer");
		MichaelMayer.setFirstName("Michael");
		MichaelMayer.setLastName("Mayer");
		MichaelMayer.setPassword(bcryptEncoder.encode("default"));
		MichaelMayer.addRole(roleDAO.findByName("USER"));
		MichaelMayer.setPhone("35678987654");
		MichaelMayer.setEmail("Mustermail@internet.at");
		userDAO.save(MichaelMayer);
		
		UserCompetenceRel ur8 = new UserCompetenceRel();
		ur8.setUser(MichaelMayer);
		ur8.setCompetence(c7);
		ur8.setLikeValue(90);
		ur8.setProficiencyValue(100);
		userCompetenceRelDAO.save(ur8);
		
		UserCompetenceRel ur9 = new UserCompetenceRel();
		ur9.setUser(MichaelMayer);
		ur9.setCompetence(c8);
		ur9.setLikeValue(90);
		ur9.setProficiencyValue(100);
		userCompetenceRelDAO.save(ur9);

		UserCompetenceRel ur10 = new UserCompetenceRel();
		ur10.setUser(MichaelMayer);
		ur10.setCompetence(c17);
		ur10.setLikeValue(50);
		ur10.setProficiencyValue(100);
		userCompetenceRelDAO.save(ur10);
		
		UserCompetenceRel ur11 = new UserCompetenceRel();
		ur11.setUser(MichaelMayer);
		ur11.setCompetence(c3);
		ur11.setLikeValue(0);
		ur11.setProficiencyValue(30);
		userCompetenceRelDAO.save(ur11);

		CracUser ErikLehnsherr = new CracUser();

		ErikLehnsherr.setName("ErikLehnsherr");
		ErikLehnsherr.setFirstName("Erik");
		ErikLehnsherr.setLastName("Lehnsherr");
		ErikLehnsherr.setPassword(bcryptEncoder.encode("default"));
		ErikLehnsherr.addRole(roleDAO.findByName("USER"));
		ErikLehnsherr.setPhone("35678987654");
		ErikLehnsherr.setEmail("Mustermail@internet.at");
		userDAO.save(ErikLehnsherr);

		UserCompetenceRel ur12 = new UserCompetenceRel();
		ur12.setUser(ErikLehnsherr);
		ur12.setCompetence(c11);
		ur12.setLikeValue(100);
		ur12.setProficiencyValue(80);
		userCompetenceRelDAO.save(ur12);
		
		UserCompetenceRel ur13 = new UserCompetenceRel();
		ur13.setUser(ErikLehnsherr);
		ur13.setCompetence(c12);
		ur13.setLikeValue(100);
		ur13.setProficiencyValue(90);
		userCompetenceRelDAO.save(ur13);

		UserCompetenceRel ur14 = new UserCompetenceRel();
		ur14.setUser(ErikLehnsherr);
		ur14.setCompetence(c13);
		ur14.setLikeValue(-50);
		ur14.setProficiencyValue(60);
		userCompetenceRelDAO.save(ur14);
		
		UserCompetenceRel ur15 = new UserCompetenceRel();
		ur15.setUser(ErikLehnsherr);
		ur15.setCompetence(c15);
		ur15.setLikeValue(30);
		ur15.setProficiencyValue(30);
		userCompetenceRelDAO.save(ur15);
		
		UserCompetenceRel ur16 = new UserCompetenceRel();
		ur16.setUser(ErikLehnsherr);
		ur16.setCompetence(c16);
		ur16.setLikeValue(30);
		ur16.setProficiencyValue(30);
		userCompetenceRelDAO.save(ur16);
		
		CracUser ErnstBlofeld = new CracUser();

		ErnstBlofeld.setName("ErnstBlofeld");
		ErnstBlofeld.setFirstName("Ernst");
		ErnstBlofeld.setLastName("Blofeld");
		ErnstBlofeld.setPassword(bcryptEncoder.encode("default"));
		ErnstBlofeld.addRole(roleDAO.findByName("USER"));
		ErnstBlofeld.setPhone("35678987654");
		ErnstBlofeld.setEmail("Mustermail@internet.at");
		userDAO.save(ErnstBlofeld);
		
		UserCompetenceRel ur17 = new UserCompetenceRel();
		ur17.setUser(ErnstBlofeld);
		ur17.setCompetence(c2);
		ur17.setLikeValue(80);
		ur17.setProficiencyValue(20);
		userCompetenceRelDAO.save(ur17);

		UserCompetenceRel ur18 = new UserCompetenceRel();
		ur18.setUser(ErnstBlofeld);
		ur18.setCompetence(c3);
		ur18.setLikeValue(80);
		ur18.setProficiencyValue(100);
		userCompetenceRelDAO.save(ur18);
		
		UserCompetenceRel ur19 = new UserCompetenceRel();
		ur19.setUser(ErnstBlofeld);
		ur19.setCompetence(c4);
		ur19.setLikeValue(90);
		ur19.setProficiencyValue(100);
		userCompetenceRelDAO.save(ur19);
		
		UserCompetenceRel ur20 = new UserCompetenceRel();
		ur20.setUser(ErnstBlofeld);
		ur20.setCompetence(c5);
		ur20.setLikeValue(100);
		ur20.setProficiencyValue(100);
		userCompetenceRelDAO.save(ur20);
		
		UserCompetenceRel ur21 = new UserCompetenceRel();
		ur21.setUser(ErnstBlofeld);
		ur21.setCompetence(c6);
		ur21.setLikeValue(30);
		ur21.setProficiencyValue(30);
		userCompetenceRelDAO.save(ur21);
		
		//User-Relationships
		
		UserRelationship u1 = new UserRelationship();
		u1.setC1(ErnstBlofeld);
		u1.setC2(ErikLehnsherr);
		u1.setFriends(false);
		u1.setLikeValue(40);
		userRelationshipDAO.save(u1);
		
		UserRelationship u2 = new UserRelationship();
		u2.setC1(SimonGruber);
		u2.setC2(ErikLehnsherr);
		u2.setFriends(false);
		u2.setLikeValue(-30);
		userRelationshipDAO.save(u2);
		
		UserRelationship u3 = new UserRelationship();
		u3.setC1(SimonGruber);
		u3.setC2(MichaelMayer);
		u3.setFriends(false);
		u3.setLikeValue(50);
		userRelationshipDAO.save(u3);
		
		UserRelationship u4 = new UserRelationship();
		u4.setC1(HansGruber);
		u4.setC2(MichaelMayer);
		u4.setFriends(false);
		u4.setLikeValue(50);
		userRelationshipDAO.save(u4);
		
		UserRelationship u5 = new UserRelationship();
		u5.setC1(HansGruber);
		u5.setC2(ErikLehnsherr);
		u5.setFriends(false);
		u5.setLikeValue(-50);
		userRelationshipDAO.save(u5);
		
		UserRelationship u6 = new UserRelationship();
		u6.setC1(SimonGruber);
		u6.setC2(HansGruber);
		u6.setFriends(true);
		u6.setLikeValue(100);
		userRelationshipDAO.save(u6);

		Calendar c = Calendar.getInstance();

		//Ebene 1
		
		Task project = new Task();
		project.setAddress("Schulstraße1");
		project.setCreator(userDAO.findByName("frontend"));
		project.setDescription("Schulfest an einer beliebigen Schule, die Schüler und ihre Eltern in den Prozess miteinbezieht");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 9, 0, 0);
		project.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 12, 0, 0);
		project.setEndTime(c);
		project.setLocation("Auf der Hinterseite des Schulgebäudes");
		project.setLng(50);
		project.setLat(15);
		project.setMaxAmountOfVolunteers(0);
		project.setMinAmountOfVolunteers(100);
		project.setTaskType(TaskType.ORGANISATIONAL);
		project.setName("Schulfest");
		project.setReadyToPublish(true);
		taskDAO.save(project);

		//Ebene 1 der Aufgaben
		
		Task t11 = new Task();
		t11.setAddress("Schulstraße1");
		t11.setCreator(userDAO.findByName("frontend"));
		t11.setDescription("Vorbereitung des Schulfestes an einer beliebigen Schule, die Schüler und ihre Eltern in den Prozess miteinbezieht");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 9, 0, 0);
		t11.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t11.setEndTime(c);
		t11.setLocation("Auf der Hinterseite des Schulgebäudes");
		t11.setLng(50);
		t11.setLat(15);
		t11.setMaxAmountOfVolunteers(0);
		t11.setMinAmountOfVolunteers(10);
		t11.setTaskType(TaskType.ORGANISATIONAL);
		t11.setName("Vorbereitung des Schulfestes");
		t11.setSuperTask(project);
		t11.setReadyToPublish(true);
		taskDAO.save(t11);

		Task t12 = new Task();
		t12.setAddress("Schulstraße1");
		t12.setCreator(userDAO.findByName("frontend"));
		t12.setDescription("Durchführung des Schulfestes an einer beliebigen Schule, die Schüler und ihre Eltern in den Prozess miteinbezieht");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t12.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 0, 0, 0);
		t12.setEndTime(c);
		t12.setLocation("Auf der Hinterseite des Schulgebäudes");
		t12.setLng(50);
		t12.setLat(15);
		t12.setMaxAmountOfVolunteers(0);
		t12.setMinAmountOfVolunteers(10);
		t12.setTaskType(TaskType.ORGANISATIONAL);
		t12.setName("Durchführung des Schulfestes");
		t12.setSuperTask(project);
		t12.setReadyToPublish(true);
		taskDAO.save(t12);

		Task t13 = new Task();
		t13.setAddress("Schulstraße1");
		t13.setCreator(userDAO.findByName("frontend"));
		t13.setDescription("Nachbearbeitung des Schulfestes eines Schulfestes an einer beliebigen Schule, die Schüler und ihre Eltern in den Prozess miteinbezieht");
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 9, 0, 0);
		t13.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 12, 0, 0);
		t13.setEndTime(c);
		t13.setLocation("Auf der Hinterseite des Schulgebäudes");
		t13.setLng(50);
		t13.setLat(15);
		t13.setMaxAmountOfVolunteers(0);
		t13.setMinAmountOfVolunteers(10);
		t13.setTaskType(TaskType.ORGANISATIONAL);
		t13.setName("Nachbearbeitung des Schulfestes");
		t13.setSuperTask(project);
		t13.setReadyToPublish(true);
		taskDAO.save(t13);

		//Ebene 3
		
		Task t21 = new Task();
		t21.setAddress("Schulstraße1");
		t21.setCreator(userDAO.findByName("frontend"));
		t21.setDescription("Vorbereitung des Essens in der Kafeteria");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 9, 0, 0);
		t21.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t21.setEndTime(c);
		t21.setLocation("In der Küche des Schulgebäudes");
		t21.setLng(50);
		t21.setLat(15);
		t21.setMaxAmountOfVolunteers(0);
		t21.setMinAmountOfVolunteers(10);
		t21.setTaskType(TaskType.ORGANISATIONAL);
		t21.setName("Vorbereitung des Essens");
		t21.setSuperTask(t11);
		t21.setReadyToPublish(true);
		taskDAO.save(t21);

		Task t22 = new Task();
		t22.setAddress("Schulstraße1");
		t22.setCreator(userDAO.findByName("frontend"));
		t22.setDescription("Vorbereitung der Umgebung");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 9, 0, 0);
		t22.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t22.setEndTime(c);
		t22.setLocation("Auf der Hinterseite des Schulgebäudes");
		t22.setLng(50);
		t22.setLat(15);
		t22.setMaxAmountOfVolunteers(0);
		t22.setMinAmountOfVolunteers(10);
		t22.setTaskType(TaskType.ORGANISATIONAL);
		t22.setName("Vorbereitung der Umgebung");
		t22.setSuperTask(t11);
		t22.setReadyToPublish(true);
		taskDAO.save(t22);

		Task t23 = new Task();
		t23.setAddress("Schulstraße1");
		t23.setCreator(userDAO.findByName("frontend"));
		t23.setDescription("Verkauf von Essen, Getränken und Tombola-Losen am Schulfest");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t23.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 0, 0, 0);
		t23.setEndTime(c);
		t23.setLocation("Auf der Hinterseite des Schulgebäudes");
		t23.setLng(50);
		t23.setLat(15);
		t23.setMaxAmountOfVolunteers(0);
		t23.setMinAmountOfVolunteers(10);
		t23.setTaskType(TaskType.WORKABLE);
		t23.setName("Verkauf am Schulfest");
		t23.setSuperTask(t12);
		t23.setReadyToPublish(true);
		taskDAO.save(t23);

		Task s231 = new Task();
		s231.setAddress("Schulstraße1");
		s231.setCreator(userDAO.findByName("frontend"));
		s231.setDescription("Verkauf von Essen, Getränken und Tombola-Losen am Schulfest, Schicht 1");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		s231.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 20, 0, 0);
		s231.setEndTime(c);
		s231.setLocation("Auf der Hinterseite des Schulgebäudes");
		s231.setLng(50);
		s231.setLat(15);
		s231.setMaxAmountOfVolunteers(0);
		s231.setMinAmountOfVolunteers(10);
		s231.setTaskType(TaskType.SHIFT);
		s231.setName("Verkauf am Schulfest, Schicht 1");
		s231.setSuperTask(t23);
		s231.setReadyToPublish(true);
		taskDAO.save(s231);
		
		CompetenceTaskRel r1 = new CompetenceTaskRel();
		r1.setTask(s231);
		r1.setCompetence(c17);
		r1.setImportanceLevel(70);
		r1.setNeededProficiencyLevel(50);
		r1.setMandatory(false);
		competenceTaskRelDAO.save(r1);
		
		CompetenceTaskRel r2 = new CompetenceTaskRel();
		r2.setTask(s231);
		r2.setCompetence(c7);
		r2.setImportanceLevel(70);
		r2.setNeededProficiencyLevel(40);
		r2.setMandatory(false);
		competenceTaskRelDAO.save(r2);

		Task s232 = new Task();
		s232.setAddress("Schulstraße1");
		s232.setCreator(userDAO.findByName("frontend"));
		s232.setDescription("Verkauf von Essen, Getränken und Tombola-Losen am Schulfest, Schicht 2");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 20, 0, 0);
		s232.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 0, 0, 0);
		s232.setEndTime(c);
		s232.setLocation("Auf der Hinterseite des Schulgebäudes");
		s232.setLng(50);
		s232.setLat(15);
		s232.setMaxAmountOfVolunteers(0);
		s232.setMinAmountOfVolunteers(10);
		s232.setTaskType(TaskType.SHIFT);
		s232.setName("Verkauf am Schulfest, Schicht 2");
		s232.setSuperTask(t23);
		s232.setReadyToPublish(true);
		taskDAO.save(s232);

		CompetenceTaskRel r3 = new CompetenceTaskRel();
		r3.setTask(s232);
		r3.setCompetence(c17);
		r3.setImportanceLevel(70);
		r3.setNeededProficiencyLevel(50);
		r3.setMandatory(false);
		competenceTaskRelDAO.save(r3);
		
		CompetenceTaskRel r4 = new CompetenceTaskRel();
		r4.setTask(s232);
		r4.setCompetence(c7);
		r4.setImportanceLevel(70);
		r4.setNeededProficiencyLevel(40);
		r4.setMandatory(false);
		competenceTaskRelDAO.save(r4);

		Task t24 = new Task();
		t24.setAddress("Schulstraße1");
		t24.setCreator(userDAO.findByName("frontend"));
		t24.setDescription("Aktualisierung der Playlist");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t24.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 0, 0, 0);
		t24.setEndTime(c);
		t24.setLocation("Auf der Hinterseite des Schulgebäudes");
		t24.setLng(50);
		t24.setLat(15);
		t24.setMaxAmountOfVolunteers(0);
		t24.setMinAmountOfVolunteers(10);
		t24.setTaskType(TaskType.WORKABLE);
		t24.setName("Musikalische Untermalung am Schulfest");
		t24.setSuperTask(t12);
		t24.setReadyToPublish(true);
		taskDAO.save(t24);
		
		CompetenceTaskRel r5 = new CompetenceTaskRel();
		r5.setTask(t24);
		r5.setCompetence(c15);
		r5.setImportanceLevel(90);
		r5.setNeededProficiencyLevel(50);
		r5.setMandatory(false);
		competenceTaskRelDAO.save(r5);
		
		CompetenceTaskRel r6 = new CompetenceTaskRel();
		r6.setTask(t24);
		r6.setCompetence(c16);
		r6.setImportanceLevel(100);
		r6.setNeededProficiencyLevel(40);
		r6.setMandatory(false);
		competenceTaskRelDAO.save(r6);

		Task t25 = new Task();
		t25.setAddress("Schulstraße1");
		t25.setCreator(userDAO.findByName("frontend"));
		t25.setDescription("Spezielle Ereignisse am Schulfest");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t25.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 0, 0, 0);
		t25.setEndTime(c);
		t25.setLocation("Auf der Hinterseite des Schulgebäudes");
		t25.setLng(50);
		t25.setLat(15);
		t25.setMaxAmountOfVolunteers(0);
		t25.setMinAmountOfVolunteers(10);
		t25.setTaskType(TaskType.ORGANISATIONAL);
		t25.setName("Events am Schulfestes");
		t25.setSuperTask(t12);
		t25.setReadyToPublish(true);
		taskDAO.save(t25);

		Task t26 = new Task();
		t26.setAddress("Schulstraße1");
		t26.setCreator(userDAO.findByName("frontend"));
		t26.setDescription("Aufräumen am Vormittag nach dem Schulfest");
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 9, 0, 0);
		t26.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 12, 0, 0);
		t26.setEndTime(c);
		t26.setLocation("Auf der Hinterseite des Schulgebäudes");
		t26.setLng(50);
		t26.setLat(15);
		t26.setMaxAmountOfVolunteers(0);
		t26.setMinAmountOfVolunteers(10);
		t26.setTaskType(TaskType.WORKABLE);
		t26.setName("Aufräumen des Schulfestes");
		t26.setSuperTask(t13);
		t26.setReadyToPublish(true);
		taskDAO.save(t26);

		Task t27 = new Task();
		t27.setAddress("Schulstraße1");
		t27.setCreator(userDAO.findByName("frontend"));
		t27.setDescription("Die Einnahmen der Kasse am Vormittag nach dem Schulfest rechnen");
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 9, 0, 0);
		t27.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 2, 12, 0, 0);
		t27.setEndTime(c);
		t27.setLocation("Im Sekretariat des Schulgebäudes");
		t27.setLng(50);
		t27.setLat(15);
		t27.setMaxAmountOfVolunteers(0);
		t27.setMinAmountOfVolunteers(10);
		t27.setTaskType(TaskType.WORKABLE);
		t27.setName("Kassenabrechnung des Schulfestes");
		t27.setSuperTask(t13);
		t27.setReadyToPublish(true);
		taskDAO.save(t27);
		
		CompetenceTaskRel r7 = new CompetenceTaskRel();
		r7.setTask(t27);
		r7.setCompetence(c8);
		r7.setImportanceLevel(100);
		r7.setNeededProficiencyLevel(60);
		r7.setMandatory(true);
		competenceTaskRelDAO.save(r7);
		
		CompetenceTaskRel r8 = new CompetenceTaskRel();
		r8.setTask(t27);
		r8.setCompetence(c17);
		r8.setImportanceLevel(80);
		r8.setNeededProficiencyLevel(60);
		r8.setMandatory(false);
		competenceTaskRelDAO.save(r8);

		//Ebene 4
		
		Task t31 = new Task();
		t31.setAddress("Schulstraße1");
		t31.setCreator(userDAO.findByName("frontend"));
		t31.setDescription("Vorbereitung des Kuchens in der Kafeteria");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 9, 0, 0);
		t31.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t31.setEndTime(c);
		t31.setLocation("In der Küche des Schulgebäudes");
		t31.setLng(50);
		t31.setLat(15);
		t31.setMaxAmountOfVolunteers(0);
		t31.setMinAmountOfVolunteers(10);
		t31.setTaskType(TaskType.WORKABLE);
		t31.setName("Vorbereitung des Kuchens");
		t31.setSuperTask(t21);
		t31.setReadyToPublish(true);
		taskDAO.save(t31);
		
		CompetenceTaskRel r9 = new CompetenceTaskRel();
		r9.setTask(t31);
		r9.setCompetence(c3);
		r9.setImportanceLevel(100);
		r9.setNeededProficiencyLevel(60);
		r9.setMandatory(false);
		competenceTaskRelDAO.save(r9);

		Task t32 = new Task();
		t32.setAddress("Schulstraße1");
		t32.setCreator(userDAO.findByName("frontend"));
		t32.setDescription("Vorbereitung der Güter für die Grillerei in der Kafeteria");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 9, 0, 0);
		t32.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t32.setEndTime(c);
		t32.setLocation("In der Küche des Schulgebäudes");
		t32.setLng(50);
		t32.setLat(15);
		t32.setMaxAmountOfVolunteers(0);
		t32.setMinAmountOfVolunteers(10);
		t32.setTaskType(TaskType.WORKABLE);
		t32.setName("Vorbereitung der Grillerei");
		t32.setSuperTask(t21);
		t32.setReadyToPublish(true);
		taskDAO.save(t32);

		Task t33 = new Task();
		t33.setAddress("Schulstraße1");
		t33.setCreator(userDAO.findByName("frontend"));
		t33.setDescription("Vorbereitung des Nudelsalates in der Kafeteria");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 9, 0, 0);
		t33.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t33.setEndTime(c);
		t33.setLocation("In der Küche des Schulgebäudes");
		t33.setLng(50);
		t33.setLat(15);
		t33.setMaxAmountOfVolunteers(0);
		t33.setMinAmountOfVolunteers(10);
		t33.setTaskType(TaskType.WORKABLE);
		t33.setName("Vorbereitung des Nudelsalates");
		t33.setSuperTask(t21);
		t33.setReadyToPublish(true);
		taskDAO.save(t33);
		
		CompetenceTaskRel r10 = new CompetenceTaskRel();
		r10.setTask(t33);
		r10.setCompetence(c4);
		r10.setImportanceLevel(100);
		r10.setNeededProficiencyLevel(60);
		r10.setMandatory(false);
		competenceTaskRelDAO.save(r10);

		Task t34 = new Task();
		t34.setAddress("Schulstraße1");
		t34.setCreator(userDAO.findByName("frontend"));
		t34.setDescription("Vorbereitung des Grillplatzes am Schulfest");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 9, 0, 0);
		t34.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t34.setEndTime(c);
		t34.setLocation("Auf der Hinterseite des Schulgebäudes");
		t34.setLng(50);
		t34.setLat(15);
		t34.setMaxAmountOfVolunteers(0);
		t34.setMinAmountOfVolunteers(10);
		t34.setTaskType(TaskType.WORKABLE);
		t34.setName("Vorbereitung des Grillplatzes");
		t34.setSuperTask(t22);
		t34.setReadyToPublish(true);
		taskDAO.save(t34);
		
		CompetenceTaskRel r11 = new CompetenceTaskRel();
		r11.setTask(t34);
		r11.setCompetence(c6);
		r11.setImportanceLevel(100);
		r11.setNeededProficiencyLevel(60);
		r11.setMandatory(false);
		competenceTaskRelDAO.save(r11);

		CompetenceTaskRel r19 = new CompetenceTaskRel();
		r19.setTask(t34);
		r19.setCompetence(c2);
		r19.setImportanceLevel(100);
		r19.setNeededProficiencyLevel(30);
		r19.setMandatory(false);
		competenceTaskRelDAO.save(r19);
		
		Task t35 = new Task();
		t35.setAddress("Schulstraße1");
		t35.setCreator(userDAO.findByName("frontend"));
		t35.setDescription("Vorbereitung der Bühne am Schulfest");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 9, 0, 0);
		t35.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t35.setEndTime(c);
		t35.setLocation("Auf der Hinterseite des Schulgebäudes");
		t35.setLng(50);
		t35.setLat(15);
		t35.setMaxAmountOfVolunteers(0);
		t35.setMinAmountOfVolunteers(10);
		t35.setTaskType(TaskType.WORKABLE);
		t35.setName("Vorbereitung der Bühne");
		t35.setSuperTask(t22);
		t35.setReadyToPublish(true);
		taskDAO.save(t35);

		CompetenceTaskRel r12 = new CompetenceTaskRel();
		r12.setTask(t35);
		r12.setCompetence(c6);
		r12.setImportanceLevel(100);
		r12.setNeededProficiencyLevel(70);
		r12.setMandatory(false);
		competenceTaskRelDAO.save(r12);

		CompetenceTaskRel r13 = new CompetenceTaskRel();
		r13.setTask(t35);
		r13.setCompetence(c1);
		r13.setImportanceLevel(100);
		r13.setNeededProficiencyLevel(60);
		r13.setMandatory(false);
		competenceTaskRelDAO.save(r13);

		Task t36 = new Task();
		t36.setAddress("Schulstraße1");
		t36.setCreator(userDAO.findByName("frontend"));
		t36.setDescription("Vorbereitung der Elektronik und Aufbau der Boxen am Schulfest");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 9, 0, 0);
		t36.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 16, 0, 0);
		t36.setEndTime(c);
		t36.setLocation("Auf der Hinterseite des Schulgebäudes");
		t36.setLng(50);
		t36.setLat(15);
		t36.setMaxAmountOfVolunteers(0);
		t36.setMinAmountOfVolunteers(10);
		t36.setTaskType(TaskType.WORKABLE);
		t36.setName("Vorbereitung der Elektronik");
		t36.setSuperTask(t22);
		t36.setReadyToPublish(true);
		taskDAO.save(t36);

		CompetenceTaskRel r14 = new CompetenceTaskRel();
		r14.setTask(t36);
		r14.setCompetence(c6);
		r14.setImportanceLevel(60);
		r14.setNeededProficiencyLevel(20);
		r14.setMandatory(false);
		competenceTaskRelDAO.save(r14);

		CompetenceTaskRel r15 = new CompetenceTaskRel();
		r15.setTask(t36);
		r15.setCompetence(c1);
		r15.setImportanceLevel(100);
		r15.setNeededProficiencyLevel(90);
		r15.setMandatory(false);
		competenceTaskRelDAO.save(r15);

		Task t37 = new Task();
		t37.setAddress("Schulstraße1");
		t37.setCreator(userDAO.findByName("frontend"));
		t37.setDescription("Ein einmaliger Auftritt als Illusionist am Schulfest, nur für Könner!");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 19, 0, 0);
		t37.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 19, 20, 0);
		t37.setEndTime(c);
		t37.setLocation("Auf der Hinterseite des Schulgebäudes");
		t37.setLng(50);
		t37.setLat(15);
		t37.setMaxAmountOfVolunteers(0);
		t37.setMinAmountOfVolunteers(10);
		t37.setTaskType(TaskType.WORKABLE);
		t37.setName("Auftritt als Illusionist");
		t37.setSuperTask(t25);
		t37.setReadyToPublish(true);
		taskDAO.save(t37);

		CompetenceTaskRel r16 = new CompetenceTaskRel();
		r16.setTask(t37);
		r16.setCompetence(c13);
		r16.setImportanceLevel(100);
		r16.setNeededProficiencyLevel(100);
		r16.setMandatory(true);
		competenceTaskRelDAO.save(r16);

		Task t38 = new Task();
		t38.setAddress("Schulstraße1");
		t38.setCreator(userDAO.findByName("frontend"));
		t38.setDescription("Vorführung klassischer Tänze, talentierte Freiwillige willkommen!");
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 21, 0, 0);
		t38.setStartTime(c);
		c = Calendar.getInstance();
		c.set(2017, 6, 1, 21, 30, 0);
		t38.setEndTime(c);
		t38.setLocation("Auf der Hinterseite des Schulgebäudes");
		t38.setLng(50);
		t38.setLat(15);
		t38.setMaxAmountOfVolunteers(0);
		t38.setMinAmountOfVolunteers(10);
		t38.setTaskType(TaskType.WORKABLE);
		t38.setName("Vorführung klassischer Tänze");
		t38.setSuperTask(t25);
		t38.setReadyToPublish(true);
		taskDAO.save(t38);

		CompetenceTaskRel r17 = new CompetenceTaskRel();
		r17.setTask(t38);
		r17.setCompetence(c14);
		r17.setImportanceLevel(100);
		r17.setNeededProficiencyLevel(90);
		r17.setMandatory(true);
		competenceTaskRelDAO.save(r17);

		return JSONResponseHelper.createResponse("included", true);
	}
	
	@RequestMapping("/filters")
	@ResponseBody
	public ResponseEntity<String> filters() {
		
		CompetenceStorage.synchronize();

		
		System.out.println("-----------------------");
		System.out.println("Adding Filters!");
		GlobalMatrixFilterConfig.clearFilters();
		GlobalMatrixFilterConfig.addFilter(new ProficiencyLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new LikeLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new ImportancyLevelFilter());
		System.out.println("-----------------------");
		
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("filters", "ADDED");
		return JSONResponseHelper.createResponse(true, meta);
		
	}
	
	/*
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/boot")
	@ResponseBody
	public ResponseEntity<String> boot() {
				
		if(!bootEnabled){
			return JSonResponseHelper.bootOff();
		}
		
		if(userDAO.findByName("Webmaster") != null){
			return JSonResponseHelper.alreadyBooted();
		}
		
		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		CracUser myUser = userDAO.findByName(userDetails.getName());
		
		//Add roles
		
		Role userRole = new Role();
		userRole.setName("USER");
		roleDAO.save(userRole);
		
		Role editorRole = new Role();
		editorRole.setName("EDITOR");
		roleDAO.save(editorRole);
		
		//Add relationship types
		
		CompetenceRelationshipType t1 = new CompetenceRelationshipType(); 
		t1.setDescription("Competences are synonym to each other");
		t1.setDistanceVal(1.0);
		t1.setName("synonym");
		
		competenceRelationshipTypeDAO.save(t1);
		
		CompetenceRelationshipType t2 = new CompetenceRelationshipType(); 
		t2.setDescription("Competences are similar to each other");
		t2.setDistanceVal(0.8);
		t2.setName("similar");
		
		competenceRelationshipTypeDAO.save(t2);
		
		CompetenceRelationshipType t3 = new CompetenceRelationshipType(); 
		t3.setDescription("Competences are closely related to each other");
		t3.setDistanceVal(0.6);
		t3.setName("closeRelated");
		
		competenceRelationshipTypeDAO.save(t3);
		
		CompetenceRelationshipType t4 = new CompetenceRelationshipType(); 
		t4.setDescription("Competences are related to each other");
		t4.setDistanceVal(0.4);
		t4.setName("related");
		
		competenceRelationshipTypeDAO.save(t4);

		CompetenceRelationshipType t5 = new CompetenceRelationshipType(); 
		t5.setDescription("Competences are far related to each other");
		t5.setDistanceVal(0.2);
		t5.setName("farRelated");
		
		competenceRelationshipTypeDAO.save(t5);

		
		//Add competences
		
		CompetencePermissionType cPermType1 = new CompetencePermissionType();
		cPermType1.setDescription("Can be added by oneself, free of restrictions.");
		cPermType1.setName("SELF");
		cPermType1.setSelf(true);
		
		CompetencePermissionType cPermType2 = new CompetencePermissionType();
		cPermType2.setDescription("Can be added only by a user with the permission to add it.");
		cPermType2.setName("EXTERNAL");
		cPermType2.setSelf(false);
		
		CompetencePermissionType cPermType3 = new CompetencePermissionType();
		cPermType3.setDescription("Can be acqired by solving tasks containing related competences as a reward.");
		cPermType3.setName("ACQUIRED");
		cPermType3.setSelf(false);
		
		CompetencePermissionType cPermType4 = new CompetencePermissionType();
		cPermType4.setDescription("Are automatically acquired, when enough tasks with this self-acquirably competence are done.");
		cPermType4.setName("ACQUIRED");
		cPermType4.setSelf(true);
		
		competencePermissionTypeDAO.save(cPermType1);
		competencePermissionTypeDAO.save(cPermType2);
		competencePermissionTypeDAO.save(cPermType3);
		competencePermissionTypeDAO.save(cPermType4);
		/*
		Competence basicHumanSkills = new Competence();
		basicHumanSkills.setCreator(myUser);
		basicHumanSkills.setDescription("The majority of people is able to do these things.");
		basicHumanSkills.setName("basic human skills");
		basicHumanSkills.setPermissionType(cPermType1);
		
		Competence breathing = new Competence();
		breathing.setCreator(myUser);
		breathing.setDescription("Beeing to stay alive by inhaling air.");
		breathing.setName("breathing");
		breathing.setPermissionType(cPermType1);

		CompetenceRelationship basic_breathing = new CompetenceRelationship();
		basic_breathing.setCompetence1(basicHumanSkills);
		basic_breathing.setCompetence2(breathing);
		basic_breathing.setType(competenceRelationshipTypeDAO.findOne((long) 1));

		Competence walking = new Competence();
		walking.setCreator(myUser);
		walking.setDescription("Getting slowly from one point to another using human legs.");
		walking.setName("walking");
		walking.setPermissionType(cPermType1);

		CompetenceRelationship basic_walking = new CompetenceRelationship();
		basic_walking.setCompetence1(basicHumanSkills);
		basic_walking.setCompetence2(walking);
		basic_walking.setType(competenceRelationshipTypeDAO.findOne((long) 2));
		
		Competence swimming = new Competence();
		swimming.setCreator(myUser);
		swimming.setDescription("Not drowning while in water.");
		swimming.setName("swimming");
		swimming.setPermissionType(cPermType1);

		CompetenceRelationship basic_swimming = new CompetenceRelationship();
		basic_swimming.setCompetence1(basicHumanSkills);
		basic_swimming.setCompetence2(swimming);
		basic_swimming.setType(competenceRelationshipTypeDAO.findOne((long) 3));
		
		Competence programming = new Competence();
		programming.setCreator(myUser);
		programming.setDescription("Beeing able to write computer programs.");
		programming.setName("programming");
		programming.setPermissionType(cPermType1);

		Competence javascriptProgramming = new Competence();
		javascriptProgramming.setCreator(myUser);
		javascriptProgramming.setDescription("Beeing able to write computer programs with/in JavaScript and it's libraries.");
		javascriptProgramming.setName("javascript-programming");
		javascriptProgramming.setPermissionType(cPermType1);

		CompetenceRelationship programming_javascriptProgramming = new CompetenceRelationship();
		programming_javascriptProgramming.setCompetence1(programming);
		programming_javascriptProgramming.setCompetence2(javascriptProgramming);
		programming_javascriptProgramming.setType(competenceRelationshipTypeDAO.findOne((long) 2));
		
		Competence phpProgramming = new Competence();
		phpProgramming.setCreator(myUser);
		phpProgramming.setDescription("Beeing able to write computer programs with/in PHP and it's libraries.");
		phpProgramming.setName("php-programming");
		phpProgramming.setPermissionType(cPermType1);

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
		
		Task waterFlowers = new Task();
		waterFlowers.setName("Water the flowers");
		waterFlowers.setDescription("All about watering the different flowers in the garden.");
		waterFlowers.setLocation("my garden");
		waterFlowers.setMaxAmountOfVolunteers(0);
		waterFlowers.setMinAmountOfVolunteers(10);
		waterFlowers.setCreator(myUser);
		waterFlowers.setTaskState(TaskState.PUBLISHED);
				
		//Add tasks
		
		Task waterRoses = new Task();
		waterRoses.setName("Water the roses");
		waterRoses.setDescription("Water the roses on the westside of the garden.");
		waterRoses.setLocation("my garden");
		waterRoses.setUrgency(5);
		waterRoses.setMaxAmountOfVolunteers(0);		
		waterRoses.setMinAmountOfVolunteers(3);		
		waterRoses.setCreator(myUser);
		waterRoses.setSuperTask(waterFlowers);
		waterRoses.setTaskState(TaskState.PUBLISHED);

		Task waterLilies = new Task();
		waterLilies.setName("Water the lillies");
		waterLilies.setDescription("Water the lilies on the eastside of the garden.");
		waterLilies.setLocation("my garden");
		waterLilies.setUrgency(2);
		waterLilies.setMaxAmountOfVolunteers(0);
		waterLilies.setMinAmountOfVolunteers(2);
		waterLilies.setCreator(myUser);
		waterLilies.setSuperTask(waterFlowers);
		waterLilies.setTaskState(TaskState.PUBLISHED);

		Task programWateringTool = new Task();
		programWateringTool.setName("Program a watering tool");
		programWateringTool.setDescription("Program a web-tool that makes watering flowers easier.");
		programWateringTool.setLocation("a desk in my garden");
		programWateringTool.setUrgency(10);
		programWateringTool.setMaxAmountOfVolunteers(0);
		programWateringTool.setMinAmountOfVolunteers(5);
		programWateringTool.setTaskState(TaskState.PUBLISHED);

		programWateringTool.setCreator(myUser);
		programWateringTool.setSuperTask(waterFlowers);
		
		waterFlowers.setChildTasks(new HashSet<Task>());
		waterFlowers.getChildTasks().add(waterRoses);
		waterFlowers.getChildTasks().add(waterLilies);
		waterFlowers.getChildTasks().add(programWateringTool);
		
		taskDAO.save(waterFlowers);
		taskDAO.save(waterRoses);
		taskDAO.save(waterLilies);
		taskDAO.save(programWateringTool);
		
		competenceTaskRelDAO.save(new CompetenceTaskRel(breathing, waterRoses, 10, 10, true));
		competenceTaskRelDAO.save(new CompetenceTaskRel(walking, waterRoses, 10, 10, false));
		
		competenceTaskRelDAO.save(new CompetenceTaskRel(breathing, waterLilies, 10, 10, true));
		competenceTaskRelDAO.save(new CompetenceTaskRel(walking, waterLilies, 10, 10, false));
		
		competenceTaskRelDAO.save(new CompetenceTaskRel(breathing, programWateringTool, 10, 10, true));
		competenceTaskRelDAO.save(new CompetenceTaskRel(walking, programWateringTool, 100, 100, false));
		competenceTaskRelDAO.save(new CompetenceTaskRel(programming, programWateringTool, 10, 10, false));
		competenceTaskRelDAO.save(new CompetenceTaskRel(phpProgramming, programWateringTool, 10, 10, false));
		competenceTaskRelDAO.save(new CompetenceTaskRel(javascriptProgramming, programWateringTool, 10, 10, false));

		
		ElasticConnector<Task> eSConnTask = new ElasticConnector<Task>(url, port, "crac_core", "task");

		eSConnTask.indexOrUpdate(""+waterFlowers.getId(),waterFlowers);
		
		for(Task t : waterFlowers.getChildTasks()){
			eSConnTask.indexOrUpdate(""+t.getId(),t);
		}

		//Add users
		
		CracUser frontend = userDAO.findOne((long)1);
		
		frontend.getCompetenceRelationships().add(new UserCompetenceRel(frontend, breathing, 50, 1));
		frontend.getCompetenceRelationships().add(new UserCompetenceRel(frontend, walking, 50, 1));
		frontend.getCompetenceRelationships().add(new UserCompetenceRel(frontend, swimming, 50, 1));
		userDAO.save(frontend);

		
		CracUser Webmaster = new CracUser();
		
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
		
		Webmaster.setName("Webmaster");
		Webmaster.setFirstName("Max");
		Webmaster.setLastName("Mustermann");
		Webmaster.setCompetenceRelationships(new HashSet<UserCompetenceRel>());
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, breathing, 50, 1));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, walking, 50, 1));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, swimming, 50, 1));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, programming, 50, 1));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, phpProgramming, 50, 1));
		Webmaster.getCompetenceRelationships().add(new UserCompetenceRel(Webmaster, javascriptProgramming, 50, 1));
		Webmaster.setPassword(bcryptEncoder.encode("noOneKnowsThisPassword!1!1"));
		//Webmaster.setRole(Role.USER);
		Webmaster.setPhone("0987656789098");
		Webmaster.setEmail("Webmaster@internet.at");
		userDAO.save(Webmaster);
	
		CracUser AverageHuman = new CracUser();
		
		AverageHuman.setName("AverageHuman");
		AverageHuman.setFirstName("Hans");
		AverageHuman.setLastName("Musterhans");
		AverageHuman.setCompetenceRelationships(new HashSet<UserCompetenceRel>());
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, breathing, 50, 1));
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, walking, 50, 1));
		AverageHuman.getCompetenceRelationships().add(new UserCompetenceRel(AverageHuman, swimming, 50, 1));
		AverageHuman.setPassword(bcryptEncoder.encode("noOneKnowsThisPasswordAnyway!1!1"));
		//AverageHuman.setRole(Role.USER);
		AverageHuman.setPhone("35678987654");
		AverageHuman.setEmail("AverageHuman@internet.at");
		userDAO.save(AverageHuman);
		
		addActualUsers();
		
		RepetitionDate date1 = new RepetitionDate(0, 0, 0, 0, 10);
		repetitionDateDAO.save(date1);
		
		CompetenceStorage.synchronize(competenceDAO, competenceRelationshipDAO);
		
		System.out.println("-----------------------");
		System.out.println("Adding Filters!");
		GlobalMatrixFilterConfig.addFilter(new ProficiencyLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new LikeLevelFilter());
		GlobalMatrixFilterConfig.addFilter(new UserRelationFilter());
		GlobalMatrixFilterConfig.addFilter(new ImportancyLevelFilter());
		System.out.println("-----------------------");

		return JSonResponseHelper.bootSuccess();
	}*/
	
	public void addActualUsers(){
		CracUser SchönböckADMIN = new CracUser();
		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

		SchönböckADMIN.setName("SchoenboeckADMIN");
		SchönböckADMIN.setFirstName("Johannes");
		SchönböckADMIN.setLastName("Schönböck");
		SchönböckADMIN.setPassword(bcryptEncoder.encode("default"));
		SchönböckADMIN.addRole(roleDAO.findByName("ADMIN"));
		SchönböckADMIN.setPhone("35678987654");
		SchönböckADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(SchönböckADMIN);
		
		CracUser SchönböckUSER = new CracUser();

		SchönböckUSER.setName("SchoenboeckUSER");
		SchönböckUSER.setFirstName("Johannes");
		SchönböckUSER.setLastName("Schönböck");
		SchönböckUSER.setPassword(bcryptEncoder.encode("default"));
		SchönböckUSER.addRole(roleDAO.findByName("USER"));
		SchönböckUSER.setPhone("35678987654");
		SchönböckUSER.setEmail("Mustermail@internet.at");
		userDAO.save(SchönböckUSER);

		CracUser PröllADMIN = new CracUser();

		PröllADMIN.setName("ProellADMIN");
		PröllADMIN.setFirstName("Birigt");
		PröllADMIN.setLastName("Pröll");
		PröllADMIN.setPassword(bcryptEncoder.encode("default"));
		PröllADMIN.addRole(roleDAO.findByName("ADMIN"));
		PröllADMIN.setPhone("35678987654");
		PröllADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(PröllADMIN);
		
		CracUser PröllUSER = new CracUser();

		PröllUSER.setName("ProellUSER");
		PröllUSER.setFirstName("Birigt");
		PröllUSER.setLastName("Pröll");
		PröllUSER.setPassword(bcryptEncoder.encode("default"));
		PröllUSER.addRole(roleDAO.findByName("USER"));
		PröllUSER.setPhone("35678987654");
		PröllUSER.setEmail("Mustermail@internet.at");
		userDAO.save(PröllUSER);
		
		CracUser EibnerADMIN = new CracUser();

		EibnerADMIN.setName("EibnerADMIN");
		EibnerADMIN.setFirstName("Wolfgang");
		EibnerADMIN.setLastName("Eibner");
		EibnerADMIN.setPassword(bcryptEncoder.encode("default"));
		EibnerADMIN.addRole(roleDAO.findByName("ADMIN"));
		EibnerADMIN.setPhone("35678987654");
		EibnerADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(EibnerADMIN);
		
		CracUser EibnerUSER = new CracUser();

		EibnerUSER.setName("EibnerUSER");
		EibnerUSER.setFirstName("Wolfgang");
		EibnerUSER.setLastName("Eibner");
		EibnerUSER.setPassword(bcryptEncoder.encode("default"));
		EibnerUSER.addRole(roleDAO.findByName("USER"));
		EibnerUSER.setPhone("35678987654");
		EibnerUSER.setEmail("Mustermail@internet.at");
		userDAO.save(EibnerUSER);

		CracUser KapsammerADMIN = new CracUser();

		KapsammerADMIN.setName("KapsammerADMIN");
		KapsammerADMIN.setFirstName("Elisabeth");
		KapsammerADMIN.setLastName("Kapsammer");
		KapsammerADMIN.setPassword(bcryptEncoder.encode("default"));
		KapsammerADMIN.addRole(roleDAO.findByName("ADMIN"));
		KapsammerADMIN.setPhone("35678987654");
		KapsammerADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(KapsammerADMIN);
		
		CracUser KapsammerUSER = new CracUser();

		KapsammerUSER.setName("KapsammerUSER");
		KapsammerUSER.setFirstName("Elisabeth");
		KapsammerUSER.setLastName("Kapsammer");
		KapsammerUSER.setPassword(bcryptEncoder.encode("default"));
		KapsammerUSER.addRole(roleDAO.findByName("USER"));
		KapsammerUSER.setPhone("35678987654");
		KapsammerUSER.setEmail("Mustermail@internet.at");
		userDAO.save(KapsammerUSER);

		CracUser SchwingerADMIN = new CracUser();

		SchwingerADMIN.setName("SchwingerADMIN");
		SchwingerADMIN.setFirstName("Wieland");
		SchwingerADMIN.setLastName("Schwinger");
		SchwingerADMIN.setPassword(bcryptEncoder.encode("default"));
		SchwingerADMIN.addRole(roleDAO.findByName("ADMIN"));
		SchwingerADMIN.setPhone("35678987654");
		SchwingerADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(SchwingerADMIN);
		
		CracUser SchwingerUSER = new CracUser();

		SchwingerUSER.setName("SchwingerUSER");
		SchwingerUSER.setFirstName("Wieland");
		SchwingerUSER.setLastName("Schwinger");
		SchwingerUSER.setPassword(bcryptEncoder.encode("default"));
		SchwingerUSER.addRole(roleDAO.findByName("USER"));
		SchwingerUSER.setPhone("35678987654");
		SchwingerUSER.setEmail("Mustermail@internet.at");
		userDAO.save(SchwingerUSER);

		
		//Users for Birgit
		
		CracUser bpAdmin = new CracUser();

		bpAdmin.setName("bp-admin");
		bpAdmin.setFirstName("Test");
		bpAdmin.setLastName("Admin");
		bpAdmin.setPassword(bcryptEncoder.encode("default"));
		bpAdmin.addRole(roleDAO.findByName("ADMIN"));
		bpAdmin.setPhone("35678987654");
		bpAdmin.setEmail("Mustermail@internet.at");
		userDAO.save(bpAdmin);

		CracUser bpVol1 = new CracUser();

		bpVol1.setName("bp-vol1");
		bpVol1.setFirstName("Test");
		bpVol1.setLastName("User1");
		bpVol1.setPassword(bcryptEncoder.encode("default"));
		bpVol1.addRole(roleDAO.findByName("USER"));
		bpVol1.setPhone("35678987654");
		bpVol1.setEmail("Mustermail@internet.at");
		userDAO.save(bpVol1);

		CracUser bpVol2 = new CracUser();

		bpVol2.setName("bp-vol2");
		bpVol2.setFirstName("Test");
		bpVol2.setLastName("User2");
		bpVol2.setPassword(bcryptEncoder.encode("default"));
		bpVol2.addRole(roleDAO.findByName("USER"));
		bpVol2.setPhone("35678987654");
		bpVol2.setEmail("Mustermail@internet.at");
		userDAO.save(bpVol2);

		CracUser bpVol3 = new CracUser();

		bpVol3.setName("bp-vol3");
		bpVol3.setFirstName("Test");
		bpVol3.setLastName("User3");
		bpVol3.setPassword(bcryptEncoder.encode("default"));
		bpVol3.addRole(roleDAO.findByName("USER"));
		bpVol3.setPhone("35678987654");
		bpVol3.setEmail("Mustermail@internet.at");
		userDAO.save(bpVol3);
		
		//Users for Claudia
		
		CracUser cvadmin1 = new CracUser();

		cvadmin1.setName("cvadmin1");
		cvadmin1.setFirstName("Test");
		cvadmin1.setLastName("Admin");
		cvadmin1.setPassword(bcryptEncoder.encode("default"));
		cvadmin1.addRole(roleDAO.findByName("ADMIN"));
		cvadmin1.setPhone("35678987654");
		cvadmin1.setEmail("Mustermail@internet.at");
		userDAO.save(cvadmin1);

		CracUser cvvol1 = new CracUser();

		cvvol1.setName("cvvol1");
		cvvol1.setFirstName("Test");
		cvvol1.setLastName("User1");
		cvvol1.setPassword(bcryptEncoder.encode("default"));
		cvvol1.addRole(roleDAO.findByName("USER"));
		cvvol1.setPhone("35678987654");
		cvvol1.setEmail("Mustermail@internet.at");
		userDAO.save(cvvol1);

		CracUser cvvol2 = new CracUser();

		cvvol2.setName("cvvol2");
		cvvol2.setFirstName("Test");
		cvvol2.setLastName("User2");
		cvvol2.setPassword(bcryptEncoder.encode("default"));
		cvvol2.addRole(roleDAO.findByName("USER"));
		cvvol2.setPhone("35678987654");
		cvvol2.setEmail("Mustermail@internet.at");
		userDAO.save(cvvol2);

		CracUser cvvol3 = new CracUser();

		cvvol3.setName("cvvol3");
		cvvol3.setFirstName("Test");
		cvvol3.setLastName("User3");
		cvvol3.setPassword(bcryptEncoder.encode("default"));
		cvvol3.addRole(roleDAO.findByName("USER"));
		cvvol3.setPhone("35678987654");
		cvvol3.setEmail("Mustermail@internet.at");
		userDAO.save(cvvol3);

		
	}

}
