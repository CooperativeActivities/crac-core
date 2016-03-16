package crac.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

  @RequestMapping("/")
  @ResponseBody
  public String index() {
	  return "Hello! Jersey up and running! <br/> Go to: <a href=\"hello-world\">me</a>";
  }

}
