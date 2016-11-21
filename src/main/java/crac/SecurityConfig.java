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

	/**
	 * this converts the login data of the user to a user-entity and compares it
	 * to the users in the database, looking for a match to confirm a registered
	 * user
	 */
    /*
	@Bean
	UserDetailsService userDetailsService() {
		return new UserDetailsService() {

			@Override
			public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {

				if (request.getHeader("Token") != null) {
					System.out.println("intok");
					CracToken t = tokenDAO.findByCode(request.getHeader("Token"));
					if(t != null){
						return assignUser(userDAO.findOne(t.getUserId()));
					}else{
						return new User(null, null, true, true, true, true, null);
					}
				} else {
					System.out.println("innontok");
					CracUser account = userDAO.findByName(name);
					return assignUser(account);
				}
				

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
*/
}

