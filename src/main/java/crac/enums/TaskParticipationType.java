package crac.enums;

import crac.models.db.entities.Task;
import crac.module.matching.interfaces.ErrorStatus;

/**
 * This enum represents different kinds of participation of a user on target task
 * Every type implements different methods, defined be the ParticipationType-interface
 * @author David Hondl
 *
 */
public enum TaskParticipationType implements ParticipationType{

	PARTICIPATING {

		@Override
		public ErrorStatus applicable(Task t) {
			if (!t.getTaskState().isJoinable()) {
				return ErrorCode.TASK_NOT_JOINABLE;
			}
			
			if(t.isFull()){
				return ErrorCode.TASK_IS_FULL;
			}
			
			return () -> false;
		}


	},
	FOLLOWING {

		@Override
		public ErrorStatus applicable(Task t) {
			if (!t.getTaskState().isInteractable()) {
				return ErrorCode.TASK_NOT_INTERACTABLE;
			}
			return () -> false;
		}

	},
	LEADING {

		@Override
		public ErrorStatus applicable(Task t) {
			return  ErrorCode.ACTION_NOT_VALID;
		}

	};

	@Override
	public boolean changeTo(ParticipationType pi) {
		if(pi == TaskParticipationType.LEADING){
			return false;
		}
		return true;
	}
	
}

interface ParticipationType {

	public ErrorStatus applicable(Task t);
	
	public boolean changeTo(ParticipationType pi);
	
}