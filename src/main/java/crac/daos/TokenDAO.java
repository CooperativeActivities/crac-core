package crac.daos;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Evaluation;
import crac.models.CracToken;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface TokenDAO extends CrudRepository<CracToken, Long> {
	public CracToken findByCode(String code);
	public CracToken findByUserId(Long userId);
}
