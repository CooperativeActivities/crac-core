package crac.utility;

import java.util.HashMap;

import org.springframework.data.repository.CrudRepository;

public class DataAccess {

	private HashMap<Class<?>, CrudRepository<?, ?>> daos = new HashMap<>();
	
	public static <T extends CrudRepository<?, ?>> void addRepo(T obj){
		instance.daos.put(obj.getClass().getInterfaces()[0], obj);
	}
	
	public static <T extends CrudRepository<?, ?>> T getRepo(Class<T> type){
		return type.cast(instance.daos.get(type));
	}
	
	private static DataAccess instance = new DataAccess();

}
