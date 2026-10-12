package com.ptit.student.service;

import com.ptit.student.dto.PageResponse;
import com.ptit.student.dto.StudentRequest;
import com.ptit.student.entity.Gender;
import com.ptit.student.entity.Student;
import com.ptit.student.exception.DuplicateException;
import com.ptit.student.exception.NotFoundException;
import com.ptit.student.repository.StudentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class StudentService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "studentCode", "fullName", "dateOfBirth", "gender", "gpa");

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PageResponse<Student> search(String keyword, Gender gender, Double minGpa, Double maxGpa,
                                        int page, int size, String sortBy, String sortDir) {
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("sortBy không hợp lệ: " + sortBy);
        }
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return PageResponse.from(repository.search(kw, gender, minGpa, maxGpa, pageable));
    }

    @Transactional(readOnly = true)
    public Student findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sinh viên có id = " + id));
    }

    @Transactional
    public Student create(StudentRequest request) {
        if (repository.existsByStudentCode(request.studentCode())) {
            throw new DuplicateException("Mã sinh viên đã tồn tại: " + request.studentCode());
        }
        Student student = new Student();
        apply(student, request);
        return repository.save(student);
    }

    @Transactional
    public Student update(Long id, StudentRequest request) {
        Student student = findById(id);
        if (repository.existsByStudentCodeAndIdNot(request.studentCode(), id)) {
            throw new DuplicateException("Mã sinh viên đã tồn tại: " + request.studentCode());
        }
        apply(student, request);
        return repository.save(student);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private void apply(Student student, StudentRequest request) {
        student.setStudentCode(request.studentCode().trim());
        student.setFullName(request.fullName().trim());
        student.setDateOfBirth(request.dateOfBirth());
        student.setGender(request.gender());
        student.setPhone(request.phone());
        student.setGpa(request.gpa());
    }
}
