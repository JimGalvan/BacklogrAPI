package com.backlogr.domain.mapper;

import com.backlogr.domain.entities.user.User;
import com.backlogr.domain.dto.user.UserResponse;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserResponse toResponse(User user);
}
