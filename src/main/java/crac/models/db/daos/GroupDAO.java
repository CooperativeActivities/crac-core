package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Competence;
import crac.models.db.entities.CracGroup;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface GroupDAO extends CrudRepository<CracGroup, Long> {
	public CracGroup findByName(String name);
	public CracGroup findByIdAndName(long id, String name);
}
