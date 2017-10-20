package crac.models.utility;

import java.util.HashMap;
import java.util.Map;

import crac.exception.InvalidParameterException;

/**
 * Helperclass that is used to configure target notification
 * @author David Hondl
 *
 */
public class NotificationConfiguration {
	
	Map<String, Object> configs;
	
	private NotificationConfiguration(){
		
	}
	
	public NotificationConfiguration put(String s, Object obj){
		configs.put(s, obj);
		return this;
	}
	
	public static NotificationConfiguration create(){
		NotificationConfiguration nc = new NotificationConfiguration();
		nc.configs = new HashMap<>();
		return nc;
	}
	
	public <T> T get (String s, Class<T> clazz){
		try{
			return clazz.cast(configs.get(s));
		}catch(ClassCastException ex){
			throw new InvalidParameterException();
		}	
	}
	
}
