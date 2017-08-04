package crac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication(exclude = {EmbeddedServletContainerAutoConfiguration.class, 
		WebMvcAutoConfiguration.class})
/**
 * main class for running the application, this file must be executed
 */
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
