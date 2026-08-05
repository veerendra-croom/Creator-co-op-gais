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
    val stateScope: String = "PRODUCTION_READY", // PRODUCTION_READY, ROUGH_SANDBOX
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
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE"),
    IDEAS("IDEAS"),
    REVIEW("REVIEW"),
    PUBLISH("PUBLISH");

    companion object {
        fun fromString(lane: String?): KanbanLane {
            return values().find { it.value.equals(lane, ignoreCase = true) } ?: TODO
        }
    }
}
