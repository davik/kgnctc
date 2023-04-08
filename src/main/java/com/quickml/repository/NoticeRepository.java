package com.quickml.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.Notice;

@Repository
public interface NoticeRepository extends MongoRepository<Notice, String> {
    List<Notice> findByCourseAndSession(String course, String session);

}
