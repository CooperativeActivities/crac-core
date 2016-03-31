package crac.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer // [2]
 class ResourceServer extends ResourceServerConfigurerAdapter {
	
	private static final String RESOURCE_ID = "crac_resource";

     @Override // [3]
     public void configure(HttpSecurity http) throws Exception {
          // @formatter:off
          http
          // Just for laughs, apply OAuth protection to only 2 resources
          .requestMatchers().and()
          .authorizeRequests()
          .anyRequest().access("#oauth2.hasScope('read')"); //[4]
          // @formatter:on
     }

     @Override
     public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
          resources.resourceId(RESOURCE_ID);
     }
     
}