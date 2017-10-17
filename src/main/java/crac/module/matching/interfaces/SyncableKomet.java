package crac.module.matching.interfaces;

import java.util.Map;

import org.springframework.data.repository.CrudRepository;

import crac.exception.KometMappingException;

public interface SyncableKomet {

	public SyncableCrac map(Map<Class<?>, CrudRepository<?, ?>> map)  throws KometMappingException;

	public boolean isValid();

	public int getUid();

}
