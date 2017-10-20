package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Material;
import crac.models.db.relation.UserMaterialSubscription;


/**
 * Spring Data CrudRepository for the user-material-subscription entity.
 * @author David Hondl
*/
@Transactional
public interface UserMaterialSubscriptionDAO extends CrudRepository<UserMaterialSubscription, Long> {
	public UserMaterialSubscription findByMaterial(Material material);
	public UserMaterialSubscription findByUser(CracUser user);
	public UserMaterialSubscription findByUserAndMaterial(CracUser user, Material material);
}
