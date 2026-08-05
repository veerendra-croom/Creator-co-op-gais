import re
import os

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # We will just replace specific chunks in DashboardScreen
    if "DashboardScreen.kt" in filepath:
        # Replace the Surface block for "Create First Task"
        target = '''Surface(
                            color = SurfaceLightColor.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)),
                            shape = DS.RadiusMedium
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(DS.Space24), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Create First Task",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }'''
        replacement = '''EmptyState(
                            message = "Your backlog is perfectly clear.",
                            icon = Icons.Default.Assignment,
                            actionText = "Create First Task",
                            onAction = { showAddTaskDialog = true }
                        )'''
        content = content.replace(target, replacement)
        
        target2 = '''Surface(
                            color = SurfaceLightColor.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)),
                            shape = DS.RadiusMedium
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(DS.Space24), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No recent activity.",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }'''
        replacement2 = '''EmptyState(
                            message = "No recent activity.",
                            icon = Icons.Default.History,
                            actionText = "Browse Workspace",
                            onAction = { /* Navigate to workspace */ }
                        )'''
        content = content.replace(target2, replacement2)
        
        with open(filepath, 'w') as f:
            f.write(content)

fix_file("app/src/main/java/com/example/ui/screens/DashboardScreen.kt")
