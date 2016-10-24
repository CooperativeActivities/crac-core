package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Role;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface RoleDAO extends CrudRepository<Role, Long> {
	public Role findByName(String name);
}
