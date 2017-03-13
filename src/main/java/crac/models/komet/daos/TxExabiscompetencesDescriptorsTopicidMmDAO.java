package crac.models.komet.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.komet.entities.TxExabiscompetencesDescriptorsTopicidMm;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface TxExabiscompetencesDescriptorsTopicidMmDAO extends CrudRepository<TxExabiscompetencesDescriptorsTopicidMm, Long> {
}
