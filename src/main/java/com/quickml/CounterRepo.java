package com.quickml;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.Counter;

@Repository
public interface CounterRepo extends MongoRepository<Counter, String> {
}