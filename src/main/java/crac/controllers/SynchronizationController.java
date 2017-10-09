package crac.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import crac.enums.ErrorCode;
import crac.models.db.daos.AttachmentDAO;
import crac.models.db.daos.CommentDAO;
import crac.models.db.daos.CompetenceAreaDAO;
import crac.models.db.daos.CompetenceDAO;
import crac.models.db.daos.CompetencePermissionTypeDAO;
import crac.models.db.daos.CompetenceRelationshipDAO;
import crac.models.db.daos.CompetenceRelationshipTypeDAO;
import crac.models.db.daos.CompetenceTaskRelDAO;
import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.EvaluationDAO;
import crac.models.db.daos.GroupDAO;
import crac.models.db.daos.MaterialDAO;
import crac.models.db.daos.RepetitionDateDAO;
import crac.models.db.daos.RoleDAO;
import crac.models.db.daos.TaskDAO;
import crac.models.db.daos.TaskRelationshipTypeDAO;
import crac.models.db.daos.UserCompetenceRelDAO;
import crac.models.db.daos.UserMaterialSubscriptionDAO;
import crac.models.db.daos.UserRelationshipDAO;
import crac.models.db.daos.UserTaskRelDAO;
import crac.models.db.entities.Competence;
import crac.models.db.entities.CompetenceArea;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Role;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetencePermissionType;
import crac.models.db.relation.CompetenceRelationship;
import crac.models.db.relation.CompetenceRelationshipType;
import crac.models.komet.daos.TxExabiscompetencesDescriptorDAO;
import crac.models.komet.daos.TxExabiscompetencesDescriptorsDescriptorMmDAO;
import crac.models.komet.daos.TxExabiscompetencesDescriptorsTopicidMmDAO;
import crac.models.komet.daos.TxExabiscompetencesTopicDAO;
import crac.models.komet.entities.TxExabiscompetencesDescriptor;
import crac.models.komet.entities.TxExabiscompetencesDescriptorsDescriptorMm;
import crac.models.komet.entities.TxExabiscompetencesDescriptorsTopicidMm;
import crac.models.komet.entities.TxExabiscompetencesTopic;
import crac.module.matching.configuration.MatchingConfiguration;
import crac.module.matching.configuration.PostMatchingConfiguration;
import crac.module.matching.configuration.PreMatchingConfiguration;
import crac.module.matching.filter.matching.ImportancyLevelFilter;
import crac.module.matching.filter.matching.LikeLevelFilter;
import crac.module.matching.filter.matching.ProficiencyLevelFilter;
import crac.module.matching.filter.matching.UserRelationFilter;
import crac.module.matching.filter.postmatching.ClearFilter;
import crac.module.matching.filter.postmatching.MissingVolunteerFilter;
import crac.module.matching.filter.prematching.GroupFilter;
import crac.module.matching.filter.prematching.LoggedUserFilter;
import crac.module.storage.CompetenceStorage;
import crac.module.utility.ElasticConnector;
import crac.module.utility.JSONResponseHelper;

@RestController
@RequestMapping("/synchronization")
public class SynchronizationController {

	@Autowired
	private AttachmentDAO attachmentDAO;

	@Autowired
	private CommentDAO commentDAO;

	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private CompetenceAreaDAO competenceAreaDAO;

	@Autowired
	private CompetencePermissionTypeDAO competencePermissionTypeDAO;

	@Autowired
	private CompetenceRelationshipDAO competenceRelationshipDAO;

	@Autowired
	private CompetenceRelationshipTypeDAO competenceRelationshipTypeDAO;

	@Autowired
	private CompetenceTaskRelDAO competenceTaskRelDAO;

	@Autowired
	private CracUserDAO userDAO;

	@Autowired
	private EvaluationDAO evaluationDAO;

	@Autowired
	private GroupDAO groupDAO;

	@Autowired
	private MaterialDAO materialDAO;

	@Autowired
	private RepetitionDateDAO repetitionDateDAO;

	@Autowired
	private RoleDAO roleDAO;

	@Autowired
	private TaskDAO taskDAO;

	@Autowired
	private TaskRelationshipTypeDAO taskRelationshipTypeDAO;

	@Autowired
	private UserCompetenceRelDAO userCompetenceRelDAO;

	@Autowired
	private UserMaterialSubscriptionDAO userMaterialSubscriptionDAO;

	@Autowired
	private UserRelationshipDAO userRelationshipDAO;

	@Autowired
	private UserTaskRelDAO userTaskRelDAO;

	@Autowired
	private TxExabiscompetencesDescriptorDAO txExabiscompetencesDescriptorDAO;

	@Autowired
	private TxExabiscompetencesDescriptorsDescriptorMmDAO txExabiscompetencesDescriptorsDescriptorMmDAO;

	@Autowired
	private TxExabiscompetencesDescriptorsTopicidMmDAO txExabiscompetencesDescriptorsTopicidMmDAO;

	@Autowired
	private TxExabiscompetencesTopicDAO txExabiscompetencesTopicDAO;

	@Autowired
	private PreMatchingConfiguration preMatchingConfiguration;

	@Autowired
	private MatchingConfiguration matchingConfig;

	@Autowired
	private PostMatchingConfiguration postMatchingConfiguration;

	@Autowired
	private CompetenceStorage cs;

	@Autowired
	private ElasticConnector<Task> ect;

	@Value("${crac.elastic.url}")
	private String url;

	@Value("${crac.elastic.bindEStoSearch}")
	private boolean bindES;

	@PostConstruct
	public void init() {
		this.internsync();
		this.filtersync();
		// this.competencesync();
	}

	/**
	 * Synchronizes the competences of the DB into the CompetenceStorage of the
	 * application and caches the relations
	 * 
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/intern")
	@ResponseBody
	public ResponseEntity<String> internsync() {
		cs.synchronize();
		System.out.println("-------------------------------");
		System.out.println("||||INTERN COMPETENCES SYNCED||||");
		System.out.println("-------------------------------");
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("sync", "INTERN_COMPETENCES");
		return JSONResponseHelper.createResponse(true, meta);
	}

	/**
	 * Copy test-data to the platform and fully synchronize all competences from
	 * KOMET
	 * 
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/full")
	@ResponseBody
	public ResponseEntity<String> fullsync() {
		this.datasync();
		this.dbsync();
		this.filtersync();
		this.internsync();
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("sync", "ALL");
		return JSONResponseHelper.createResponse(true, meta);
	}

	/**
	 * Fully synchronize all competences from KOMET
	 * 
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/competences")
	@ResponseBody
	public ResponseEntity<String> syncCompetences() {
		this.dbsync();
		this.filtersync();
		this.internsync();
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("sync", "ALL_COMPETENCES");
		return JSONResponseHelper.createResponse(true, meta);
	}

	/**
	 * Synchronize all competences from KOMET-DB to CrAc-DB
	 * 
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/database")
	@ResponseBody
	public ResponseEntity<String> dbsync() {
		HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> m = new HashMap<>();

		handleTopics();
		// handleTopicRelationships();
		handleCompetences(m);
		handleRelationships(m);

		System.out.println("-------------------------------");
		System.out.println("||||DATABASE SYNCED||||");
		System.out.println("-------------------------------");

		return JSONResponseHelper.createResponse(m, true);

	}
	/*
	 * private void handleTopicRelationships() {
	 * Iterable<TxExabiscompetencesDescriptorsTopicidMm> kometTopicRelList =
	 * txExabiscompetencesDescriptorsTopicidMmDAO .findAll();
	 * Iterable<CompetenceArea> cracAreaList = competenceAreaDAO.findAll();
	 * HashMap<Long, CompetenceArea> cracAreaMap = new HashMap<>();
	 * 
	 * ArrayList<CompetenceArea> newc = new ArrayList<>();
	 * ArrayList<CompetenceArea> updatec = new ArrayList<>();
	 * ArrayList<CompetenceArea> deletec = new ArrayList<>();
	 * 
	 * for (CompetenceArea c : cracAreaList) { cracAreaMap.put(c.getId(), c); }
	 * 
	 * for (TxExabiscompetencesTopic single : kometTopicList) { if
	 * (!single.getTitle().equals("")) { if (!cracAreaMap.containsKey((long)
	 * single.getUid())) { newc.add(single.MapToCompetenceArea()); } else {
	 * updatec.add(single.MapToCompetenceArea()); cracAreaMap.remove((long)
	 * single.getUid()); } } }
	 * 
	 * for (Map.Entry<Long, CompetenceArea> set : cracAreaMap.entrySet()) {
	 * deletec.add(set.getValue()); }
	 * 
	 * handleNewCompetenceAreas(newc); handleUpdatedCompetenceAreas(updatec); //
	 * handleDeletedCompetenceAreas(deletec);
	 * 
	 * }
	 */

	private void handleTopics() {
		Iterable<TxExabiscompetencesTopic> kometTopicList = txExabiscompetencesTopicDAO.findAll();
		Iterable<CompetenceArea> cracAreaList = competenceAreaDAO.findAll();
		HashMap<Long, CompetenceArea> cracAreaMap = new HashMap<>();

		ArrayList<CompetenceArea> newc = new ArrayList<>();
		ArrayList<CompetenceArea> updatec = new ArrayList<>();
		ArrayList<CompetenceArea> deletec = new ArrayList<>();

		for (CompetenceArea c : cracAreaList) {
			cracAreaMap.put(c.getId(), c);
		}

		for (TxExabiscompetencesTopic single : kometTopicList) {
			if (!single.getTitle().equals("")) {
				if (!cracAreaMap.containsKey((long) single.getUid())) {
					newc.add(single.MapToCompetenceArea());
				} else {
					updatec.add(single.MapToCompetenceArea());
					cracAreaMap.remove((long) single.getUid());
				}
			}
		}

		for (Map.Entry<Long, CompetenceArea> set : cracAreaMap.entrySet()) {
			deletec.add(set.getValue());
		}

		handleNewCompetenceAreas(newc);
		handleUpdatedCompetenceAreas(updatec);
		// handleDeletedCompetenceAreas(deletec);

	}

	private void handleNewCompetenceAreas(ArrayList<CompetenceArea> competences) {
		for (CompetenceArea c : competences) {
			competenceAreaDAO.save(c);
		}
	}

	private void handleUpdatedCompetenceAreas(ArrayList<CompetenceArea> competences) {
		for (CompetenceArea c : competences) {
			competenceAreaDAO.save(c);
		}
	}

	private void handleDeletedCompetenceAreas(ArrayList<CompetenceArea> competences) {
		for (CompetenceArea c : competences) {
			competenceAreaDAO.delete(c);
		}
	}

	private void handleRelationships(HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> m) {
		Iterable<TxExabiscompetencesDescriptorsDescriptorMm> kometRelationshipList = txExabiscompetencesDescriptorsDescriptorMmDAO
				.findAll();
		Iterable<CompetenceRelationship> cracRelationshipList = competenceRelationshipDAO.findAll();
		HashMap<Long, CompetenceRelationship> cracRelationshipMap = new HashMap<>();

		for (CompetenceRelationship c : cracRelationshipList) {
			cracRelationshipMap.put(c.getId(), c);
		}

		ArrayList<CompetenceRelationship> newc = new ArrayList<>();
		ArrayList<CompetenceRelationship> updatec = new ArrayList<>();
		ArrayList<CompetenceRelationship> deletec = new ArrayList<>();

		for (TxExabiscompetencesDescriptorsDescriptorMm single : kometRelationshipList) {

			if (competenceDAO.findOne((long) single.getUidForeign()) == null
					|| competenceDAO.findOne((long) single.getUidForeign()) == null) {

				deletec.add(single.mapToCompetence(competenceRelationshipTypeDAO, competenceDAO));
				cracRelationshipMap.remove((long) single.getUid());

			} else if (!cracRelationshipMap.containsKey((long) single.getUid())) {
				newc.add(single.mapToCompetence(competenceRelationshipTypeDAO, competenceDAO));
			} else {
				updatec.add(single.mapToCompetence(competenceRelationshipTypeDAO, competenceDAO));
				cracRelationshipMap.remove((long) single.getUid());
			}
		}

		for (Map.Entry<Long, CompetenceRelationship> set : cracRelationshipMap.entrySet()) {
			deletec.add(set.getValue());
		}

		handleNewCompetenceRelationships(newc, m);
		handleUpdatedCompetenceRelationships(updatec, m);
		// handleDeletedCompetenceRelationships(deletec);

	}

	private void handleNewCompetenceRelationships(ArrayList<CompetenceRelationship> competencerels,
			HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> m) {
		for (CompetenceRelationship c : competencerels) {

			if (c.getCompetence1() != null && c.getCompetence2() != null) {

				competenceRelationshipDAO.save(c);

				String comp1id = c.getCompetence1().getId() + "";
				String comp2id = c.getCompetence2().getId() + "";

				HashMap<String, String> comp1 = new HashMap<>();
				HashMap<String, String> comp2 = new HashMap<>();

				if (m.get("created").containsKey(comp1id)) {
					comp1 = m.get("created").get(comp1id).get("relations");
				} else if (m.get("updated").containsKey(comp1id)) {
					comp1 = m.get("updated").get(comp1id).get("relations");
				}

				if (m.get("created").containsKey(comp2id)) {
					comp2 = m.get("created").get(comp2id).get("relations");
				} else if (m.get("updated").containsKey(comp2id)) {
					comp2 = m.get("updated").get(comp2id).get("relations");
				}

				if (comp1 != null && comp2 != null) {
					comp1.put(comp2id + "", "CREATE");
					comp2.put(comp1id + "", "CREATE");
				}
			}
		}

	}

	private void handleUpdatedCompetenceRelationships(ArrayList<CompetenceRelationship> competencerels,
			HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> m) {
		for (CompetenceRelationship c : competencerels) {
			competenceRelationshipDAO.save(c);
			String comp1id = c.getCompetence1().getId() + "";
			String comp2id = c.getCompetence2().getId() + "";

			HashMap<String, String> comp1 = new HashMap<>();
			HashMap<String, String> comp2 = new HashMap<>();

			if (m.get("created").containsKey(comp1id)) {
				comp1 = m.get("created").get(comp1id).get("relations");
			} else if (m.get("updated").containsKey(comp1id)) {
				comp1 = m.get("updated").get(comp1id).get("relations");
			}

			if (m.get("created").containsKey(comp2id)) {
				comp2 = m.get("created").get(comp2id).get("relations");
			} else if (m.get("updated").containsKey(comp2id)) {
				comp2 = m.get("updated").get(comp2id).get("relations");
			}

			if (comp1 != null && comp2 != null) {
				comp1.put(comp2id + "", "UPDATE");
				comp2.put(comp1id + "", "UPDATE");
			}
		}
	}

	private void handleDeletedCompetenceRelationships(ArrayList<CompetenceRelationship> competencerels) {
		for (CompetenceRelationship c : competencerels) {
			competenceRelationshipDAO.delete(c);
		}
	}

	private void handleCompetences(HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> m) {

		Iterable<TxExabiscompetencesDescriptor> kometCompetenceList = txExabiscompetencesDescriptorDAO.findAll();
		Iterable<Competence> cracCompetenceList = competenceDAO.findAll();
		HashMap<Long, Competence> cracCompetenceMap = new HashMap<>();

		for (Competence c : cracCompetenceList) {
			cracCompetenceMap.put(c.getId(), c);
		}

		ArrayList<Competence> newc = new ArrayList<>();
		ArrayList<Competence> updatec = new ArrayList<>();
		ArrayList<Competence> deletec = new ArrayList<>();

		for (TxExabiscompetencesDescriptor single : kometCompetenceList) {
			if (!single.getTitleshort().equals("")) {
				if (!cracCompetenceMap.containsKey((long) single.getUid())) {
					newc.add(single.mapToCompetence(competenceAreaDAO));
				} else {
					updatec.add(single.mapToCompetence(competenceAreaDAO));
					cracCompetenceMap.remove((long) single.getUid());
				}
			}
		}

		for (Map.Entry<Long, Competence> set : cracCompetenceMap.entrySet()) {
			deletec.add(set.getValue());
		}

		HashMap<String, HashMap<String, HashMap<String, String>>> meta = new HashMap<String, HashMap<String, HashMap<String, String>>>();

		HashMap<String, HashMap<String, String>> details = new HashMap<String, HashMap<String, String>>();
		HashMap<String, String> numbercreate = new HashMap<String, String>();
		numbercreate.put("number", newc.size() + "");
		HashMap<String, String> numberupdate = new HashMap<String, String>();
		numberupdate.put("number", updatec.size() + "");
		HashMap<String, String> numberdelete = new HashMap<String, String>();
		numberdelete.put("number", deletec.size() + "");

		details.put("CREATE", numbercreate);
		details.put("UPDATE", numberupdate);
		details.put("DELETE", numberdelete);
		meta.put("details", details);
		m.put("meta", meta);

		m.put("created", handleNewCompetences(newc));
		m.put("updated", handleUpdatedCompetences(updatec));
		// m.put("deleted", handleDeletedCompetences(deletec));

	}

	private HashMap<String, HashMap<String, HashMap<String, String>>> handleNewCompetences(
			ArrayList<Competence> competences) {
		HashMap<String, HashMap<String, HashMap<String, String>>> m = new HashMap<>();
		for (Competence c : competences) {
			HashMap<String, HashMap<String, String>> compid = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> action = new HashMap<String, String>();
			action.put("name", "CREATE");
			compid.put("action", action);
			compid.put("relations", new HashMap<String, String>());

			for (TxExabiscompetencesDescriptorsTopicidMm conn : txExabiscompetencesDescriptorsTopicidMmDAO
					.findByUidLocal((int) c.getId())) {
				c.addCompetenceArea(competenceAreaDAO.findOne((long) conn.getUidForeign()));
			}

			competenceDAO.save(c);
			m.put(c.getId() + "", compid);
		}
		return m;
	}

	private HashMap<String, HashMap<String, HashMap<String, String>>> handleUpdatedCompetences(
			ArrayList<Competence> competences) {
		HashMap<String, HashMap<String, HashMap<String, String>>> m = new HashMap<>();
		for (Competence c : competences) {
			HashMap<String, HashMap<String, String>> compid = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> action = new HashMap<String, String>();
			action.put("name", "UPDATE");
			compid.put("action", action);
			compid.put("relations", new HashMap<String, String>());

			for (TxExabiscompetencesDescriptorsTopicidMm conn : txExabiscompetencesDescriptorsTopicidMmDAO
					.findByUidLocal((int) c.getId())) {
				c.addCompetenceArea(competenceAreaDAO.findOne((long) conn.getUidForeign()));
			}

			competenceDAO.save(c);
			m.put(c.getId() + "", compid);
		}
		return m;
	}

	private HashMap<String, HashMap<String, HashMap<String, String>>> handleDeletedCompetences(
			ArrayList<Competence> competences) {
		HashMap<String, HashMap<String, HashMap<String, String>>> m = new HashMap<>();
		for (Competence c : competences) {
			HashMap<String, HashMap<String, String>> compid = new HashMap<String, HashMap<String, String>>();
			HashMap<String, String> action = new HashMap<String, String>();
			action.put("name", "DELETE");
			compid.put("action", action);
			compid.put("relations", new HashMap<String, String>());
			competenceDAO.delete(c);
			m.put(c.getId() + "", compid);
		}
		return m;
	}

	/**
	 * Add filters to the configuration
	 * 
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/filter")
	@ResponseBody
	public ResponseEntity<String> filtersync() {
		System.out.println("-------------------------------");
		System.out.println("||||FILTERS SYNCED||||");
		System.out.println("-------------------------------");

		preMatchingConfiguration.addFilter(new LoggedUserFilter());
		preMatchingConfiguration.addFilter(new GroupFilter());

		matchingConfig.addFilter(new ProficiencyLevelFilter());
		matchingConfig.addFilter(new LikeLevelFilter());
		matchingConfig.addFilter(new ImportancyLevelFilter());
		matchingConfig.addFilter(new UserRelationFilter());

		postMatchingConfiguration.addFilter(new ClearFilter());
		postMatchingConfiguration.addFilter(new MissingVolunteerFilter());

		HashMap<String, Object> meta = new HashMap<>();
		meta.put("sync", "FILTER");
		return JSONResponseHelper.createResponse(true, meta);
	}

	/**
	 * Copy test-data to the platform
	 * 
	 * @return ResponseEntity
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping("/data")
	@ResponseBody
	public ResponseEntity<String> datasync() {
		System.out.println("-------------------------------");
		System.out.println("||||DATA SYNCED||||");
		System.out.println("-------------------------------");

		// refreshESTasks();
		addCompetenceRelationshipTypes();
		addRoles();
		// addTestComps();
		addCompetencePermissionType();
		addUsers();
		HashMap<String, Object> meta = new HashMap<>();
		meta.put("sync", "TEST_DATA");
		return JSONResponseHelper.createResponse(true, meta);
	}

	private void addTestComps() {

		UsernamePasswordAuthenticationToken userDetails = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		CracUser myUser = userDAO.findByName(userDetails.getName());

		Competence basicHumanSkills = new Competence();
		basicHumanSkills.setId(1);
		basicHumanSkills.setCreator(myUser);
		basicHumanSkills.setDescription("The majority of people is able to do these things.");
		basicHumanSkills.setName("basic human skills");

		Competence breathing = new Competence();
		breathing.setId(2);
		breathing.setCreator(myUser);
		breathing.setDescription("Beeing to stay alive by inhaling air.");
		breathing.setName("breathing");

		CompetenceRelationship basic_breathing = new CompetenceRelationship();
		basic_breathing.setId(1);
		basic_breathing.setCompetence1(basicHumanSkills);
		basic_breathing.setCompetence2(breathing);
		basic_breathing.setType(competenceRelationshipTypeDAO.findOne((long) 1));

		Competence walking = new Competence();
		walking.setId(3);
		walking.setCreator(myUser);
		walking.setDescription("Getting slowly from one point to another using human legs.");
		walking.setName("walking");

		CompetenceRelationship basic_walking = new CompetenceRelationship();
		basic_walking.setId(2);
		basic_walking.setCompetence1(basicHumanSkills);
		basic_walking.setCompetence2(walking);
		basic_walking.setType(competenceRelationshipTypeDAO.findOne((long) 2));

		Competence swimming = new Competence();
		swimming.setId(4);
		swimming.setCreator(myUser);
		swimming.setDescription("Not drowning while in water.");
		swimming.setName("swimming");

		CompetenceRelationship basic_swimming = new CompetenceRelationship();
		basic_swimming.setId(3);
		basic_swimming.setCompetence1(basicHumanSkills);
		basic_swimming.setCompetence2(swimming);
		basic_swimming.setType(competenceRelationshipTypeDAO.findOne((long) 3));

		Competence programming = new Competence();
		programming.setId(5);
		programming.setCreator(myUser);
		programming.setDescription("Beeing able to write computer programs.");
		programming.setName("programming");

		Competence javascriptProgramming = new Competence();
		javascriptProgramming.setId(6);
		javascriptProgramming.setCreator(myUser);
		javascriptProgramming
				.setDescription("Beeing able to write computer programs with/in JavaScript and it's libraries.");
		javascriptProgramming.setName("javascript-programming");

		CompetenceRelationship programming_javascriptProgramming = new CompetenceRelationship();
		programming_javascriptProgramming.setId(4);
		programming_javascriptProgramming.setCompetence1(programming);
		programming_javascriptProgramming.setCompetence2(javascriptProgramming);
		programming_javascriptProgramming.setType(competenceRelationshipTypeDAO.findOne((long) 2));

		Competence phpProgramming = new Competence();
		phpProgramming.setId(7);
		phpProgramming.setCreator(myUser);
		phpProgramming.setDescription("Beeing able to write computer programs with/in PHP and it's libraries.");
		phpProgramming.setName("php-programming");

		CompetenceRelationship programming_phpProgramming = new CompetenceRelationship();
		programming_phpProgramming.setId(5);
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

	}

	private void addRoles() {
		System.out.println("-------------------------------");
		System.out.println("--roles synced--");
		System.out.println("-------------------------------");

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
	}

	private void addCompetenceRelationshipTypes() {
		System.out.println("-------------------------------");
		System.out.println("--relationship-types synced--");
		System.out.println("-------------------------------");

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

	}

	private void addCompetencePermissionType() {
		System.out.println("-------------------------------");
		System.out.println("--permission-types synced--");
		System.out.println("-------------------------------");

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
		cPermType4.setDescription(
				"Are automatically acquired, when enough tasks with this self-acquirably competence are done.");
		cPermType4.setName("ACQUIRED");
		cPermType4.setSelf(true);

		competencePermissionTypeDAO.save(cPermType1);
		competencePermissionTypeDAO.save(cPermType2);
		competencePermissionTypeDAO.save(cPermType3);
		competencePermissionTypeDAO.save(cPermType4);
	}

	private void addUsers() {
		System.out.println("-------------------------------");
		System.out.println("--users synced--");
		System.out.println("-------------------------------");

		CracUser Webmaster = new CracUser();

		BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

		Webmaster.setName("Webmaster");
		Webmaster.setFirstName("Max");
		Webmaster.setLastName("Mustermann");
		Webmaster.setPassword(bcryptEncoder.encode("noOneKnowsThisPassword!1!1"));
		// Webmaster.setRole(Role.USER);
		Webmaster.setPhone("0987656789098");
		Webmaster.setEmail("Webmaster@internet.at");
		userDAO.save(Webmaster);

		CracUser AverageHuman = new CracUser();

		AverageHuman.setName("AverageHuman");
		AverageHuman.setFirstName("Hans");
		AverageHuman.setLastName("Musterhans");
		AverageHuman.setPassword(bcryptEncoder.encode("noOneKnowsThisPasswordAnyway!1!1"));
		// AverageHuman.setRole(Role.USER);
		AverageHuman.setPhone("35678987654");
		AverageHuman.setEmail("AverageHuman@internet.at");
		userDAO.save(AverageHuman);

		CracUser SchönböckADMIN = new CracUser();

		SchönböckADMIN.setName("SchönboeckADMIN");
		SchönböckADMIN.setFirstName("Johannes");
		SchönböckADMIN.setPassword(bcryptEncoder.encode("default"));
		SchönböckADMIN.addRole(roleDAO.findByName("ADMIN"));
		SchönböckADMIN.setPhone("35678987654");
		SchönböckADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(SchönböckADMIN);

		CracUser SchönböckUSER = new CracUser();

		SchönböckUSER.setName("SchönböckUSER");
		SchönböckUSER.setFirstName("Johannes");
		SchönböckUSER.setLastName("Schönboeck");
		SchönböckUSER.setPassword(bcryptEncoder.encode("default"));
		SchönböckUSER.addRole(roleDAO.findByName("USER"));
		SchönböckUSER.setPhone("35678987654");
		SchönböckUSER.setEmail("Mustermail@internet.at");
		userDAO.save(SchönböckUSER);

		CracUser PröllADMIN = new CracUser();

		PröllADMIN.setName("PröllADMIN");
		PröllADMIN.setFirstName("Birigt");
		PröllADMIN.setLastName("Proell");
		PröllADMIN.setPassword(bcryptEncoder.encode("default"));
		PröllADMIN.addRole(roleDAO.findByName("ADMIN"));
		PröllADMIN.setPhone("35678987654");
		PröllADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(PröllADMIN);

		CracUser PröllUSER = new CracUser();

		PröllUSER.setName("PröllUSER");
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

		// New testing accounts for Birgit

		CracUser SandraADMIN = new CracUser();

		SandraADMIN.setName("SandraADMIN");
		SandraADMIN.setFirstName("Sandra");
		SandraADMIN.setLastName("Wolkerstorfer");
		SandraADMIN.setPassword(bcryptEncoder.encode("default"));
		SandraADMIN.addRole(roleDAO.findByName("ADMIN"));
		SandraADMIN.setPhone("35678987654");
		SandraADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(SandraADMIN);

		CracUser SandraUSER = new CracUser();

		SandraUSER.setName("SandraUSER");
		SandraUSER.setFirstName("Sandra");
		SandraUSER.setLastName("Wolkerstorfer");
		SandraUSER.setPassword(bcryptEncoder.encode("default"));
		SandraUSER.addRole(roleDAO.findByName("USER"));
		SandraUSER.setPhone("35678987654");
		SandraUSER.setEmail("Mustermail@internet.at");
		userDAO.save(SandraUSER);

		CracUser DanielADMIN = new CracUser();

		DanielADMIN.setName("DanielADMIN");
		DanielADMIN.setFirstName("Daniel");
		DanielADMIN.setLastName("Braden");
		DanielADMIN.setPassword(bcryptEncoder.encode("default"));
		DanielADMIN.addRole(roleDAO.findByName("ADMIN"));
		DanielADMIN.setPhone("35678987654");
		DanielADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(DanielADMIN);

		CracUser DanielUSER = new CracUser();

		DanielUSER.setName("DanielUSER");
		DanielUSER.setFirstName("Daniel");
		DanielUSER.setLastName("Braden");
		DanielUSER.setPassword(bcryptEncoder.encode("default"));
		DanielUSER.addRole(roleDAO.findByName("USER"));
		DanielUSER.setPhone("35678987654");
		DanielUSER.setEmail("Mustermail@internet.at");
		userDAO.save(DanielUSER);

		CracUser SusanneADMIN = new CracUser();

		SusanneADMIN.setName("SusanneADMIN");
		SusanneADMIN.setFirstName("Susanne");
		SusanneADMIN.setLastName("Stiller");
		SusanneADMIN.setPassword(bcryptEncoder.encode("default"));
		SusanneADMIN.addRole(roleDAO.findByName("ADMIN"));
		SusanneADMIN.setPhone("35678987654");
		SusanneADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(SusanneADMIN);

		CracUser SusanneUSER = new CracUser();

		SusanneUSER.setName("SusanneUSER");
		SusanneUSER.setFirstName("Susanne");
		SusanneUSER.setLastName("Stiller");
		SusanneUSER.setPassword(bcryptEncoder.encode("default"));
		SusanneUSER.addRole(roleDAO.findByName("USER"));
		SusanneUSER.setPhone("35678987654");
		SusanneUSER.setEmail("Mustermail@internet.at");
		userDAO.save(SusanneUSER);

		CracUser GerhardADMIN = new CracUser();

		GerhardADMIN.setName("GerhardADMIN");
		GerhardADMIN.setFirstName("Gerhard");
		GerhardADMIN.setLastName("Funk");
		GerhardADMIN.setPassword(bcryptEncoder.encode("default"));
		GerhardADMIN.addRole(roleDAO.findByName("ADMIN"));
		GerhardADMIN.setPhone("35678987654");
		GerhardADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(GerhardADMIN);

		CracUser GerhardUSER = new CracUser();

		GerhardUSER.setName("GerhardUSER");
		GerhardUSER.setFirstName("Gerhard");
		GerhardUSER.setLastName("Funk");
		GerhardUSER.setPassword(bcryptEncoder.encode("default"));
		GerhardUSER.addRole(roleDAO.findByName("USER"));
		GerhardUSER.setPhone("35678987654");
		GerhardUSER.setEmail("Mustermail@internet.at");
		userDAO.save(GerhardUSER);

		// Users for Birgit

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

		// Users for Claudia

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

		// Add fwds

		CracUser BradenADMIN = new CracUser();

		BradenADMIN.setName("BradenADMIN");
		BradenADMIN.setFirstName("Daniel");
		BradenADMIN.setLastName("Braden");
		BradenADMIN.setPassword(bcryptEncoder.encode("default"));
		BradenADMIN.addRole(roleDAO.findByName("ADMIN"));
		BradenADMIN.setPhone("35678987654");
		BradenADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(BradenADMIN);

		CracUser BradenUSER = new CracUser();

		BradenUSER.setName("BradenUSER");
		BradenUSER.setFirstName("Daniel");
		BradenUSER.setLastName("Braden");
		BradenUSER.setPassword(bcryptEncoder.encode("default"));
		BradenUSER.addRole(roleDAO.findByName("USER"));
		BradenUSER.setPhone("35678987654");
		BradenUSER.setEmail("Mustermail@internet.at");
		userDAO.save(BradenUSER);

		CracUser StillerADMIN = new CracUser();

		StillerADMIN.setName("StillerADMIN");
		StillerADMIN.setFirstName("Susanne");
		StillerADMIN.setLastName("Stiller");
		StillerADMIN.setPassword(bcryptEncoder.encode("default"));
		StillerADMIN.addRole(roleDAO.findByName("ADMIN"));
		StillerADMIN.setPhone("35678987654");
		StillerADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(StillerADMIN);

		CracUser StillerUSER = new CracUser();

		StillerUSER.setName("StillerUSER");
		StillerUSER.setFirstName("Susanne");
		StillerUSER.setLastName("Stiller");
		StillerUSER.setPassword(bcryptEncoder.encode("default"));
		StillerUSER.addRole(roleDAO.findByName("USER"));
		StillerUSER.setPhone("35678987654");
		StillerUSER.setEmail("Mustermail@internet.at");
		userDAO.save(StillerUSER);

		CracUser VasicADMIN = new CracUser();

		VasicADMIN.setName("VasicADMIN");
		VasicADMIN.setFirstName("Mili");
		VasicADMIN.setLastName("Vasic");
		VasicADMIN.setPassword(bcryptEncoder.encode("default"));
		VasicADMIN.addRole(roleDAO.findByName("ADMIN"));
		VasicADMIN.setPhone("35678987654");
		VasicADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(VasicADMIN);

		CracUser VasicUSER = new CracUser();

		VasicUSER.setName("VasicUSER");
		VasicUSER.setFirstName("Mili");
		VasicUSER.setLastName("Vasic");
		VasicUSER.setPassword(bcryptEncoder.encode("default"));
		VasicUSER.addRole(roleDAO.findByName("USER"));
		VasicUSER.setPhone("35678987654");
		VasicUSER.setEmail("Mustermail@internet.at");
		userDAO.save(VasicUSER);

		CracUser VitekaADMIN = new CracUser();

		VitekaADMIN.setName("VitekaADMIN");
		VitekaADMIN.setFirstName("Anita");
		VitekaADMIN.setLastName("Viteka");
		VitekaADMIN.setPassword(bcryptEncoder.encode("default"));
		VitekaADMIN.addRole(roleDAO.findByName("ADMIN"));
		VitekaADMIN.setPhone("35678987654");
		VitekaADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(VitekaADMIN);

		CracUser VitekaUSER = new CracUser();

		VitekaUSER.setName("VitekaUSER");
		VitekaUSER.setFirstName("Anita");
		VitekaUSER.setLastName("Viteka");
		VitekaUSER.setPassword(bcryptEncoder.encode("default"));
		VitekaUSER.addRole(roleDAO.findByName("USER"));
		VitekaUSER.setPhone("35678987654");
		VitekaUSER.setEmail("Mustermail@internet.at");
		userDAO.save(VitekaUSER);

		// Add praktikanten

		CracUser VeriADMIN = new CracUser();

		VeriADMIN.setName("VeriADMIN");
		VeriADMIN.setFirstName("Veri");
		VeriADMIN.setLastName("Unknown");
		VeriADMIN.setPassword(bcryptEncoder.encode("default"));
		VeriADMIN.addRole(roleDAO.findByName("ADMIN"));
		VeriADMIN.setPhone("35678987654");
		VeriADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(VeriADMIN);

		CracUser VeriUSER = new CracUser();

		VeriUSER.setName("VeriUSER");
		VeriUSER.setFirstName("Veri");
		VeriUSER.setLastName("Unknown");
		VeriUSER.setPassword(bcryptEncoder.encode("default"));
		VeriUSER.addRole(roleDAO.findByName("USER"));
		VeriUSER.setPhone("35678987654");
		VeriUSER.setEmail("Mustermail@internet.at");
		userDAO.save(VeriUSER);

		CracUser AlexADMIN = new CracUser();

		AlexADMIN.setName("AlexADMIN");
		AlexADMIN.setFirstName("Alex");
		AlexADMIN.setLastName("Unknown");
		AlexADMIN.setPassword(bcryptEncoder.encode("default"));
		AlexADMIN.addRole(roleDAO.findByName("ADMIN"));
		AlexADMIN.setPhone("35678987654");
		AlexADMIN.setEmail("Mustermail@internet.at");
		userDAO.save(AlexADMIN);

		CracUser AlexUSER = new CracUser();

		AlexUSER.setName("AlexUSER");
		AlexUSER.setFirstName("Alex");
		AlexUSER.setLastName("Unknown");
		AlexUSER.setPassword(bcryptEncoder.encode("default"));
		AlexUSER.addRole(roleDAO.findByName("USER"));
		AlexUSER.setPhone("35678987654");
		AlexUSER.setEmail("Mustermail@internet.at");
		userDAO.save(AlexUSER);

	}

	// ELASTICSEARCH

	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/elastic/reset",
			"/elastic/reset/" }, method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> refreshESTasks() {

		DeleteIndexResponse deleted = ect.deleteIndex();
		if (deleted.isAcknowledged()) {
			return JSONResponseHelper.successfullyDeleted(url);
		} else {
			return JSONResponseHelper.createResponse(false, "bad_request", ErrorCode.ID_NOT_FOUND);
		}
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@RequestMapping(value = { "/elastic/sync",
			"/elastic/sync/" }, method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public ResponseEntity<String> syncESTasks() {

		taskDAO.sync();
		
		return JSONResponseHelper.successfullyUpdated(url);
	}

}
