package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Attachment;
import crac.models.db.entities.Task;

/**
 * Spring Data CrudRepository for the attachment entity.
 * 
 * @author David Hondl
*/
@Transactional
public interface AttachmentDAO extends CrudRepository<Attachment, Long> {
	public Attachment findByName(String name);

	public Attachment findByIdAndTask(long id, Task task);
}
