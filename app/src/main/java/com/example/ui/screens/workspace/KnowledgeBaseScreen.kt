package com.example.ui.screens.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun KnowledgeBaseScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val allArticles = listOf(
        ArticleItemData("Syndicate Onboarding", "Standard procedure for new shard members.", "GOVERNANCE"),
        ArticleItemData("Revenue Split Anchoring", "How to verify and lock programmatic splits.", "FINANCE"),
        ArticleItemData("Dispute Resolution", "Protocol for resolving creative differences.", "LEGAL"),
        ArticleItemData("Production Quality Gates", "Standards for moving tasks to PUBLISH lane.", "QUALITY"),
        ArticleItemData("AI Prompt Standards", "Ensuring consistent output from Gemini agents.", "TECH")
    )

    val articles = remember(searchQuery) {
        if (searchQuery.isEmpty()) allArticles
        else allArticles.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.category.contains(searchQuery, ignoreCase = true) 
        }
    }

    val categories = allArticles.map { it.category }.distinct()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("KNOWLEDGE BASE", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search manuals, protocols...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentRed,
                unfocusedBorderColor = ColorDivider,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("OPERATIONAL CATEGORIES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        AssistChip(
                            onClick = { searchQuery = cat },
                            label = { Text(cat) },
                            colors = AssistChipDefaults.assistChipColors(labelColor = AccentBlue),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Text("PROCEDURAL MANUALS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (articles.isEmpty()) {
                item {
                    Text("No matching protocols found in archive.", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                items(articles) { article ->
                    ArticleItem(title = article.title, subtitle = article.subtitle, category = article.category)
                }
            }
        }
    }
}

data class ArticleItemData(val title: String, val subtitle: String, val category: String)

@Composable
fun ArticleItem(title: String, subtitle: String, category: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoStories, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(category, color = AccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
