package crac.daos;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;
import crac.models.CracUser;
import crac.relationmodels.UserCompetenceRel;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface UserCompetenceRelDAO extends CrudRepository<UserCompetenceRel, Long> {
	public UserCompetenceRel findByUserAndCompetence(CracUser user, Competence competence);
}
