package crac;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import crac.daos.CracUserDAO;
import crac.models.CracUser;

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

  @Autowired
  CracUserDAO accountRepository;

  @Override
  public void init(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService()).passwordEncoder(new BCryptPasswordEncoder());
  }

  /**
   * this converts the login data of the user to a user-entity and compares it to the users in the
   * database, looking for a match to confirm a registered user
   */
  @Bean
  UserDetailsService userDetailsService() {
    return new UserDetailsService() {

      @Override
      public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
    	CracUser account = accountRepository.findByName(name);
        if(account != null) {
        return new User(account.getName(), account.getPassword(), true, true, true, true,
                AuthorityUtils.createAuthorityList("USER"));
        } else {
          throw new UsernameNotFoundException("could not find the user '"
                  + name + "'");
        }
      }
      
    };
  }
}

/**
 * the security configuration for this web application
 */
@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
 
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
    	.anyRequest().fullyAuthenticated()
    	.and()
	    	.httpBasic()
	    .and()
		    .logout()
	        .logoutUrl("/logout")
	        .logoutSuccessUrl("/test")
	    .and()
	    	.csrf().disable();
  }
  
}