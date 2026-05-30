package com.backlogr.domain.mapper;

import com.backlogr.domain.entities.workspace.Workspace;
import com.backlogr.domain.entities.workspace.WorkspaceMember;
import com.backlogr.domain.dto.workspace.WorkspaceMemberResponse;
import com.backlogr.domain.dto.workspace.WorkspaceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface WorkspaceMapper {

    WorkspaceResponse toResponse(Workspace workspace);

    List<WorkspaceResponse> toResponseList(List<Workspace> workspaces);

    @Mapping(target = "email",    source = "user.email")
    @Mapping(target = "name",     source = "user.name")
    @Mapping(target = "joinedAt", source = "createdAt")
    WorkspaceMemberResponse toMemberResponse(WorkspaceMember member);

    List<WorkspaceMemberResponse> toMemberResponseList(List<WorkspaceMember> members);
}
