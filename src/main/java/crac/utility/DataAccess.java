package crac.utility;

import java.util.HashMap;

import org.springframework.data.repository.CrudRepository;

public class DataAccess {

	//Access to Repositories
	
	private HashMap<Class<?>, CrudRepository<?, ?>> daos = new HashMap<>();
	
	public static <T extends CrudRepository<?, ?>> void addRepo(T obj){
		instance.daos.put(obj.getClass().getInterfaces()[0], obj);
	}
	
	public static <T extends CrudRepository<?, ?>> T getRepo(Class<T> type){
		return type.cast(instance.daos.get(type));
	}
	
	//Access to ElasticSearch

	private HashMap<Class<?>, ElasticConnector<?>> connectors = new HashMap<>();
	
	public static <T> void addConnector(ElasticConnector<T> obj, Class<T> t){
		instance.connectors.put(t, obj);
	}
	
	public static <T> ElasticConnector<T> getConnector(Class<T> type){
		return (ElasticConnector<T>) instance.connectors.get(type);
	}
	
	//Instance of the class
	
	private static DataAccess instance = new DataAccess();

}
