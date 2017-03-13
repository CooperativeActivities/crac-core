package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Competence;
import crac.models.db.entities.Group;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface GroupDAO extends CrudRepository<Group, Long> {
	public Group findByName(String name);
}
