package crac.daos;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface CompetenceDAO extends CrudRepository<Competence, Long> {
	public Competence findByName(String name);
}
