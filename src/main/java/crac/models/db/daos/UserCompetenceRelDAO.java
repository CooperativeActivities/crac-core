package crac.models.db.daos;


import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.relation.UserCompetenceRel;


/**
 * Spring Data CrudRepository for the user-competence-relationship entity.
 * @author David Hondl
*/
@Transactional
public interface UserCompetenceRelDAO extends CrudRepository<UserCompetenceRel, Long> {
	public UserCompetenceRel findByUserAndCompetence(CracUser user, Competence competence);
	public Set<UserCompetenceRel> findByUser(CracUser user);
}
