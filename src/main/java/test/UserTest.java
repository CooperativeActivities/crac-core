package test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import crac.Application;
import crac.daos.CompetenceDAO;
import crac.daos.CracUserDAO;
import crac.daos.TaskDAO;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=8080"})
public class UserTest {
	private URL base;
	private RestTemplate template;
	
	@Autowired
	private CracUserDAO cracUserDAO;

	@Autowired
	private CompetenceDAO competenceDAO;

	@Autowired
	private TaskDAO taskDAO;

	@Before
	public void setUp() throws Exception {
		this.base = new URL("http://localhost:8080/user");
		template = new TestRestTemplate();
	}

	@Test
	public void indexUser() throws Exception {
		Long userId = 1L;
		ResponseEntity<String> response = template.getForEntity(base.toString()+"/"+userId.toString(), String.class);
		ObjectMapper mapper = new ObjectMapper();
		CracUser compareUser1 = mapper.readValue(response.getBody(), CracUser.class);
		CracUser compareUser2 = cracUserDAO.findOne(userId);

		assertThat(compareUser1.getId(), is(compareUser2.getId()));
		assertThat(compareUser1.getName(), is(compareUser2.getName()));
		//Password can't be compared because the salt is different everytime it's called
	}
	
	@Test
	public void createPutDeleteUser() throws Exception {
		
		int lenghtStart = 0;
		int lengtAfterInsert = 0;
		int lenghtAfterDelete = 0;
		
		for(CracUser value : cracUserDAO.findAll()) {
			lenghtStart++;
		}
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", "testCaseName153!");
		//map.put("password", "addDummyPwHere");
		
		template.postForEntity(base.toString(), map, String.class);
		
		for(CracUser value : cracUserDAO.findAll()) {
			lengtAfterInsert++;
		}
		
		assertThat(lenghtStart+1, is(lengtAfterInsert));
		assertThat(cracUserDAO.findByName("testCaseName153!").getName(), is("testCaseName153!"));
		
		Map<String, String> updateMap = new HashMap<String, String>();
		updateMap.put("name", "testCaseRenamedName153!");
		
		template.put(base.toString()+"/"+cracUserDAO.findByName("testCaseName153!").getId(), updateMap);
		
		assertThat(cracUserDAO.findByName("testCaseRenamedName153!").getName(), is("testCaseRenamedName153!"));
		
		template.delete(base.toString()+"/"+cracUserDAO.findByName("testCaseRenamedName153!").getId());
		
		for(CracUser value : cracUserDAO.findAll()) {
			lenghtAfterDelete++;
		}
		
		assertThat(lenghtStart, is(lenghtAfterDelete));
				
	}
	
}