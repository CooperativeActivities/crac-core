package crac.models.komet.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.komet.entities.TxExabiscompetencesTopic;


/**
 * Spring Data CrudRepository for the tx-exabiscompetences-topic entity (from Komet-DB)
 * @author David Hondl
 *
 */
@Transactional
public interface TxExabiscompetencesTopicDAO extends CrudRepository<TxExabiscompetencesTopic, Long> {
}
