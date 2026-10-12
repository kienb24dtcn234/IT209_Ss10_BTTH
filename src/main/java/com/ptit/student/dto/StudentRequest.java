package com.ptit.student.dto;

import com.ptit.student.entity.Gender;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentRequest(
        @NotBlank(message = "Mã sinh viên không được để trống")
        @Size(max = 20, message = "Mã sinh viên tối đa 20 ký tự")
        String studentCode,

        @NotBlank(message = "Tên sinh viên không được để trống")
        @Size(max = 100, message = "Tên sinh viên tối đa 100 ký tự")
        String fullName,

        @NotNull(message = "Ngày sinh không được để trống")
        @Past(message = "Ngày sinh phải là ngày trong quá khứ")
        LocalDate dateOfBirth,

        @NotNull(message = "Giới tính phải là MALE, FEMALE hoặc OTHER")
        Gender gender,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 0")
        String phone,

        @NotNull(message = "GPA không được để trống")
        @DecimalMin(value = "0.0", message = "GPA phải từ 0.0 đến 4.0")
        @DecimalMax(value = "4.0", message = "GPA phải từ 0.0 đến 4.0")
        Double gpa
) {
}
