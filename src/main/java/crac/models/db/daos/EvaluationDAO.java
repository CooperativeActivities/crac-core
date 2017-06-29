package crac.models.db.daos;


import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Competence;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface EvaluationDAO extends CrudRepository<Evaluation, Long> {
	public List<Evaluation> findByUserAndFilled(CracUser user, boolean filled);

}
