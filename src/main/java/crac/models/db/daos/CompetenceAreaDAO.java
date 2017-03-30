package crac.models.db.daos;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.CompetenceArea;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface CompetenceAreaDAO extends CrudRepository<CompetenceArea, Long> {
}
