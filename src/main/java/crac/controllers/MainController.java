package crac.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

  @RequestMapping("/test")
  @ResponseBody
  public String index() {
	  return "Hello World! This is just a test!";
  }

}
