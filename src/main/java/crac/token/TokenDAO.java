package crac.token;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Evaluation;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface TokenDAO extends CrudRepository<Token, Long> {
	public Token findByCode(String code);
	public Token findByUser(CracUser user);
}
