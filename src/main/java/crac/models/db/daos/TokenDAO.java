package crac.models.db.daos;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Competence;
import crac.models.db.entities.CracToken;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface TokenDAO extends CrudRepository<CracToken, Long> {
	public CracToken findByCode(String code);
	public CracToken findByUserId(Long userId);
}
