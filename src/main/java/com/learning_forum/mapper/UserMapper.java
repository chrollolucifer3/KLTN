package com.learning_forum.mapper;

import com.learning_forum.dto.request.UserCreationRequest;
import com.learning_forum.dto.request.UserUpdateRequest;
import com.learning_forum.dto.respone.UserResponse;
import com.learning_forum.dto.respone.UserResponseForAdmin;
import com.learning_forum.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
//    void updatePassword(@MappingTarget User user, UserUpdatePasswordRequest request);

    @Mapping(source = "role", target = "role")
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(source = "avatarUrl", target = "avatarUrl")
    UserResponseForAdmin toUserResponseForAdmin(User user);

    UserResponse toUserResponseForUser(User user);

    UserResponse toUserResponse(User user);
}
