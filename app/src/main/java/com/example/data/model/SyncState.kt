package com.example.data.model

@kotlinx.serialization.Serializable
sealed class SyncState {
    @kotlinx.serialization.Serializable
    object Synced : SyncState()
    
    @kotlinx.serialization.Serializable
    object Syncing : SyncState()
    
    @kotlinx.serialization.Serializable
    object PendingLocalChanges : SyncState()
    
    @kotlinx.serialization.Serializable
    object Offline : SyncState()
    
    @kotlinx.serialization.Serializable
    object Error : SyncState()
}
