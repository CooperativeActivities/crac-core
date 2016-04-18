package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;
import crac.models.Group;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface GroupDAO extends CrudRepository<Group, Long> {
	public Group findByName(String name);
}
