
/*
package crac.test;

import java.util.Calendar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crac.Application;
import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.Task;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebIntegrationTest
public class TaskTest {
	
	@Autowired
	private TaskDAO taskDAO;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Test
	public void test(){
		Task c;
		Task nc;
		
		c = taskDAO.findOne(1l);
		nc = c.copy(Calendar.getInstance());
		try {
			System.out.println(mapper.writeValueAsString(nc));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}*/
