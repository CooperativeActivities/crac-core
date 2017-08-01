package crac.module.matching.helpers;

import java.util.ArrayList;
import java.util.Set;

import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
import crac.module.matching.configuration.MatchingConfiguration;
import crac.module.storage.CompetenceStorage;

public class CompetenceCollectionMatrix {

	private CompetenceStorage cs;
	private MatrixField[][] matrix;
	private String[] rowsU;
	private String[] columnsT;
	private CracUser u;
	private Task t;
	private boolean doable;
	private ArrayList<String> mandatoryViolations = new ArrayList<>();
	private Set<UserCompetenceRel> userComps;
	private Set<CompetenceTaskRel> taskComps;

	private boolean nullTask;
	private boolean nullUser;

	public CompetenceCollectionMatrix(CracUser u, Task t, MatchingConfiguration m, CompetenceStorage cs) {
		this.cs = cs;
		this.u = u;
		this.t = t;
		this.doable = true;
		this.nullTask = false;
		this.nullUser = false;

		this.taskComps = t.getMappedCompetences();
		this.userComps = u.getCompetenceRelationships();

		if (this.taskComps == null) {
			this.nullTask = true;
		} else if (this.taskComps.isEmpty()) {
			this.nullTask = true;
		}

		if (this.userComps == null) {
			this.nullUser = true;
		} else if (this.userComps.isEmpty()) {
			this.nullUser = true;
		}

		if (!this.nullTask && !this.nullUser) {
			matrix = new MatrixField[userComps.size()][taskComps.size()];
			rowsU = new String[userComps.size()];
			columnsT = new String[taskComps.size()];

			buildMatrix();
			markMandatoryViolation();
			applyFilters(m);
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
						cs.getCompetenceSimilarity(ucr.getCompetence(), ctr.getCompetence()));
				tCount++;
			}
			uCount++;
		}
	}

	private void applyFilters(MatchingConfiguration m) {

		for (MatrixField[] row : matrix) {

			for (MatrixField field : row) {
				m.applyFilters(field);
			}
		}
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
		if (this.nullTask && this.nullUser) {
			return 0.5;
		} else if (this.nullTask && !this.nullUser) {
			return 0.5;
		}  else if (!this.nullTask && this.nullUser) {
			return 0;
		} else {
			return calcMatchColumn();
			// return calcMatchRow();
		}
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
		if (this.nullTask || this.nullUser) {
			System.out.println("_____________________________________");
			System.out.println("Task " + t.getName() + " or User " + u.getName() + " contain empty competences!");
			System.out.println("_____________________________________");
		} else {
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
}
