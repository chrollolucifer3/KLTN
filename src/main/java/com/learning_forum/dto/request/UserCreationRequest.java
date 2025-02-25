package com.learning_forum.dto.request;

import lombok.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 6, max = 20, message = "Username phải có chiều dài từ 6 đến 20 ký tự")
    String username;

    @NotBlank(message = "Password không được để trống")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ. Vui lòng nhập email đúng định dạng (vd: example@domain.com)")
    String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^0\\d{9}$",
            message = "Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại bao gồm 10 số và bắt đầu từ 0"
    )
    String phone;

    @NotBlank(message = "Role không được để trống")
    String role;

    @NotBlank(message = "Full name không được để trống")
    String fullName;

    LocalDate dob;
}
