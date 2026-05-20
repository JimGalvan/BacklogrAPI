package com.backlogr.mapper;

import com.backlogr.domain.user.User;
import com.backlogr.dto.user.UserResponse;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserResponse toResponse(User user);
}
