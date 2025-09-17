package com.example.satellite.repository;


import com.example.satellite.entity.PracticeTemplate;
import com.example.satellite.payload.OptionInterface;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PracticeTemplateRepository extends JpaRepository<PracticeTemplate, UUID> {
    List<PracticeTemplate> findByTitleContainingIgnoreCaseOrderByTitleAsc(String name);

    @Query("""
    select t.id as id, t.title as name
    from PracticeTemplate t
    where lower(t.title) like lower(concat('%', :name, '%'))
    order by t.title asc
  """)
    List<OptionInterface> findByNameLike(@Param("name") String name);
}
