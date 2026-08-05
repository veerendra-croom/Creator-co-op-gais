import re
import os

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    target = '''            if (filteredAssets.isEmpty()) {
                item {
                    Text("No assets uploaded.", color = TextSecondary, fontSize = 14.sp)
                }
            }'''
            
    replacement = '''            if (filteredAssets.isEmpty()) {
                item {
                    EmptyState(
                        message = "No assets uploaded yet.",
                        icon = Icons.Default.Folder,
                        actionText = "Upload Asset",
                        onAction = { showUploadDialog = true }
                    )
                }
            }'''
            
    content = content.replace(target, replacement)
    
    with open(filepath, 'w') as f:
        f.write(content)

fix_file("app/src/main/java/com/example/ui/screens/workspace/WorkspaceFilesHubScreen.kt")
