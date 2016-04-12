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
import crac.daos.CracUserDAO;
import crac.models.CracUser;

import java.net.URL;

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
		//Last assertion can't work, because the password has no json output
		//assertThat(compareUser1.getPassword(), is(compareUser2.getPassword()));
	}
}