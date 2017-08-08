package crac.models.output;

import crac.models.db.entities.Role;
import lombok.Getter;
import lombok.Setter;

public class RoleShort {
	
	@Getter
	@Setter
	private long id;

	@Getter
	@Setter
	private String name;

	public RoleShort(Role r){
		this.id = r.getId();
		this.name = r.getName();
	}

}
