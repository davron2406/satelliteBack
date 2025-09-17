package com.example.satellite.repository;




import com.example.satellite.entity.Question;
import com.example.satellite.entity.enums.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByTopicIdAndDifficulty(UUID topicId, Difficulty difficulty);

    @Query("""
         select q from Question q
         left join fetch q.answers a
         where q.id = :id
      """)
    Optional<Question> findWithAnswers(@Param("id") UUID id);

    List<Question> findByIdIn(Collection<UUID> ids);

    @Query("""
            select distinct q from Question q
            left join fetch q.answers a
            where q.id in :ids
           """)
    List<Question> findAllWithAnswersByIdIn(@Param("ids") Collection<UUID> ids);
}

