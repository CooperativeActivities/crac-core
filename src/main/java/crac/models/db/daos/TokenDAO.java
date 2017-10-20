package crac.models.db.daos;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.CracToken;


/**
 * Spring Data CrudRepository for the token entity.
 * @author David Hondl
*/
@Transactional
public interface TokenDAO extends CrudRepository<CracToken, Long> {
	public CracToken findByCode(String code);
	public CracToken findByUserId(Long userId);
}
