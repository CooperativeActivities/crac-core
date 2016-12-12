package crac.competenceGraph;

import java.util.ArrayList;
import java.util.Set;

import crac.models.CracUser;
import crac.models.Task;
import crac.relationmodels.CompetenceTaskRel;
import crac.relationmodels.UserCompetenceRel;

public class CompetenceCollectionMatrix {
	
	private double[][] matrix;
	private String[] rowsU;
	private String[] columnsT;
	private CracUser u;
	private Task t;
	
	public CompetenceCollectionMatrix(CracUser u, Task t){
		this.u = u;
		this.t= t;
		
		Set<UserCompetenceRel> userComps = u.getCompetenceRelationships();
		Set<CompetenceTaskRel> taskComps = t.getMappedCompetences();
				
		matrix = new double[userComps.size()][taskComps.size()];
		rowsU = new String[userComps.size()];
		columnsT = new String[taskComps.size()];
		
		int uCount = 0;
		for(UserCompetenceRel ucr : userComps){
			rowsU[uCount] = ucr.getCompetence().getName();
			int tCount = 0;
			for(CompetenceTaskRel ctr : taskComps){
				columnsT[tCount] = ctr.getCompetence().getName();
				matrix[uCount][tCount] = CompetenceStorage.getCompetenceSimilarity(ucr.getCompetence(), ctr.getCompetence());
				tCount++;
			}
			uCount ++;
		}
	}
	
	public void print(){
		System.out.println("| "+u.getName()+" (User) ↓ | "+t.getName()+" (Task) → |");
		System.out.println("_____________________________________");
		String columnsString = "|    |";
		for(String s : columnsT){
			columnsString += " "+s+"|";
		}
		System.out.println(columnsString);
		System.out.println("--------------------------------");
		int rowc = 0;
		for(double[] row : matrix){
			String rowString = "| "+rowsU[rowc]+ "|";
			for(double column : row){
				rowString += " "+column+" |";
			}
			System.out.println(rowString);
			System.out.println("--------------------------------");
			rowc++;
		}
		System.out.println("_____________________________________");
	}

}
