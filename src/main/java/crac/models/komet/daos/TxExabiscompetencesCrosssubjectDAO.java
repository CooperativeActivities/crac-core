package crac.models.komet.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.komet.entities.TxExabiscompetencesCrosssubject;


/**
 * Spring Data CrudRepository for the tx-exabiscompetences-crosssubject entity (from Komet-DB)
 * @author David Hondl
 *
 */
@Transactional
public interface TxExabiscompetencesCrosssubjectDAO extends CrudRepository<TxExabiscompetencesCrosssubject, Long> {
}
