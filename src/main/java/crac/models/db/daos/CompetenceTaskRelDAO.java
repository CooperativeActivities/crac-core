package crac.models.db.daos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import crac.models.db.entities.Competence;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;


/**
 * Spring Data CrudRepository for the competence-task-relationship entity.
 * @author David Hondl
*/
@Transactional
public interface CompetenceTaskRelDAO extends CrudRepository<CompetenceTaskRel, Long> {
	public CompetenceTaskRel findByTaskAndCompetence(Task task, Competence competence);
}
