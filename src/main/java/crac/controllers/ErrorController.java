package crac.controllers;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements EmbeddedServletContainerCustomizer {
	@Override
	public void customize(final ConfigurableEmbeddedServletContainer factory) {
		ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/401");

		factory.addErrorPages(error401Page);
	}

	@RequestMapping("/401")
	public ResponseEntity<String> template401() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return ResponseEntity.ok().headers(headers).body("{\"status\": 401, \"error\": \"Unauthorized\"}");
	}

}
