package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Role;


/**
 * Spring Data CrudRepository for the role entity.
 * @author David Hondl
*/
@Transactional
public interface RoleDAO extends CrudRepository<Role, Long> {
	public Role findByName(String name);
}
