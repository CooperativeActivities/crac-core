package crac.daos;


import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;
import crac.models.CracUser;
import crac.models.relation.UserCompetenceRel;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface UserCompetenceRelDAO extends CrudRepository<UserCompetenceRel, Long> {
	public UserCompetenceRel findByUserAndCompetence(CracUser user, Competence competence);
	public Set<UserCompetenceRel> findByUser(CracUser user);
}
