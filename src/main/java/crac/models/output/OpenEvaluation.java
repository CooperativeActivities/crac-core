package crac.models.output;

import crac.enums.TaskParticipationType;
import crac.models.db.entities.Evaluation;
import crac.models.db.relation.UserTaskRel;

public class OpenEvaluation {
	
	private TaskShort task;
	
	private TaskParticipationType participationType;
	
	private Evaluation evaluation;
	
	public OpenEvaluation(UserTaskRel rel){
		this.task = new TaskShort(rel.getTask());
		this.participationType = rel.getParticipationType();
		this.evaluation = rel.getEvaluation();
	}

	public TaskShort getTask() {
		return task;
	}

	public void setTask(TaskShort task) {
		this.task = task;
	}

	public TaskParticipationType getParticipationType() {
		return participationType;
	}

	public void setParticipationType(TaskParticipationType participationType) {
		this.participationType = participationType;
	}

	public Evaluation getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

}
