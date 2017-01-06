package crac.models.storage;

import java.util.ArrayList;
import java.util.Set;

import crac.enums.TaskParticipationType;
import crac.models.Competence;
import crac.models.CracUser;
import crac.models.Task;
import crac.models.relation.CompetenceTaskRel;
import crac.models.relation.UserCompetenceRel;
import crac.models.relation.UserRelationship;
import crac.models.relation.UserTaskRel;
import crac.storage.CompetenceStorage;

public class CompetenceCollectionMatrix {

	private MatrixField[][] matrix;
	private String[] rowsU;
	private String[] columnsT;
	private CracUser u;
	private Task t;
	private boolean doable;
	private ArrayList<String> mandatoryViolations = new ArrayList<>();
	private Set<UserCompetenceRel> userComps;
	private Set<CompetenceTaskRel> taskComps;
	private SearchFilter sf;

	public CompetenceCollectionMatrix(CracUser u, Task t, SearchFilter sf) {
		this.u = u;
		this.t = t;
		this.doable = true;

		this.userComps = u.getCompetenceRelationships();
		this.taskComps = t.getMappedCompetences();
		this.sf = sf;

		matrix = new MatrixField[userComps.size()][taskComps.size()];
		rowsU = new String[userComps.size()];
		columnsT = new String[taskComps.size()];

		buildMatrix();
		markMandatoryViolation();
		if (sf.isSet()) {
			applyFilters(sf);
		}

	}

	private void buildMatrix() {
		int uCount = 0;
		for (UserCompetenceRel ucr : userComps) {
			rowsU[uCount] = ucr.getCompetence().getName();
			int tCount = 0;
			for (CompetenceTaskRel ctr : taskComps) {
				if (ctr.isMandatory()) {
					columnsT[tCount] = ctr.getCompetence().getName() + "(m!)";
				} else {
					columnsT[tCount] = ctr.getCompetence().getName();
				}
				matrix[uCount][tCount] = new MatrixField(ctr, ucr,
						CompetenceStorage.getCompetenceSimilarity(ucr.getCompetence(), ctr.getCompetence()));
				tCount++;
			}
			uCount++;
		}
	}

	private void applyFilters(SearchFilter sf) {

		for (MatrixField[] row : matrix) {
			for (MatrixField field : row) {
				System.out.println("---------------------------");
				System.out.println("ROW:");
				System.out.println("Original: "+field.getVal());
				int neededProficiency = field.getTaskRelation().getNeededProficiencyLevel();
				int proficiencyValue = field.getUserRelation().getProficiencyValue();
				int likeValue = field.getUserRelation().getLikeValue();
				int importanceValue = field.getTaskRelation().getImportanceLevel();

				if (sf.getProficiency() == 1) {
					field.setVal(addProficiencyLevel(field.getVal(), neededProficiency, proficiencyValue));
				}
				System.out.println("After Proficiency: "+field.getVal());
				if (sf.getLike() == 1) {
					field.setVal(addLikeLevel(field.getVal(), likeValue));
				}
				System.out.println("After Like: "+field.getVal());
				if (sf.getFriends() == 1) {
					field.setVal(addFriendsLevel(field.getVal(), field.getUserRelation().getUser(),
							field.getTaskRelation().getTask()));
				}
				System.out.println("After Friends: "+field.getVal());
				if (sf.getImportance() == 1) {
					field.setVal(addImportancyLevel(field.getVal(), importanceValue));
				}
				System.out.println("After Importance: "+field.getVal());
				System.out.println("---------------------------");
			}
		}
	}

	private double addFriendsLevel(double value, CracUser user, Task t) {

		double newVal = value;

		ArrayList<UserRelationship> others = getRelatedPersons(user, t);

		for (UserRelationship rel : others) {
			newVal = newVal * (1 + (((1 - newVal / 2) * (double) rel.getLikeValue() / 100) * 0.7));
		}

		return newVal;
	}

	private ArrayList<UserRelationship> getRelatedPersons(CracUser user, Task t) {
		ArrayList<UserRelationship> others = new ArrayList<>();
		for (UserTaskRel trel : t.getUserRelationships()) {
			if (trel.getParticipationType() == TaskParticipationType.PARTICIPATING) {
				for (UserRelationship urel : trel.getUser().getUserRelationshipsAs1()) {
					if (urel.getC2().getId() == user.getId()) {
						others.add(urel);
					}
				}
				for (UserRelationship urel : trel.getUser().getUserRelationshipsAs2()) {
					if (urel.getC1().getId() == user.getId()) {
						others.add(urel);
					}
				}
			}
		}
		return others;
	}

	private double addProficiencyLevel(double value, int neededProficiency, int proficiencyValue) {
		double newVal = value;
		if (proficiencyValue < neededProficiency) {
			newVal = value * ((double) 1 - (((double) neededProficiency / 100) - ((double) proficiencyValue / 100)));
		}
		return newVal;
	}

	private double addLikeLevel(double value, int likeValue) {

		double newVal = value * (1 + (1 - value / 2) * (double) likeValue / 100);

		if (newVal > 1) {
			newVal = 1;
		} else if (newVal < 0) {
			newVal = 0;
		}

		return newVal;

	}

	private double addImportancyLevel(double value, int importancyValue) {

		double newVal = value;

		// do only if the value is not 1, since 1 means that the user possesses
		// the competence
		if (value != 1) {
			newVal = value * (1 - ((double) importancyValue / 300));
		}
		return newVal;
	}

	private void markMandatoryViolation() {

		double[] columns = bestColumn();

		for (int i = 0; i < columns.length; i++) {

			CompetenceTaskRel t = null;
			int c = 0;

			for (CompetenceTaskRel ctr : taskComps) {
				if (c == i) {
					t = ctr;
				}
				c++;
			}

			if (t.isMandatory() && columns[i] < 1) {
				this.doable = false;
				this.mandatoryViolations.add("Violation -> " + t.getCompetence().getName() + ": " + columns[i]);
			}
		}

	}

	public double calcMatch() {
		return calcMatchColumn();
		// return calcMatchRow();
	}

	private double calcMatchColumn() {

		double[] intermediate = bestColumn();

		double sum = 0;

		for (int i = 0; i < intermediate.length; i++) {
			sum += intermediate[i];
		}

		double result = sum / intermediate.length;

		return (double) Math.round(result * 100) / 100;
	}

	private double calcMatchRow() {

		double[] intermediate = bestRow();

		double sum = 0;

		for (int i = 0; i < intermediate.length; i++) {
			sum += intermediate[i];
		}

		double result = sum / intermediate.length;

		return (double) Math.round(result * 100) / 100;
	}

	private double[] bestColumn() {

		double[] intermediate = new double[matrix[0].length];

		for (int i = 0; i < matrix.length; i++) {

			for (int j = 0; j < matrix[i].length; j++) {

				if (matrix[i][j].getVal() > intermediate[j]) {
					intermediate[j] = matrix[i][j].getVal();
				}
			}
		}

		return intermediate;

	}

	private double[] bestRow() {

		double[] intermediate = new double[matrix.length];

		for (int i = 0; i < matrix.length; i++) {

			for (int j = 0; j < matrix[i].length; j++) {

				if (matrix[i][j].getVal() > intermediate[i]) {
					intermediate[i] = matrix[i][j].getVal();
				}
			}
		}

		return intermediate;

	}

	public boolean isDoable() {
		return doable;
	}

	public void print() {
		double[] bestRow = bestRow();
		System.out.println("| " + u.getName() + " (User) ↓ | " + t.getName() + " (Task) → |");
		System.out.println("_____________________________________");
		String columnsString = "|    |";
		for (String s : columnsT) {
			columnsString += " " + s + " |";
		}
		System.out.println(columnsString + "| bestVals");
		System.out.println("--------------------------------");
		int rowc = 0;
		for (MatrixField[] row : matrix) {
			String rowString = "| " + rowsU[rowc] + "|";
			for (MatrixField column : row) {
				rowString += " " + column.getVal() + " |";
			}
			System.out.println(rowString + "| " + bestRow[rowc]);
			System.out.println("--------------------------------");
			rowc++;
		}
		String bString = "| bestVals |";
		for (double b : bestColumn()) {
			bString += " " + b + "|";
		}
		System.out.println("--------------------------------");
		System.out.println(bString);
		System.out.println("_____________________________________");
		System.out.println("DOABLE: " + doable);
		if (mandatoryViolations.size() == 0) {
			System.out.println("NO VIOLATIONS");
		} else {
			System.out.println("VIOLATIONS");
			for (String s : mandatoryViolations) {
				System.out.println(s);
			}
		}
		System.out.println("RESULT FOR ROW: " + calcMatchRow());
		System.out.println("RESULT FOR COLUMN: " + calcMatchColumn());
		System.out.println("_____________________________________");
	}

}
