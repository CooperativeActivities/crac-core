package crac;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationDetailsSource;
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
import org.springframework.web.bind.annotation.RequestHeader;

import crac.daos.CracUserDAO;
import crac.daos.TokenDAO;
import crac.models.CracUser;
import crac.models.Role;
import crac.models.CracToken;

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	CracUserDAO userDAO;
	
	@Autowired
	TokenDAO tokenDAO;

	@Autowired
	private HttpServletRequest request;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService()).passwordEncoder(new BCryptPasswordEncoder());
	}

	/**
	 * this converts the login data of the user to a user-entity and compares it
	 * to the users in the database, looking for a match to confirm a registered
	 * user
	 */
	@Bean
	UserDetailsService userDetailsService() {
		return new UserDetailsService() {

			@Override
			public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
/*
				if (request.getHeader("Token") != null) {
				go
					System.out.println("intok");
					Token t = tokenDAO.findByCode(request.getHeader("Token"));
					if(t != null){
						return assignUser(t.getUser());
					}else{
						return new User(null, null, true, true, true, true, null);
					}
				} else {
					System.out.println("innontok");
					CracUser account = userDAO.findByName(name);
					return assignUser(account);
				}*/
				CracUser account = userDAO.findByName(name);
				return assignUser(account);

			}

		};
	}

	public User assignUser(CracUser user) {
		List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
		System.out.println(user.getName());
		try {
			user.getRoles().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Role role : user.getRoles()) {
			System.out.println(role.getId());
			authorityList.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
		}
		if (user != null) {
			return new User(user.getName(), user.getPassword(), true, true, true, true, authorityList);
		} else {
			throw new UsernameNotFoundException("could not find the user");
		}
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