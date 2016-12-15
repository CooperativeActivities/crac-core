package crac.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.Attachment;
import crac.models.Competence;
import crac.models.Task;
import crac.models.relation.CompetenceTaskRel;


/**
 * Spring Data CrudRepository for the competence entity.
 */
@Transactional
public interface CompetenceTaskRelDAO extends CrudRepository<CompetenceTaskRel, Long> {
	public CompetenceTaskRel findByTaskAndCompetence(Task task, Competence competence);
}
