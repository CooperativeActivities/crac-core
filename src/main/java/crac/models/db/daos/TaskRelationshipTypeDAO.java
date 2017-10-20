package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.relation.TaskRelationshipType;


/**
 * Spring Data CrudRepository for the task-relationship-type entity.
 * @author David Hondl
*/
@Transactional
public interface TaskRelationshipTypeDAO extends CrudRepository<TaskRelationshipType, Long> {
}
