package com.quickml.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.Counter;

@Repository
public interface CounterRepository extends MongoRepository<Counter, String> {
}