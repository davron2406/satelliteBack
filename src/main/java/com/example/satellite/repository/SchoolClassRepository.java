package com.example.satellite.repository;


import com.example.satellite.entity.SchoolClass;
import com.example.satellite.payload.OptionInterface;
import com.example.satellite.payload.StudentResultView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {
    Optional<SchoolClass> findByNameIgnoreCase(String name);

    @Query(value = """
    select c.id as id, c.name as name
    from public.school_classes c
    join class_teachers sct on sct.class_id = c.id
    where sct.user_id = :teacherId
      and (:name is null or c.name ILIKE ('%' || :name || '%'))
    order by c.name asc
  """, nativeQuery = true)
    List<OptionInterface> findByTeacherAndName(@Param("teacherId") UUID teacherId,
                                                @Param("name") String name);

    @Query(value = """
    select c.id as id, c.name as name
    from public.school_classes c
    where (:name is null or c.name ILIKE ('%' || :name || '%'))
    order by c.name asc
  """, nativeQuery = true)
    List<OptionInterface> findAllByName(@Param("name") String name);


    // if you mapped many-to-many as "teachers"
    List<SchoolClass> findByTeachers_Id(UUID teacherId);


    @Query("""
        select
            s.id as studentId,
            s.firstName as firstName,
            s.lastName as lastName,

            r.id as resultId,
            r.correctCount as correctCount,
            r.totalCount as totalQuestions,
            r.correctCount as score,
            r.submittedAt as startedAt,
            r.submittedAt as finishedAt

        from SchoolClass c
            join c.students s
            left join PracticeTestResult r
                   on r.userId = s.id
                  and r.templateId = :templateId

        where c.id = :classId
        order by s.lastName, s.firstName
    """)
    List<StudentResultView> findClassStudentResults(
            @Param("classId") UUID classId,
            @Param("templateId") UUID templateId
    );
}
