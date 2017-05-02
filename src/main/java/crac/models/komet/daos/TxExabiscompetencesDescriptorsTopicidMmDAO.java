package crac.models.komet.daos;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.komet.entities.TxExabiscompetencesDescriptorsTopicidMm;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface TxExabiscompetencesDescriptorsTopicidMmDAO extends CrudRepository<TxExabiscompetencesDescriptorsTopicidMm, Long> {
	public List<TxExabiscompetencesDescriptorsTopicidMm> findByUidForeign(int uidForeign);
	public List<TxExabiscompetencesDescriptorsTopicidMm> findByUidLocal(int uidLocal);
}
