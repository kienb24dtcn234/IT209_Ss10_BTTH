package com.ptit.student.repository;

import com.ptit.student.entity.Gender;
import com.ptit.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentCode(String studentCode);

    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);

    @Query("""
            SELECT s FROM Student s
            WHERE (:keyword IS NULL
                   OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR s.phone LIKE CONCAT('%', :keyword, '%'))
              AND (:gender IS NULL OR s.gender = :gender)
              AND (:minGpa IS NULL OR s.gpa >= :minGpa)
              AND (:maxGpa IS NULL OR s.gpa <= :maxGpa)
            """)
    Page<Student> search(@Param("keyword") String keyword,
                         @Param("gender") Gender gender,
                         @Param("minGpa") Double minGpa,
                         @Param("maxGpa") Double maxGpa,
                         Pageable pageable);
}
