package com.quickml.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.TeachingSchool;

@Repository
public interface TeachingSchoolRepository extends MongoRepository<TeachingSchool, String> {
    // find by name
    TeachingSchool findByName(String name);
}