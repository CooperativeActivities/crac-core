package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.CracUser;
import crac.models.Material;
import crac.models.relation.UserMaterialSubscription;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface UserMaterialSubscriptionDAO extends CrudRepository<UserMaterialSubscription, Long> {
	public UserMaterialSubscription findByMaterial(Material material);
	public UserMaterialSubscription findByUser(CracUser user);
	public UserMaterialSubscription findByUserAndMaterial(CracUser user, Material material);
}
