package crac.models.komet.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.komet.entities.TxExabiscompetencesCrosssubjectsCompetenceMm;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface TxExabiscompetencesCrosssubjectsCompetenceMmDAO extends CrudRepository<TxExabiscompetencesCrosssubjectsCompetenceMm, Long> {
}
