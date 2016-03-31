package crac.security;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security")
public class SecurityResponse {

	@RequestMapping(value = "/access", method = RequestMethod.GET, produces="application/json")
    public String greeting(@RequestParam(value="code") String token) {
        return "{\"token\":\""+token+"\"}";
    }
	
}
