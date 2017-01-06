package crac.utility;

public class SimpleLogger {
	
	private String string = "";
	
	private static SimpleLogger instance = new SimpleLogger();
	
	public static void setString(String string){
		instance.string = string;
	}
	
	public static String getString(){
		return instance.string;
	}

}
