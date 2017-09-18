package crac.enums;

import java.util.Calendar;
import java.util.Set;

import crac.exception.NoSuchStateException;
import crac.exception.RequirementsNotFullfilledException;
import crac.exception.SubItemsNotReadyException;
import crac.models.db.daos.TaskDAO;
import crac.models.db.entities.Task;
import crac.models.db.relation.UserTaskRel;
import crac.module.matching.interfaces.TaskStateInterface;

public enum TaskState implements TaskStateInterface {

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
		public void nextTaskState(Task t, TaskDAO taskDAO) {
			if (t.isSuperTask() && t.fieldsFilled()) {
				if (t.childTasksReady()) {
					t.setGlobalTreeState(TaskState.PUBLISHED, taskDAO);
				}else{
					throw new SubItemsNotReadyException();
				}
			}else{
				throw new RequirementsNotFullfilledException();
			}
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
		public void nextTaskState(Task t, TaskDAO taskDAO) {
			if (t.getStartTime().getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
				t.setTaskState(TaskState.STARTED);
				taskDAO.save(t);
			}
			/*else{
				throw new RequirementsNotFullfilledException();
			}*/

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
		public void nextTaskState(Task t, TaskDAO taskDAO) {
			Set<UserTaskRel> ur = t.getUserRelationships();
			boolean usersDone = true;
			for (UserTaskRel u : ur) {
				if (!u.isCompleted() && u.getParticipationType() == TaskParticipationType.PARTICIPATING) {
					usersDone = false;
				}
			}
			if (usersDone) {
				t.setTaskState(TaskState.COMPLETED);
			}else{
				throw new RequirementsNotFullfilledException();
			}
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
		public void nextTaskState(Task t, TaskDAO taskDAO) {
			throw new NoSuchStateException();
		}
	};

}
