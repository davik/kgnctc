package com.quickml.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.Notice;

@Repository
public interface NoticeRepository extends MongoRepository<Notice, String> {
    List<Notice> findByCourseAndSession(String course, String session);

    // find latest 5 notices by course and session
    @Query(value = "{ 'course' : ?0, 'session' : ?1 }", fields = "{ 'content' : 1, 'date' : 1 }", sort = "{ 'date' : -1 }")
    List<Notice> findTop5ByCourseAndSession(String course, String session);

    // Find latest 5 notices by course and session
    List<Notice> findTop5ByCourseAndSessionOrderByDateDesc(String course, String session);

}
