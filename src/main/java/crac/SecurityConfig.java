package crac;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import crac.daos.CracUserDAO;
import crac.models.CracUser;
import crac.models.Role;

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

  @Autowired
  CracUserDAO userDAO;

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
    	CracUser account = userDAO.findByName(name);
    	List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
    	System.out.println(account.getName());
    	try{
    		account.getRoles().toString();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	for(Role role : account.getRoles()){
    		System.out.println(role.getId());
    		authorityList.add(new SimpleGrantedAuthority("ROLE_"+role.getName()));
    	}
    	if(account != null) {
    		/*return new User(account.getName(), account.getPassword(), true, true, true, true,
        			AuthorityUtils.createAuthorityList("ROLE_ADMIN"));*/
        	
        return new User(account.getName(), account.getPassword(), true, true, true, true,
        		authorityList);
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
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
 
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
    	//.antMatchers("/adminOnly").hasAuthority("ADMIN")
    	//.antMatchers("/openAccess/*").permitAll()
    	.anyRequest().fullyAuthenticated()
    	.and()
	    	.httpBasic()
	    	/*
	    .and()
		    .logout()
	        .logoutUrl("/logout")
	        .logoutSuccessUrl("/test")
	        */
	    .and()
	    	.csrf().disable();
  }
  
}