package com.learning_forum.mapper;

import com.learning_forum.dto.request.UserCreationRequest;
import com.learning_forum.dto.request.UserUpdateRequest;
import com.learning_forum.dto.respone.UserResponse;
import com.learning_forum.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
    UserResponse toUserResponse(User user);
}
