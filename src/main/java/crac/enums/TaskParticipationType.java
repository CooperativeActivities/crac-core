package crac.enums;

import crac.models.db.entities.Task;
import crac.module.matching.interfaces.ErrorStatus;

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

		@Override
		public boolean changeTo(ParticipationType pi) {
			// TODO Auto-generated method stub
			return false;
		}

	},
	FOLLOWING {

		@Override
		public ErrorStatus applicable(Task t) {
			if (t.getTaskState().isInteractable()) {
				return ErrorCode.TASK_NOT_INTERACTABLE;
			}
			return () -> false;
		}

		@Override
		public boolean changeTo(ParticipationType pi) {
			// TODO Auto-generated method stub
			return false;
		}

	},
	LEADING {

		@Override
		public ErrorStatus applicable(Task t) {
			return  ErrorCode.ACTION_NOT_VALID;
		}

		@Override
		public boolean changeTo(ParticipationType pi) {
			// TODO Auto-generated method stub
			return false;
		}

	},
	MATERIAL{

		@Override
		public ErrorStatus applicable(Task t) {
			if (t.getTaskState().isInteractable()) {
				return ErrorCode.TASK_NOT_INTERACTABLE;
			}
			return () -> false;
		}

		@Override
		public boolean changeTo(ParticipationType pi) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}

}

interface ParticipationType {

	public ErrorStatus applicable(Task t);
	
	public boolean changeTo(ParticipationType pi);
	
}