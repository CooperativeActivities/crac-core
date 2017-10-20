package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.CracUser;
import crac.models.db.relation.UserRelationship;


/**
 * Spring Data CrudRepository for the user-relationship entity.
 * @author David Hondl
*/
@Transactional
public interface UserRelationshipDAO extends CrudRepository<UserRelationship, Long> {
	public UserRelationship findByC1AndC2(CracUser c1, CracUser c2);
}
