package crac.models.db.daos;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Evaluation;


/**
 * Spring Data CrudRepository for the evaluation entity.
 * @author David Hondl
*/
@Transactional
public interface EvaluationDAO extends CrudRepository<Evaluation, Long> {
	
}
