package com.learning_forum.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
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

    @Email(message = "Email không hợp lệ. Vui lòng nhập email đúng định dạng (vd: example@domain.com)")
    String email;

    @Pattern(
            regexp = "^0\\d{9}$",
            message = "Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại bao gồm 10 số và bắt đầu từ 0"
    )
    String phone;

    @Pattern(regexp = "^[^0-9]*$", message = "Tên không được chứa số")
    String fullName;

    @Past(message = "Ngày sinh phải nhỏ hơn ngày hiện tại")
    LocalDate dob;
}
