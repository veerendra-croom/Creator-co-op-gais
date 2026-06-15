package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<User?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY (upvotes - downvotes) DESC, timestamp DESC")
    fun getAllPostsSortedByTrending(): Flow<List<Post>>

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsSortedByNew(): Flow<List<Post>>

    @Query("SELECT * FROM posts ORDER BY upvotes DESC")
    fun getAllPostsSortedByTop(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE spaceName = :spaceName ORDER BY timestamp DESC")
    fun getPostsBySpace(spaceName: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun getPostById(id: String): Flow<Post?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: String): Flow<Project?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<Project>)
}

@Dao
interface PitchDao {
    @Query("SELECT * FROM pitches WHERE projectId = :projectId ORDER BY submittedAt DESC")
    fun getPitchesForProject(projectId: String): Flow<List<Pitch>>

    @Query("SELECT * FROM pitches WHERE applicantId = :applicantId ORDER BY submittedAt DESC")
    fun getPitchesByApplicant(applicantId: String): Flow<List<Pitch>>

    @Query("SELECT * FROM pitches WHERE id = :id")
    fun getPitchById(id: String): Flow<Pitch?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPitch(pitch: Pitch)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE workspaceId = :workspaceId AND channel = :channel ORDER BY timestamp ASC")
    fun getMessagesForWorkspaceChannel(workspaceId: String, channel: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
}

@Dao
interface ContractDao {
    @Query("SELECT * FROM contracts WHERE projectId = :projectId")
    fun getContractByProject(projectId: String): Flow<Contract?>

    @Query("SELECT * FROM contracts ORDER BY signedAtMilli DESC")
    fun getAllContracts(): Flow<List<Contract>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: Contract)
}
