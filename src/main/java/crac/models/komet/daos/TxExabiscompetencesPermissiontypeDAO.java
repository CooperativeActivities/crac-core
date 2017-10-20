package crac.models.komet.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.komet.entities.TxExabiscompetencesPermissiontype;


/**
 * Spring Data CrudRepository for the tx-exabiscompetences-permission-type entity (from Komet-DB)
 * @author David Hondl
 *
 */
@Transactional
public interface TxExabiscompetencesPermissiontypeDAO extends CrudRepository<TxExabiscompetencesPermissiontype, Long> {
}
