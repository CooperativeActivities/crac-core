package crac.daos;


import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Competence;
import crac.models.Evaluation;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface EvaluationDAO extends CrudRepository<Evaluation, Long> {
}
