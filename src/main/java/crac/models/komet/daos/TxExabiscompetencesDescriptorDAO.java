package crac.models.komet.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.komet.entities.TxExabiscompetencesDescriptor;


/**
 * Spring Data CrudRepository for the tx-exabiscompetences-descriptor entity (from Komet-DB)
 * @author David Hondl
 *
 */
@Transactional
public interface TxExabiscompetencesDescriptorDAO extends CrudRepository<TxExabiscompetencesDescriptor, Long> {
}
