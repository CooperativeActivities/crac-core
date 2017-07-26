package crac.models.utility;

import java.util.Random;

public class RandomUtility {
	
	public static String randomString(final int length) {
	    Random r = new Random(); // perhaps make it a class variable so you don't make a new one every time
	    StringBuilder sb = new StringBuilder();
	    for(int i = 0; i < length; i++) {
	        int c = r.nextInt(9);
	        sb.append(c);
	    }
	    return sb.toString();
	}

}
