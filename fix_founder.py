import re

def insert_after(filepath, target, addition):
    with open(filepath, 'r') as f:
        content = f.read()
    content = content.replace(target, target + "\n" + addition)
    with open(filepath, 'w') as f:
        f.write(content)

fix_founder = "app/src/main/java/com/example/ui/screens/FounderCommandCenterScreen.kt"

insert_after(fix_founder, 'text = "Closed Beta Governance & Executive Deck",', '                            color = TextSecondary,\n                            fontSize = 10.sp,\n                            fontWeight = FontWeight.Medium\n                        )\n                        Text(\n                            text = "Created by Botla Veerendra & Macha Praveen",')

