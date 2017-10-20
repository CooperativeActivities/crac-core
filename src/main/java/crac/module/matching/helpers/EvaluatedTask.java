package crac.module.matching.helpers;

import crac.models.db.entities.Task;
import lombok.Data;

/**
 * Helperclass that represents target task and its assessed matching-score
 * Comparable to other objects of this class based on its assessment
 * @author David Hondl
 *
 */
@Data
public class EvaluatedTask implements Comparable<Object>{
	
	private Task task;
	
	private double assessment;
	
	private boolean doable;

	public EvaluatedTask(Task task, double assessment) {
		this.task = task;
		this.assessment = assessment;
		this.doable = true;
	}

	@Override
	public int compareTo(Object task) {
		if(((EvaluatedTask) task).getAssessment() > this.getAssessment()){
			return 1;
		}else if(((EvaluatedTask) task).getAssessment() == this.getAssessment()){
			return 0;
		}else{
			return -1;
		}
	}
	
}
