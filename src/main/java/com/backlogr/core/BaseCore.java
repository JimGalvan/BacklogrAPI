package com.backlogr.core;

import com.backlogr.repository.workspace.WorkspaceMemberRepository;
import com.backlogr.repository.workspace.WorkspaceRepository;
import com.backlogr.common.Result;
import jakarta.inject.Inject;

import java.util.UUID;

public abstract class BaseCore {

    @Inject
    protected WorkspaceRepository workspaceRepository;

    @Inject
    protected WorkspaceMemberRepository workspaceMemberRepository;

    protected Result<Void> requireWorkspaceMember(UUID userId, UUID workspaceId) {
        if (workspaceRepository.findByIdOptional(workspaceId).isEmpty()) {
            return Result.notFound("Workspace not found.");
        }
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            return Result.forbidden("You are not a member of this workspace.");
        }
        return Result.ok(null);
    }
}
