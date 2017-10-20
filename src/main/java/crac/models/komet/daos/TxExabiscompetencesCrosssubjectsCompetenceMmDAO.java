package crac.models.komet.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.komet.entities.TxExabiscompetencesCrosssubjectsCompetenceMm;


/**
 * Spring Data CrudRepository for the tx-exabiscompetences-crosssubjects-competence entity (from Komet-DB)
 * @author David Hondl
 *
 */
@Transactional
public interface TxExabiscompetencesCrosssubjectsCompetenceMmDAO extends CrudRepository<TxExabiscompetencesCrosssubjectsCompetenceMm, Long> {
}
