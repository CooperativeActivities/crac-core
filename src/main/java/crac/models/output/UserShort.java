package crac.models.output;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Role;
import lombok.Getter;
import lombok.Setter;

public class UserShort {
	
	@Getter
	@Setter
	private long id;

	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private String email;
		
	@Getter
	@Setter
	private String lastName;
	
	@Getter
	@Setter
	private String firstName;
	
	@Getter
	@Setter
	private String phone;
	
	@Getter
	@Setter
	private Set<RoleShort> roles;

	public UserShort(CracUser u){
		
		this.id = u.getId();
		this.name = u.getName();
		this.email = u.getEmail();
		this.lastName = u.getLastName();
		this.firstName = u.getFirstName();
		this.phone = u.getPhone();
		
		roles = new HashSet<>();
		
		for(Role r : u.getRoles()){
			roles.add(new RoleShort(r));
		}
		
	}

}
