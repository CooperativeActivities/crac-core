package crac.module.matching.interfaces;

import java.util.Map;

import org.springframework.data.repository.CrudRepository;

public interface SyncableKomet {

	public SyncableCrac map(Map<Class<?>, CrudRepository<?, ?>> map);

	public boolean isValid();

	public int getUid();

}
