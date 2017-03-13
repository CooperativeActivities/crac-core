package crac.models.storage;

import java.util.ArrayList;
import java.util.Set;

import crac.decider.core.FilterConfiguration;
import crac.decider.workers.config.GlobalMatrixFilterConfig;
import crac.models.db.entities.CracUser;
import crac.models.db.entities.Task;
import crac.models.db.relation.CompetenceTaskRel;
import crac.models.db.relation.UserCompetenceRel;
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

	public CompetenceCollectionMatrix(CracUser u, Task t, FilterConfiguration m) {
		this.u = u;
		this.t = t;
		this.doable = true;

		this.userComps = u.getCompetenceRelationships();
		this.taskComps = t.getMappedCompetences();

		matrix = new MatrixField[userComps.size()][taskComps.size()];
		rowsU = new String[userComps.size()];
		columnsT = new String[taskComps.size()];

		buildMatrix();
		markMandatoryViolation();
		applyFilters(m);

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

	private void applyFilters(FilterConfiguration m) {

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
