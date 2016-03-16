package com.models;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jersey.representations.HelloWorld;

@Transactional
public interface HelloWorldDAO extends CrudRepository<HelloWorld, Long> {


}
