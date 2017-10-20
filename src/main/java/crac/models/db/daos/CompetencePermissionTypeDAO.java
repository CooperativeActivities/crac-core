package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.relation.CompetencePermissionType;


/**
 * Spring Data CrudRepository for the competence-permission-type entity.
 * @author David Hondl
*/
@Transactional
public interface CompetencePermissionTypeDAO extends CrudRepository<CompetencePermissionType, Long> {
}
