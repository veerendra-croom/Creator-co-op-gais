import re

def insert_after(filepath, target, addition):
    with open(filepath, 'r') as f:
        content = f.read()
    content = content.replace(target, target + "\n" + addition)
    with open(filepath, 'w') as f:
        f.write(content)

fix_admin = "app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt"

# Add state variable
insert_after(fix_admin, "var showSponsorships by remember { mutableStateOf(false) }", "    var showAdminHelp by remember { mutableStateOf(false) }")

# Add Help button
help_btn = '''
                Button(
                    onClick = { showAdminHelp = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("open_admin_help_button").padding(start = 6.dp).height(48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.HelpOutline, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(DS.Space8))
                    Text("HELP", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }'''
insert_after(fix_admin, 'Text("SPONSORS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)\n                }', help_btn)

# Add Dialog inclusion
dialog_inc = '''
    if (showAdminHelp) {
        AdminHelpDialog(onDismiss = { showAdminHelp = false })
    }'''
insert_after(fix_admin, 'onDismiss = { showSponsorships = false }\n        )\n    }', dialog_inc)

