package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "production_tasks", indices = [Index(value = ["workspaceId"])])
data class ProductionTask(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val creatorId: String,
    val title: String,
    val contentBody: String = "",
    val stateScope: String = "PRODUCTION_READY", // PRODUCTION_READY, PRIVATE_DRAFT
    val kanbanLane: String = "TODO",
    val priority: String = "MEDIUM",
    val deadline: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "deliverables", indices = [Index(value = ["workspaceId", "taskId"])])
data class Deliverable(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val taskId: String,
    val submitterId: String = "",
    val assetId: String = "",
    val versionNotes: String = "",
    val linkedTaskId: String = "",
    val title: String = "",
    val assetUrl: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val reviewFeedback: String? = null,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "task_templates")
data class TaskTemplate(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val templateName: String = "",
    val tasksJson: String = "[]",
    val createdBy: String = "",
    val name: String = "", // Added to be safe
    val structureJson: String = "", // Added to be safe
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class KanbanLane(val value: String) {
    TODO("TODO"),
    IDEAS("IDEAS"),
    RESEARCH("RESEARCH"),
    SCRIPT("SCRIPT"),
    RECORD("RECORD"),
    EDIT("EDIT"),
    REVIEW("REVIEW"),
    PUBLISH("PUBLISH"),
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE");

    companion object {
        fun fromString(lane: String?): KanbanLane {
            if (lane.isNullOrBlank()) return IDEAS
            val normalized = lane.trim().uppercase()
            return when (normalized) {
                "TODO", "IDEAS", "IDEATION" -> IDEAS
                "RESEARCH" -> RESEARCH
                "SCRIPT", "SCRIPTING" -> SCRIPT
                "RECORD", "FILMING", "RECORDING", "SHOOT" -> RECORD
                "EDIT", "EDITING", "IN_PROGRESS" -> EDIT
                "REVIEW", "IN_REVIEW" -> REVIEW
                "PUBLISH", "PUBLISHED", "DONE", "COMPLETED" -> PUBLISH
                else -> values().find { it.value.equals(normalized, ignoreCase = true) } ?: IDEAS
            }
        }
    }
}
