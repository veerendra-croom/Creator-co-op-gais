package com.example.data.security

import com.example.data.model.WorkspaceMember
import kotlinx.serialization.Serializable

/**
 * Granular Role-Based Access Control (RBAC) Guard for Creator Co-Op workspaces.
 * Enforces strict capability matrices across Owners, Leads, Editors, Motion Designers,
 * Audio Engineers, and Guest Reviewers.
 */
object WorkspacePermissionGuard {

    enum class WorkspaceAction {
        MODIFY_WORKSPACE_METADATA,
        INVITE_COLLABORATORS,
        REMOVE_COLLABORATORS,
        AMEND_LEGAL_AGREEMENT,
        SIGN_LEGAL_AGREEMENT,
        CREATE_PRODUCTION_TASK,
        EDIT_TASK_CONTENT,
        PROMOTE_TO_PUBLISH_LANE,
        SUBMIT_DELIVERABLE,
        APPROVE_DELIVERABLE,
        ACCESS_ESCROW_FINANCIALS,
        EXPORT_MASTER_ASSETS,
        ACCESS_ADMIN_AUDIT_LOGS
    }

    @Serializable
    data class PermissionEvaluation(
        val isAllowed: Boolean,
        val action: String,
        val userRole: String,
        val rejectionReason: String? = null
    )

    /**
     * Evaluates whether a workspace member has authority to perform a specific action.
     */
    fun evaluate(
        member: WorkspaceMember?,
        action: WorkspaceAction,
        isCreatorOrOwner: Boolean = false
    ): PermissionEvaluation {
        if (member == null) {
            return PermissionEvaluation(
                isAllowed = false,
                action = action.name,
                userRole = "ANONYMOUS",
                rejectionReason = "User is not an authenticated member of this workspace."
            )
        }

        val role = member.assignedRoleTitle.trim().lowercase()
        val isLeadOrOwner = isCreatorOrOwner || role in listOf("owner", "lead creator", "founder", "head", "director")
        val isCoreContributor = isLeadOrOwner || role in listOf("editor", "lead editor", "animator", "motion designer", "sound engineer", "scriptwriter")

        val isAllowed = when (action) {
            WorkspaceAction.MODIFY_WORKSPACE_METADATA -> isLeadOrOwner
            WorkspaceAction.INVITE_COLLABORATORS -> isLeadOrOwner || isCoreContributor
            WorkspaceAction.REMOVE_COLLABORATORS -> isLeadOrOwner
            WorkspaceAction.AMEND_LEGAL_AGREEMENT -> isLeadOrOwner
            WorkspaceAction.SIGN_LEGAL_AGREEMENT -> true // Any registered member can sign their own agreement
            WorkspaceAction.CREATE_PRODUCTION_TASK -> isCoreContributor || member.canModifyProduction
            WorkspaceAction.EDIT_TASK_CONTENT -> isCoreContributor || member.canModifyProduction
            WorkspaceAction.PROMOTE_TO_PUBLISH_LANE -> isLeadOrOwner || (isCoreContributor && member.canModifyProduction)
            WorkspaceAction.SUBMIT_DELIVERABLE -> isCoreContributor || member.canModifyProduction
            WorkspaceAction.APPROVE_DELIVERABLE -> isLeadOrOwner
            WorkspaceAction.ACCESS_ESCROW_FINANCIALS -> isLeadOrOwner || isCoreContributor
            WorkspaceAction.EXPORT_MASTER_ASSETS -> isCoreContributor || member.canModifyProduction
            WorkspaceAction.ACCESS_ADMIN_AUDIT_LOGS -> isLeadOrOwner
        }

        val reason = if (!isAllowed) {
            "Action '${action.name}' requires elevated privileges. Current role: '${member.assignedRoleTitle}'."
        } else null

        return PermissionEvaluation(
            isAllowed = isAllowed,
            action = action.name,
            userRole = member.assignedRoleTitle,
            rejectionReason = reason
        )
    }
}
