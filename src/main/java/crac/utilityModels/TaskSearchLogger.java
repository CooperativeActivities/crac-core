package crac.utilityModels;

import java.util.ArrayList;
import java.util.HashMap;

import crac.notifier.NotificationDistributor;

public class TaskSearchLogger {
	
	private String titleTask;
	
	private String titlePerson;
	
	private ArrayList<String> titleColumns;
	
	private ArrayList<String> titleRows;
	
	private HashMap<Integer, String> values;
	
	private static TaskSearchLogger instance = new TaskSearchLogger();
	
	public static TaskSearchLogger getInstance(){
		return instance;
	}
	
	public static void emptyInstance(){
		instance.titleColumns = new ArrayList<String>();
		instance.titleRows = new ArrayList<String>();
		instance.values = new HashMap<Integer, String>();
		instance.titleColumns.clear();
		instance.titleRows.clear();
		instance.values.clear();
	}

	public void print(){
		
		System.out.println("///////////////////////////");
		System.out.println("////////BEGINNING//////////");
		System.out.println("///////////////////////////");
		
		System.out.println("TASK: "+instance.titleTask);
		System.out.println("USER: "+instance.titlePerson);
		String firstRow = "";
		for(String titleColumn : instance.titleColumns){
			firstRow += titleColumn + " | ";
		}
		System.out.println(firstRow);
		for(int i = 0; i  < instance.values.size(); i++){
			System.out.println(instance.titleRows.get(i) + " | " + instance.values.get(i));
		}
		System.out.println("///////////////////////////");
		System.out.println("//////////ENDING///////////");
		System.out.println("///////////////////////////");
		
	}

	public String getTitleTask() {
		return titleTask;
	}

	public void setTitleTask(String titleTask) {
		this.titleTask = titleTask;
	}

	public String getTitlePerson() {
		return titlePerson;
	}

	public void setTitlePerson(String titlePerson) {
		this.titlePerson = titlePerson;
	}

	public void addColumnTitle(String title) {
		this.titleColumns.add(title);
	}

	public void addRowTitle(String title) {
		this.titleRows.add(title);
	}

	public void addValue(double value, int row){
		if(this.values.containsKey(row)){
			String newVal = this.values.get(row) + value + " | " ;
			this.values.put(row, newVal);
		}else{
			this.values.put(row, "| "+ + value + " | ");
		}
	}
}
