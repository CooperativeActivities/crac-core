package crac;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import crac.models.db.daos.CracUserDAO;
import crac.models.db.daos.TokenDAO;
import crac.models.db.entities.CracToken;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Role;

/**
 * This class handles authentication of users (either by token or basic-authentication)
 * @author David Hondl
 *
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	CracUserDAO userDAO;

	@Autowired
	TokenDAO tokenDAO;

	@Autowired
	private HttpServletRequest request;

	/**
	 * The authenticate-method checks the sent authentication (name and password) and token and allows or disallows the request to target endpoint
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String tokenCode = request.getHeader("Token");
		String name = authentication.getName();
		String password = authentication.getCredentials().toString();
		
		System.out.println("-----------------------");
		System.out.println("INCOMING REQUEST");
		System.out.println("Name: "+name);
		System.out.println("Password: "+password);
		System.out.println("Token: "+tokenCode);
		System.out.println("-----------------------");

		CracUser user = userDAO.findByName(name);
		CracToken token = tokenDAO.findByCode(tokenCode);

		System.out.println("-----------------------");
		System.out.println("LOOKING UP AUTHENTICATION-DATA");
		
		if (token != null) {
			System.out.println("Valid token found!");
			return assignUser(userDAO.findOne(token.getUserId()));
		} else {
			System.out.println("No token found!");
			if (user != null) {
				System.out.println("Username found!");
				BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
				if (bcryptEncoder.matches(password, user.getPassword())) {
					System.out.println("Valid password!");
					System.out.println("-----------------------");
					return assignUser(user);
				} else {
					System.out.println("Invalid password!");
					System.out.println("-----------------------");
					return null;
				}
			} else {
				System.out.println("No username found!");
				System.out.println("-----------------------");
				return null;
			}
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	public UsernamePasswordAuthenticationToken assignUser(CracUser user) {
		List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
		System.out.println("-----------------------");
		System.out.println("USER FOUND - CONFIGURE ACCESS");
		System.out.println("User: "+user.getName());
		try {
			user.getRoles().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Role role : user.getRoles()) {
			System.out.println("Assigned Role with ID "+role.getId()+": "+role.getName());
			authorityList.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
		}

		System.out.println("-----------------------");
		return new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword(), authorityList);

	}
}