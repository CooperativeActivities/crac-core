package crac.controllers;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The main-controller used for hello world and testing
 */
@Controller
public class MainController {
	

  @RequestMapping("/test")
  @ResponseBody
  public String index() {
	  return "Hello World! This is just a test!";

  }
  
  @RequestMapping("/testMe")
  @ResponseBody
  public String indexWord() {
	  UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	  return "Logged in as "+userDetails.getUsername();
  }

}
