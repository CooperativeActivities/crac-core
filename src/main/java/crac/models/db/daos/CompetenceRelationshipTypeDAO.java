package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.relation.CompetenceRelationshipType;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface CompetenceRelationshipTypeDAO extends CrudRepository<CompetenceRelationshipType, Long> {
}
