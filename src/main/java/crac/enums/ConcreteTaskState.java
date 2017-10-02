package crac.enums;

import java.util.Set;

import crac.exception.InvalidActionException;
import crac.exception.RequirementsNotFullfilledException;
import crac.exception.SubItemsNotReadyException;
import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;

public enum ConcreteTaskState implements TaskState {

	NOT_PUBLISHED {
		@Override
		public boolean isJoinable() {
			return false;
		}

		@Override
		public boolean isExtendable() {
			return true;
		}

		@Override
		public boolean inConduction() {
			return false;
		}

		@Override
		public void set(Task t, TaskState state, TaskDAO taskDAO) throws InvalidActionException {

			if (t.isSuperTask() && t.fieldsFilled()) {
				if (t.childTasksReady()) {
					t.setGlobalTreeState(ConcreteTaskState.PUBLISHED, taskDAO);
					taskDAO.save(t);
				} else {
					throw new SubItemsNotReadyException();
				}
			} else {
				throw new RequirementsNotFullfilledException();
			}
		}

		@Override
		public boolean isInteractable() {
			return false;
		}

		@Override
		public TaskState nextState() {
			return ConcreteTaskState.PUBLISHED;
		}
	},

	PUBLISHED {
		@Override
		public boolean isJoinable() {
			return true;
		}

		@Override
		public boolean isExtendable() {
			return true;
		}

		@Override
		public boolean inConduction() {
			return false;
		}

		@Override
		public void set(Task t, TaskState state, TaskDAO taskDAO) throws InvalidActionException {

			if (t.checkStartAllowance()) {
				t.setTaskState(ConcreteTaskState.STARTED);
				taskDAO.save(t);
			} else {
				throw new InvalidActionException(ErrorCode.REQUIREMENTS_NOT_FULLFILLED);
			}
		}

		@Override
		public boolean isInteractable() {
			return true;
		}

		@Override
		public TaskState nextState() {
			return ConcreteTaskState.STARTED;
		}
	},

	STARTED {
		@Override
		public boolean isJoinable() {
			return true;
		}

		@Override
		public boolean isExtendable() {
			return true;
		}

		@Override
		public boolean inConduction() {
			return true;
		}

		@Override
		public void set(Task t, TaskState state, TaskDAO taskDAO) throws InvalidActionException {

			Set<UserTaskRel> ur = t.getUserRelationships();
			boolean usersDone = true;
			for (UserTaskRel u : ur) {
				if (!u.isCompleted() && u.getParticipationType() == TaskParticipationType.PARTICIPATING) {
					usersDone = false;
				}
			}
			if (usersDone) {
				t.setTaskState(ConcreteTaskState.COMPLETED);
				taskDAO.save(t);
			} else {
				throw new InvalidActionException(ErrorCode.REQUIREMENTS_NOT_FULLFILLED);
			}
		}

		@Override
		public boolean isInteractable() {
			return true;
		}

		@Override
		public TaskState nextState() {
			return ConcreteTaskState.COMPLETED;
		}
	},

	COMPLETED {
		@Override
		public boolean isJoinable() {
			return false;
		}

		@Override
		public boolean isExtendable() {
			return false;
		}

		@Override
		public boolean inConduction() {
			return true;
		}

		@Override
		public void set(Task t, TaskState state, TaskDAO taskDAO) throws InvalidActionException {
			throw new InvalidActionException(ErrorCode.NO_SUCH_STATE_FOUND);
		}

		@Override
		public boolean isInteractable() {
			return false;
		}

		@Override
		public TaskState nextState() throws InvalidActionException {
			throw new InvalidActionException(ErrorCode.NO_SUCH_STATE_FOUND);
		}
	};

}

interface TaskState {
	
	abstract boolean isJoinable();

	abstract boolean isInteractable();

	abstract boolean isExtendable();

	abstract boolean inConduction();

	abstract TaskState nextState() throws InvalidActionException;

	abstract void set(Task t, TaskState state, TaskDAO taskDAO) throws InvalidActionException;

	default boolean checkNext(TaskState t) throws InvalidActionException {
		return t == nextState();
	}

	default void setTaskState(Task t, TaskState state, TaskDAO taskDAO) throws InvalidActionException {
		if (!checkNext(state)) {
			throw new InvalidActionException(ErrorCode.STATE_NOT_ALLOWED);
		}
		set(t, state, taskDAO);
	}
}
