package com.learning_forum.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ. Vui lòng nhập email đúng định dạng (vd: example@domain.com)")
    String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(
            regexp = "^0\\d{9}$",
            message = "Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại bao gồm 10 số và bắt đầu từ 0"
    )
    String phone;

    @NotBlank(message = "Tên không được để trống")
    String fullName;

    LocalDate dob;

}
