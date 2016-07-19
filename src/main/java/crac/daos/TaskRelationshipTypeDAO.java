package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.relationmodels.CompetenceRelationshipType;
import crac.relationmodels.TaskRelationshipType;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface TaskRelationshipTypeDAO extends CrudRepository<TaskRelationshipType, Long> {
}
