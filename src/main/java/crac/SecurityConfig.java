package crac;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import crac.daos.CracUserDAO;
import crac.daos.TokenDAO;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	CracUserDAO userDAO;
	
	@Autowired
	TokenDAO tokenDAO;


	@Autowired
    private CustomAuthenticationProvider authProvider;
 
    @Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }
    
    @Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				// .antMatchers("/adminOnly").hasAuthority("ADMIN")
				// .antMatchers("/openAccess/*").permitAll()
				.anyRequest().fullyAuthenticated().and().httpBasic()
				/*
				 * .and() .logout() .logoutUrl("/logout")
				 * .logoutSuccessUrl("/test")
				 */
				.and().csrf().disable();
	}

}

