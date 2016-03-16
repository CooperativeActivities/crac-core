package crac.controllers;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import crac.models.HelloWorld;
import crac.models.HelloWorldDAO;

@RestController
@RequestMapping("/hello-world")
public class HelloWorldRestController {
	
	  @Autowired
	  private HelloWorldDAO helloWorldDAO;

	@RequestMapping(value = "/getAll", method = RequestMethod.GET)
	  @ResponseBody
	  public String getAll(String hello, String world) {
		Iterable<HelloWorld> worldList = new ArrayList<HelloWorld>();
	    try {
	    	worldList = helloWorldDAO.findAll();
	    }
	    catch (Exception ex) {
	      System.out.println("Error creating the user: " + ex.toString());
	    }
	    return new Gson().toJson(worldList);
	  }
	
}

	
  