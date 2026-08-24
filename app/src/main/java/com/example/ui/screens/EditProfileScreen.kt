package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodels.GlobalViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userProfile: UserProfile,
    globalViewModel: GlobalViewModel,
    onBack: () -> Unit
) {
    var displayName by remember { mutableStateOf(userProfile.displayName) }
    var bio by remember { mutableStateOf(userProfile.bio) }
    var websiteUrl by remember { mutableStateOf(userProfile.websiteUrl) }
    var specialty by remember { mutableStateOf(userProfile.primarySpecialty) }
    var availabilityStatus by remember { mutableStateOf(userProfile.availabilityStatus) }
    
    val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    val skills = remember { 
        val initialSkills = try { 
            json.decodeFromString(ListSerializer(String.serializer()), userProfile.skillsJson)
        } catch (e: Exception) { 
            emptyList<String>() 
        }
        val list = mutableStateListOf<String>()
        list.addAll(initialSkills)
        list
    }
    val portfolioItems = remember { 
        val initialPortfolio = try { 
            json.decodeFromString(ListSerializer(String.serializer()), userProfile.portfolioJson)
        } catch (e: Exception) { 
            emptyList<String>() 
        }
        val list = mutableStateListOf<String>()
        list.addAll(initialPortfolio)
        list
    }
    
    val specialtiesList = listOf("Video Editor", "Scriptwriter", "VFX Artist", "3D Animator", "Sound Engineer", "Growth Strategist", "Thumbnail Designer")
    var newSkill by remember { mutableStateOf("") }
    var newPortfolioTitle by remember { mutableStateOf("") }

    val currentUserId = userProfile.id
    val existingListingState by remember(currentUserId) { globalViewModel.getListingForUser(currentUserId) }.collectAsState(initial = null)
    
    var isSeekingWork by remember { mutableStateOf(false) }
    var lfwSkills by remember { mutableStateOf("") }
    var lfwAvailability by remember { mutableStateOf("") }
    var lfwRateExpectations by remember { mutableStateOf("") }
    
    LaunchedEffect(existingListingState) {
        existingListingState?.let { listing ->
            isSeekingWork = listing.isActive
            val details = try {
                json.decodeFromString<com.example.data.model.LookingForWorkDetails>(listing.detailsJson)
            } catch (e: Exception) {
                com.example.data.model.LookingForWorkDetails()
            }
            lfwSkills = details.skills
            lfwAvailability = details.availability
            lfwRateExpectations = details.rateExpectations
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("edit_profile_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Edit Profile",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = {
                    val updatedProfile = userProfile.copy(
                        displayName = displayName,
                        bio = bio,
                        websiteUrl = websiteUrl,
                        primarySpecialty = specialty,
                        skillsJson = json.encodeToString(ListSerializer(String.serializer()), skills.toList()),
                        portfolioJson = json.encodeToString(ListSerializer(String.serializer()), portfolioItems.toList()),
                        availabilityStatus = availabilityStatus
                    )
                    globalViewModel.updateUserProfile(updatedProfile)
                    
                    if (isSeekingWork) {
                        val detailsJson = json.encodeToString(
                            com.example.data.model.LookingForWorkDetails.serializer(),
                            com.example.data.model.LookingForWorkDetails(
                                skills = lfwSkills,
                                availability = lfwAvailability,
                                rateExpectations = lfwRateExpectations
                            )
                        )
                        globalViewModel.insertWorkListing(
                            com.example.data.model.LookingForWork(
                                userId = currentUserId,
                                detailsJson = detailsJson,
                                isActive = true,
                                createdAt = existingListingState?.createdAt ?: System.currentTimeMillis()
                            )
                        )
                    } else {
                        globalViewModel.deleteWorkListingForUser(currentUserId)
                    }
                    
                    onBack()
                },
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("SAVE PROFILE", fontWeight = FontWeight.Black, color = AccentBlue)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
            
            // Avatar Section
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(SurfaceColor)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(40.dp))
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(AccentBlue)
                            .padding(6.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            ProfileTextField(
                label = "Display Name",
                value = displayName,
                onValueChange = { displayName = it },
                icon = Icons.Default.Badge,
                tag = "edit_name_input"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("PRIMARY SPECIALTY", style = MaterialTheme.typography.labelLarge, color = AccentBlue, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                specialtiesList.forEach { s ->
                    val selected = specialty == s
                    Surface(
                        onClick = { specialty = s },
                        color = if (selected) AccentBlue.copy(alpha = 0.2f) else SurfaceColor,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (selected) AccentBlue else ColorDivider),
                        modifier = Modifier.testTag("specialty_chip_$s")
                    ) {
                        Text(
                            text = s,
                            color = if (selected) AccentBlue else Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("AVAILABILITY STATUS", style = MaterialTheme.typography.labelLarge, color = AccentBlue, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (availabilityStatus == "OPEN_TO_PROJECTS") "Open to Projects" else "Not Available",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (availabilityStatus == "OPEN_TO_PROJECTS") "Active. Visible in Discovery & Talent Directory." else "Busy. Hiding profile status from active searches.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = availabilityStatus == "OPEN_TO_PROJECTS",
                        onCheckedChange = { isChecked ->
                            availabilityStatus = if (isChecked) "OPEN_TO_PROJECTS" else "NOT_AVAILABLE"
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentBlue,
                            checkedTrackColor = AccentBlue.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("availability_toggle")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("SEEKING WORK DETAILS", style = MaterialTheme.typography.labelLarge, color = AccentBlue, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Seeking Work Listing",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Show on 'Available Talent' board so leads can invite you.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isSeekingWork,
                            onCheckedChange = { isSeekingWork = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentBlue,
                                checkedTrackColor = AccentBlue.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("seeking_work_toggle")
                        )
                    }
                    
                    if (isSeekingWork) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = lfwSkills,
                            onValueChange = { lfwSkills = it },
                            label = { Text("What skills are you offering? (e.g. 4K Drone editing, VFX)") },
                            modifier = Modifier.fillMaxWidth().testTag("seeking_work_skills"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceColor,
                                unfocusedContainerColor = SurfaceColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = AccentBlue,
                                unfocusedLabelColor = TextSecondary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = lfwAvailability,
                            onValueChange = { lfwAvailability = it },
                            label = { Text("Availability (e.g. 15 hours/week, immediate start)") },
                            modifier = Modifier.fillMaxWidth().testTag("seeking_work_availability"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceColor,
                                unfocusedContainerColor = SurfaceColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = AccentBlue,
                                unfocusedLabelColor = TextSecondary
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = lfwRateExpectations,
                            onValueChange = { lfwRateExpectations = it },
                            label = { Text("Rate Expectations (e.g. $45/hour, project flat-rates)") },
                            modifier = Modifier.fillMaxWidth().testTag("seeking_work_rates"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceColor,
                                unfocusedContainerColor = SurfaceColor,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = AccentBlue,
                                unfocusedLabelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            ProfileTextField(
                label = "Professional Bio",
                value = bio,
                onValueChange = { bio = it },
                icon = Icons.Default.Info,
                singleLine = false,
                minLines = 3,
                tag = "edit_bio_input"
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            ProfileTextField(
                label = "Portfolio/Website URL",
                value = websiteUrl,
                onValueChange = { websiteUrl = it },
                icon = Icons.Default.Language,
                tag = "edit_website_input"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("PROJECT SHOWCASE", style = MaterialTheme.typography.labelLarge, color = AccentBlue, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newPortfolioTitle,
                    onValueChange = { newPortfolioTitle = it },
                    modifier = Modifier.weight(1f).testTag("add_portfolio_input"),
                    placeholder = { Text("Project Title", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AccentBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        if (newPortfolioTitle.isNotBlank()) {
                            portfolioItems.add(newPortfolioTitle)
                            newPortfolioTitle = ""
                        }
                    },
                    modifier = Modifier.background(SurfaceColor, RoundedCornerShape(12.dp)).testTag("add_portfolio_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Portfolio Item", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            portfolioItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(SurfaceColor, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item, color = Color.White, fontWeight = FontWeight.Medium)
                    IconButton(onClick = { portfolioItems.remove(item) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = AccentRed, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("SKILLS & EXPERTISE", style = MaterialTheme.typography.labelLarge, color = AccentBlue, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newSkill,
                    onValueChange = { newSkill = it },
                    modifier = Modifier.weight(1f).testTag("add_skill_input"),
                    placeholder = { Text("e.g. Motion Graphics", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AccentBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = {
                        if (newSkill.isNotBlank()) {
                            skills.add(newSkill)
                            newSkill = ""
                        }
                    },
                    modifier = Modifier.background(SurfaceColor, RoundedCornerShape(12.dp)).testTag("add_skill_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Skill", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skills.forEach { skill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceColor)
                            .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
                            .clickable { skills.remove(skill) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(skill, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    singleLine: Boolean = true,
    minLines: Int = 1,
    tag: String
) {
    Column {
        Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Color.White),
            modifier = Modifier.fillMaxWidth().testTag(tag),
            leadingIcon = { Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = ColorDivider,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AccentBlue
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = singleLine,
            minLines = minLines
        )
    }
}
