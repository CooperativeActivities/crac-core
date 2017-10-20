package crac.models.output;

import crac.enums.TaskParticipationType;
import crac.models.db.entities.Evaluation;
import crac.models.db.entities.Task.TaskShort;
import crac.models.db.relation.UserTaskRel;
import lombok.Data;

/**
 * Helper class for output, that contains information about an open evaluation
 * @author David Hondl
 *
 */
@Data
public class OpenEvaluation {
	
	private TaskShort task;
	private TaskParticipationType participationType;
	private Evaluation evaluation;
	
	public OpenEvaluation(UserTaskRel rel){
		this.task = rel.getTask().toShort();
		this.participationType = rel.getParticipationType();
		this.evaluation = rel.getEvaluation();
	}

}
