package com.backlogr.core.workspace;

import com.backlogr.core.BaseCore;
import com.backlogr.domain.workspace.Workspace;
import com.backlogr.domain.workspace.WorkspaceMember;
import com.backlogr.dto.workspace.CreateWorkspaceRequest;
import com.backlogr.dto.workspace.InviteMemberRequest;
import com.backlogr.dto.workspace.WorkspaceMemberResponse;
import com.backlogr.dto.workspace.WorkspaceResponse;
import com.backlogr.mapper.WorkspaceMapper;
import com.backlogr.repository.user.UserRepository;
import com.backlogr.shared.Result;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class WorkspaceCore extends BaseCore {

    @Inject
    UserRepository userRepository;

    @Inject
    WorkspaceMapper workspaceMapper;

    @Transactional
    public Result<WorkspaceResponse> createWorkspace(UUID authenticatedUserId, CreateWorkspaceRequest request) {
        if (!authenticatedUserId.equals(request.ownerId())) {
            return Result.forbidden("You can only create workspaces for yourself.");
        }
        if (userRepository.findById(request.ownerId()) == null) {
            return Result.notFound("User not found.");
        }

        Workspace workspace = new Workspace();
        workspace.name = request.name();
        workspace.ownerId = request.ownerId();
        workspaceRepository.persist(workspace);

        WorkspaceMember member = new WorkspaceMember();
        member.workspaceId = workspace.id;
        member.userId = request.ownerId();
        workspaceMemberRepository.persist(member);

        return Result.created(workspaceMapper.toResponse(workspace));
    }

    public Result<WorkspaceResponse> getWorkspace(UUID workspaceId) {
        return workspaceRepository.findByIdOptional(workspaceId)
                .map(workspaceMapper::toResponse)
                .map(Result::ok)
                .orElse(Result.notFound("Workspace not found."));
    }

    public Result<List<WorkspaceMemberResponse>> getMembers(UUID workspaceId) {
        Result<Workspace> workspaceResult = resolveWorkspace(workspaceId);
        if (!workspaceResult.isSuccess()) return workspaceResult.asError();
        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceId);
        return Result.ok(workspaceMapper.toMemberResponseList(members));
    }

    @Transactional
    public Result<WorkspaceMemberResponse> inviteMember(UUID authenticatedUserId, UUID workspaceId, InviteMemberRequest request) {
        Result<Workspace> workspaceResult = resolveWorkspace(workspaceId);
        if (!workspaceResult.isSuccess()) return workspaceResult.asError();
        Workspace workspace = workspaceResult.getValue();
        if (!authenticatedUserId.equals(workspace.ownerId)) {
            return Result.forbidden("Only the workspace owner can invite members.");
        }

        var invitee = userRepository.findByEmail(request.email()).orElse(null);
        if (invitee == null) {
            return Result.notFound("No account found for " + request.email() + ".");
        }
        if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, invitee.id)) {
            return Result.conflict("User is already a member of this workspace.");
        }

        WorkspaceMember member = new WorkspaceMember();
        member.workspaceId = workspaceId;
        member.userId = invitee.id;
        workspaceMemberRepository.persist(member);

        return Result.created(workspaceMapper.toMemberResponse(member));
    }

    @Transactional
    public Result<Void> removeMember(UUID authenticatedUserId, UUID workspaceId, UUID targetUserId) {
        Result<Workspace> workspaceResult = resolveWorkspace(workspaceId);
        if (!workspaceResult.isSuccess()) return workspaceResult.asError();
        Workspace workspace = workspaceResult.getValue();
        if (!authenticatedUserId.equals(workspace.ownerId)) {
            return Result.forbidden("Only the workspace owner can remove members.");
        }
        if (targetUserId.equals(workspace.ownerId)) {
            return Result.badRequest("The workspace owner cannot be removed.");
        }

        WorkspaceMember member = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, targetUserId).orElse(null);
        if (member == null) {
            return Result.notFound("User is not a member of this workspace.");
        }

        workspaceMemberRepository.delete(member);
        return Result.ok(null);
    }

    public Result<List<WorkspaceResponse>> getUserWorkspaces(UUID userId) {
        if (userRepository.findById(userId) == null) {
            return Result.notFound("User not found.");
        }
        List<UUID> workspaceIds = workspaceMemberRepository.findWorkspaceIdsByUserId(userId);
        List<Workspace> workspaces = workspaceRepository.findAllByIds(workspaceIds);
        return Result.ok(workspaceMapper.toResponseList(workspaces));
    }

    private Result<Workspace> resolveWorkspace(UUID workspaceId) {
        return workspaceRepository.findByIdOptional(workspaceId)
                .map(Result::ok)
                .orElseGet(() -> Result.notFound("Workspace not found."));
    }
}
