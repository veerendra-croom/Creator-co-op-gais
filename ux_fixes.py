import re
import os

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Remove "No pending reports found for category..."
    target = '''Text("No pending reports found for category: $selectedCategoryFilter", color = TextSecondary, fontSize = 11.sp)'''
    replacement = '''EmptyState(message = "No pending reports found for category: $selectedCategoryFilter", icon = Icons.Default.CheckCircle, actionText = "Refresh", onAction = {})'''
    
    # We will just do a fast regex replace
    if "AdminDashboardScreen.kt" in filepath:
        content = content.replace('''Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                            Text("No pending reports found for category: $selectedCategoryFilter", color = TextSecondary, fontSize = 11.sp)
                                        }''', '''EmptyState(message = "No pending reports found for category: $selectedCategoryFilter", icon = Icons.Default.CheckCircle)''')
                                        
        content = content.replace('''Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("No resolved reports found.", color = TextSecondary, fontSize = 12.sp)
                                    }''', '''EmptyState(message = "No resolved reports found.", icon = Icons.Default.DoneAll)''')

        content = content.replace('''Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("No risky users found.", color = TextSecondary, fontSize = 12.sp)
                                    }''', '''EmptyState(message = "No risky users found.", icon = Icons.Default.VerifiedUser)''')

        content = content.replace('''Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No user profiles match your search criteria.", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }''', '''EmptyState(message = "No user profiles match your search criteria.", icon = Icons.Default.SearchOff)''')

        content = content.replace('''Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                Text("No active verification requests found.", color = TextSecondary, fontSize = 12.sp)
                            }''', '''EmptyState(message = "No active verification requests found.", icon = Icons.Default.CheckCircle)''')

    with open(filepath, 'w') as f:
        f.write(content)

fix_file("app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt")
