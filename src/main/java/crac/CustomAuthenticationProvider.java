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

import crac.daos.CracUserDAO;
import crac.daos.TokenDAO;
import crac.models.CracToken;
import crac.models.CracUser;
import crac.models.Role;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	CracUserDAO userDAO;

	@Autowired
	TokenDAO tokenDAO;

	@Autowired
	private HttpServletRequest request;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String tokenCode = request.getHeader("Token");
		String name = authentication.getName();
		String password = authentication.getCredentials().toString();
		
		System.out.println(name);
		System.out.println(password);
		System.out.println(tokenCode);

		CracUser user = userDAO.findByName(name);
		CracToken token = tokenDAO.findByCode(tokenCode);

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
					return assignUser(user);
				} else {
					System.out.println("Invalid password!");
					return null;
				}
			} else {
				System.out.println("No username found!");
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

		return new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword(), authorityList);

	}
}