package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import com.example.data.supabase.SupabaseSynchronizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class AppRepository(private val db: AppDatabase, private val context: android.content.Context? = null) {
    val userDao = db.userDao()
    val postDao = db.postDao()
    val commentDao = db.commentDao()
    val projectDao = db.projectDao()
    val pitchDao = db.pitchDao()
    val messageDao = db.messageDao()
    val contractDao = db.contractDao()

    // Flow streams
    val myUser: Flow<User?> = userDao.getUserById("me")
    val trendingPosts: Flow<List<Post>> = postDao.getAllPostsSortedByTrending()
    val newPosts: Flow<List<Post>> = postDao.getAllPostsSortedByNew()
    val topPosts: Flow<List<Post>> = postDao.getAllPostsSortedByTop()
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()
    val allContracts: Flow<List<Contract>> = contractDao.getAllContracts()

    fun getPostsBySpace(spaceName: String) = postDao.getPostsBySpace(spaceName)
    fun getPostById(id: String) = postDao.getPostById(id)
    fun getUserById(id: String): Flow<User?> = userDao.getUserById(id)
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    fun getCommentsForPost(postId: String) = commentDao.getCommentsForPost(postId)
    fun getProjectById(id: String) = projectDao.getProjectById(id)
    fun getPitchesForProject(projectId: String) = pitchDao.getPitchesForProject(projectId)
    fun getPitchesByApplicant(applicantId: String) = pitchDao.getPitchesByApplicant(applicantId)
    fun getMessagesForChannel(workspaceId: String, channel: String) = messageDao.getMessagesForWorkspaceChannel(workspaceId, channel)
    fun getContractByProject(projectId: String) = contractDao.getContractByProject(projectId)

    // Write actions
    suspend fun saveUser(user: User) {
        userDao.insertUser(user)
        context?.let { SupabaseSynchronizer.syncUpUser(it, user) }
    }

    suspend fun insertPost(post: Post) {
        postDao.insertPost(post)
        context?.let { SupabaseSynchronizer.syncUpPost(it, post) }
    }

    suspend fun updatePostVote(postId: String, voteType: String) {
        val currentPost = postDao.getPostById(postId).firstOrNull() ?: return
        var diffUp = 0
        var diffDown = 0

        // Handle vote logic changes
        val originalVote = currentPost.userVote
        val updatedPost = if (originalVote == voteType) {
            // Undo vote
            if (originalVote == "up") diffUp = -1
            if (originalVote == "down") diffDown = -1
            currentPost.copy(
                userVote = "none",
                upvotes = (currentPost.upvotes + diffUp).coerceAtLeast(0),
                downvotes = (currentPost.downvotes + diffDown).coerceAtLeast(0)
            )
        } else {
            // Undo old vote
            if (originalVote == "up") diffUp = -1
            if (originalVote == "down") diffDown = -1

            // Apply new vote
            if (voteType == "up") diffUp += 1
            if (voteType == "down") diffDown += 1

            currentPost.copy(
                userVote = voteType,
                upvotes = (currentPost.upvotes + diffUp).coerceAtLeast(0),
                downvotes = (currentPost.downvotes + diffDown).coerceAtLeast(0)
            )
        }
        postDao.insertPost(updatedPost)
        context?.let { SupabaseSynchronizer.syncUpPost(it, updatedPost) }
    }

    suspend fun insertComment(comment: Comment) {
        commentDao.insertComment(comment)
        context?.let { SupabaseSynchronizer.syncUpComment(it, comment) }
        
        // Increment post comment count
        val post = postDao.getPostById(comment.postId).firstOrNull()
        if (post != null) {
            val updatedPost = post.copy(commentCount = post.commentCount + 1)
            postDao.insertPost(updatedPost)
            context?.let { SupabaseSynchronizer.syncUpPost(it, updatedPost) }
        }
    }

    suspend fun insertProject(project: Project) {
        projectDao.insertProject(project)
        context?.let { SupabaseSynchronizer.syncUpProject(it, project) }
    }

    suspend fun insertPitch(pitch: Pitch) {
        pitchDao.insertPitch(pitch)
        context?.let { SupabaseSynchronizer.syncUpPitch(it, pitch) }
    }

    suspend fun updatePitchStatus(pitchId: String, status: String) {
        val pitch = pitchDao.getPitchById(pitchId).firstOrNull() ?: return
        val updatedPitch = pitch.copy(status = status)
        pitchDao.insertPitch(updatedPitch)
        context?.let { SupabaseSynchronizer.syncUpPitch(it, updatedPitch) }

        if (status == "ACCEPTED") {
            // Mark project as "NEGOTIATING"
            val project = projectDao.getProjectById(pitch.projectId).firstOrNull()
            if (project != null) {
                val updatedProj = project.copy(status = "NEGOTIATING")
                projectDao.insertProject(updatedProj)
                context?.let { SupabaseSynchronizer.syncUpProject(it, updatedProj) }
                
                // Create draft contract
                val contractId = UUID.randomUUID().toString()
                val draftContract = Contract(
                    id = contractId,
                    projectId = project.id,
                    projectTitle = project.title,
                    managerName = "You (Manager)",
                    talentName = pitch.applicantName,
                    talentId = pitch.applicantId,
                    talentRole = pitch.applicantRole,
                    hostEquity = project.hostEquity,
                    editorEquity = project.editorEquity,
                    writerEquity = project.writerEquity,
                    jurisdiction = "US-CA (California)",
                    exitClauses = "1. Immediate termination on material breach.\n2. Redistribution of equity based on actual milestones delivered.",
                    docusignEnvelopeId = "ds-env-${UUID.randomUUID().toString().substring(0, 8)}",
                    status = "SENT" // Sent for DocuSign
                )
                contractDao.insertContract(draftContract)
                context?.let { SupabaseSynchronizer.syncUpContract(it, draftContract) }
            }
        }
    }

    suspend fun signContractByTalent(projectId: String) {
        val contract = contractDao.getContractByProject(projectId).firstOrNull() ?: return
        val updatedContract = if (contract.status == "SENT") {
            contract.copy(status = "SIGNED_BY_TALENT")
        } else if (contract.status == "SIGNED_BY_MANAGER") {
            contract.copy(status = "FULLY_SIGNED", signedAtMilli = System.currentTimeMillis())
        } else {
            contract
        }
        contractDao.insertContract(updatedContract)
        context?.let { SupabaseSynchronizer.syncUpContract(it, updatedContract) }

        checkAndUnlockWorkspace(projectId, updatedContract)
    }

    suspend fun signContractByManager(projectId: String) {
        val contract = contractDao.getContractByProject(projectId).firstOrNull() ?: return
        val updatedContract = if (contract.status == "SENT") {
            contract.copy(status = "SIGNED_BY_MANAGER")
        } else if (contract.status == "SIGNED_BY_TALENT") {
            contract.copy(status = "FULLY_SIGNED", signedAtMilli = System.currentTimeMillis())
        } else {
            contract
        }
        contractDao.insertContract(updatedContract)
        context?.let { SupabaseSynchronizer.syncUpContract(it, updatedContract) }

        checkAndUnlockWorkspace(projectId, updatedContract)
    }

    private suspend fun checkAndUnlockWorkspace(projectId: String, contract: Contract) {
        if (contract.status == "FULLY_SIGNED") {
            // Update project status to "CONTRACTED"
            val project = projectDao.getProjectById(projectId).firstOrNull()
            if (project != null) {
                val updatedProj = project.copy(status = "CONTRACTED")
                projectDao.insertProject(updatedProj)
                context?.let { SupabaseSynchronizer.syncUpProject(it, updatedProj) }

                // Insert welcome message in general channel
                insertMessage(Message(
                    id = UUID.randomUUID().toString(),
                    workspaceId = projectId,
                    channel = "general",
                    senderId = "system",
                    senderName = "System Bot",
                    senderRole = "Contract Agent",
                    text = "Welcome to your shared workspace! 🎉 All parties have signed the contract (ID: ${contract.id.substring(0,6)}). Workspace is unlocked. Channel AdSense splitting is officially active.",
                    timestamp = System.currentTimeMillis()
                ))
            }
        }
    }

    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message)
        context?.let { SupabaseSynchronizer.syncUpMessage(it, message) }
    }


    suspend fun prepopulateIfEmpty() {
        // Prepopulate users if empty
        val existingUser = userDao.getUserById("me").firstOrNull()
        if (existingUser == null) {
            userDao.insertUser(User(
                id = "me",
                email = "appcroom@gmail.com",
                phone = "+1 (555) 0192",
                displayName = "VibeCreative Studio",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                primaryRole = "Co-Op Member",
                secondaryRolesJson = "[\"Video Editing\", \"Thumbnail Design\"]",
                karmaScore = 120,
                isVerifiedPro = true,
                stripeAccountId = "acct_demo_me",
                portfolioLinksJson = "[\"youtube.com/vibe_creative\", \"behance.net/vibe_creative\"]",
                availableBalance = 3450.00,
                pendingBalance = 1500.00,
                treasuryBalance = 800.00
            ))

            userDao.insertUser(User(
                id = "user_manager",
                email = "manager@coop.com",
                phone = "+1 (555) 0101",
                displayName = "Lexi Manager",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
                primaryRole = "Co-Op Member",
                secondaryRolesJson = "[\"Channel Strategy\", \"Sponsor Relations\"]",
                karmaScore = 200,
                isVerifiedPro = true,
                stripeAccountId = "acct_demo_manager",
                portfolioLinksJson = "[\"youtube.com/lexi_channel\"]",
                availableBalance = 4250.00,
                pendingBalance = 2000.00,
                treasuryBalance = 1200.00
            ))

            userDao.insertUser(User(
                id = "user_editor",
                email = "editor@coop.com",
                phone = "+1 (555) 0202",
                displayName = "Jack Editor",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde",
                primaryRole = "Co-Op Member",
                secondaryRolesJson = "[\"Video Editing\", \"VFX & Motion Graphics\"]",
                karmaScore = 150,
                isVerifiedPro = true,
                stripeAccountId = "acct_demo_editor",
                portfolioLinksJson = "[\"behance.net/jack_edits\"]",
                availableBalance = 2100.00,
                pendingBalance = 800.00,
                treasuryBalance = 400.00
            ))

            userDao.insertUser(User(
                id = "user_admin",
                email = "admin@coop.com",
                phone = "+1 (555) 0303",
                displayName = "Alex Admin",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                primaryRole = "Co-Op Member",
                secondaryRolesJson = "[\"Mediation Rules\", \"Contract Design\"]",
                karmaScore = 500,
                isVerifiedPro = true,
                stripeAccountId = "acct_demo_admin",
                portfolioLinksJson = "[]",
                availableBalance = 98500.00,
                pendingBalance = 0.0,
                treasuryBalance = 24500.00
            ))

            userDao.insertUser(User(
                id = "user_mcn",
                email = "mcn@coop.com",
                phone = "+1 (555) 0404",
                displayName = "Alpha MCN Group",
                avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2",
                primaryRole = "Co-Op Member",
                secondaryRolesJson = "[\"Compliance Audit\", \"Enterprise Match\"]",
                karmaScore = 800,
                isVerifiedPro = true,
                stripeAccountId = "acct_demo_mcn",
                portfolioLinksJson = "[]",
                availableBalance = 153000.00,
                pendingBalance = 12000.00,
                treasuryBalance = 34000.00
            ))
        }

        // Prepopulate posts if empty
        val existingNewPosts = postDao.getAllPostsSortedByNew().firstOrNull()
        if (existingNewPosts.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val posts = listOf(
                Post(
                    id = "post-1",
                    title = "Is anyone doing splits for YouTube channel growth? Let's discuss.",
                    authorName = "Host_Gaming_Channel",
                    authorRole = "Channel Manager",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde",
                    body = "Hey everyone! I run a gaming channel currently at 45k subs. Looking a permanent editor. Instead of upfront $200 per video, I'm proposing a 30% contract-backed equity share of the channel's AdSense + Sponsor payouts. This keeps interests aligned and saves dry cash. Has anyone transitioned to this model using Creator Co-Op successfully?",
                    spaceName = "Gaming",
                    timestamp = now - 3600000 * 2, // 2 hours ago
                    upvotes = 34,
                    downvotes = 1,
                    commentCount = 2,
                    userVote = "none",
                    mediaUrl = "https://www.w3schools.com/html/mov_bbb.mp4"
                ),
                Post(
                    id = "post-2",
                    title = "Timestamp feedback tools are complete game changers for team drafts",
                    authorName = "Pro_Vibe_Editor",
                    authorRole = "Video Editor",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330",
                    body = "Just finished our first production on a Tech Review channel in our Matched Workspace! Being able to link timestamps directly in chat while playing the video draft saved me 4 hours of decoding 'at 3:45 cut the transition.' If you haven't used the Workspace Feedback Tool, start now!",
                    spaceName = "Editing",
                    timestamp = now - 3600000 * 4, // 4 hours ago
                    upvotes = 56,
                    downvotes = 0,
                    commentCount = 1,
                    userVote = "none",
                    mediaUrl = "https://assets.mixkit.co/videos/preview/mixkit-girl-in-neon-sign-cyberpunk-look-39824-large.mp4"
                ),
                Post(
                    id = "post-3",
                    title = "New US Jurisdictional Exit Clauses Added to the Smart Contract Builder",
                    authorName = "LegalCreator_Lawyer",
                    authorRole = "Legal Lead",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d",
                    body = "We've locked in the standardized template for 1099-K reporting and mutual exit clauses. This avoids the major dispute of 'what happens to my equity split if I leave the team after 6 months?'. Highly suggest reviewing the updated templates under Contract Vault.",
                    spaceName = "Writing",
                    timestamp = now - 3600000 * 24, // 1 day ago
                    upvotes = 89,
                    downvotes = 2,
                    commentCount = 0,
                    userVote = "none"
                )
            )
            postDao.insertPosts(posts)

            // Dynamic comments
            val comments = listOf(
                Comment(
                    id = "c-1",
                    postId = "post-1",
                    authorName = "Pro_Vibe_Editor",
                    authorRole = "Video Editor",
                    text = "Absolutely! Best decision for long term incentive. Makes editing feel joint-owned.",
                    timestamp = now - 5400000L
                ),
                Comment(
                    id = "c-2",
                    postId = "post-1",
                    authorName = "TechExpert_Reviews",
                    authorRole = "Channel Manager",
                    text = "Did this with my thumbnail artist. Now they propose ideas proactively since thumbnails directly affect CTR and their payouts!",
                    timestamp = now - 3600000 * 1
                ),
                Comment(
                    id = "c-3",
                    postId = "post-2",
                    authorName = "Gaming_Kid_88",
                    authorRole = "Technical Specialist",
                    text = "It integrates perfectly. Glad they added video URL playback support directly in empty scopes.",
                    timestamp = now - 3600000 * 3
                )
            )
            for (comment in comments) {
                commentDao.insertComment(comment)
            }
        }

        // Prepopulate projects if empty
        val existingProjects = projectDao.getAllProjects().firstOrNull()
        if (existingProjects.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val projects = listOf(
                Project(
                    id = "proj-1",
                    managerId = "user-proj-1",
                    title = "Premium Tech Reviews Channel Needs Permanent Editor",
                    niche = "Tech",
                    contentStrategy = "High-quality, cinematic tech breakdowns focusing on workflow automation, AI laptops, and premium accessories. Delivering 2 videos per week. Equity split model is backed by US-CA standard jurisdiction contract.",
                    subscriberCount = 120000,
                    status = "PUBLISHED",
                    createdAt = now - 3600000 * 12,
                    hostEquity = 40,
                    editorEquity = 30,
                    writerEquity = 30
                ),
                Project(
                    id = "proj-2",
                    managerId = "user-proj-2",
                    title = "Narrative Comedy Animation Channel",
                    niche = "Gaming",
                    contentStrategy = "Animated Minecraft narrative sketches and viral game comedies. Production team needs 2D character designers and frame animators who will receive deep revenue splits on compilation AdSense earnings.",
                    subscriberCount = 85000,
                    status = "PUBLISHED",
                    createdAt = now - 3600000 * 24,
                    hostEquity = 30,
                    editorEquity = 20,
                    writerEquity = 10,
                    animatorEquity = 40
                ),
                Project(
                    id = "proj-3",
                    managerId = "user-proj-3",
                    title = "Viral Finance Shorts Channel",
                    niche = "Finance",
                    contentStrategy = "Daily 60-second breakdowns of markets, micro-investment tips, and startup trends. Production needs rapid script-writers and voice-over talent with lightning output.",
                    subscriberCount = 520000,
                    status = "PUBLISHED",
                    createdAt = now - 3600000 * 48,
                    hostEquity = 50,
                    editorEquity = 25,
                    writerEquity = 25
                )
            )
            projectDao.insertProjects(projects)
        }
    }
}
