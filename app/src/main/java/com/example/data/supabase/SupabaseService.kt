package com.example.data.supabase

import com.example.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SupabaseService {

    // --- USERS TABLE ---
    @GET("rest/v1/users")
    suspend fun getUsers(
        @Query("id") idFilter: String? = null
    ): Response<List<User>>

    @POST("rest/v1/users")
    suspend fun upsertUser(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body user: User
    ): Response<List<User>>


    // --- POSTS TABLE ---
    @GET("rest/v1/posts")
    suspend fun getPosts(
        @Query("select") select: String = "*",
        @Query("order") order: String = "timestamp.desc"
    ): Response<List<Post>>

    @POST("rest/v1/posts")
    suspend fun upsertPost(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body post: Post
    ): Response<List<Post>>


    // --- COMMENTS TABLE ---
    @GET("rest/v1/comments")
    suspend fun getComments(
        @Query("postId") postId: String? = null
    ): Response<List<Comment>>

    @POST("rest/v1/comments")
    suspend fun insertComment(
        @Body comment: Comment
    ): Response<List<Comment>>


    // --- PROJECTS TABLE ---
    @GET("rest/v1/projects")
    suspend fun getProjects(): Response<List<Project>>

    @POST("rest/v1/projects")
    suspend fun upsertProject(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body project: Project
    ): Response<List<Project>>


    // --- PITCHES TABLE ---
    @GET("rest/v1/pitches")
    suspend fun getPitches(
        @Query("projectId") projectId: String? = null,
        @Query("applicantId") applicantId: String? = null
    ): Response<List<Pitch>>

    @POST("rest/v1/pitches")
    suspend fun upsertPitch(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body pitch: Pitch
    ): Response<List<Pitch>>


    // --- MESSAGES TABLE ---
    @GET("rest/v1/messages")
    suspend fun getMessages(
        @Query("workspaceId") workspaceId: String? = null,
        @Query("channel") channel: String? = null
    ): Response<List<Message>>

    @POST("rest/v1/messages")
    suspend fun insertMessage(
        @Body message: Message
    ): Response<List<Message>>


    // --- CONTRACTS TABLE ---
    @GET("rest/v1/contracts")
    suspend fun getContracts(): Response<List<Contract>>

    @POST("rest/v1/contracts")
    suspend fun upsertContract(
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body contract: Contract
    ): Response<List<Contract>>
}
