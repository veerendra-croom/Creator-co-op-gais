import re

def fix_onboarding(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # The easiest way is to modify the 4th onboarding page description
    target = 'description = "Establish your first workspace node and start collaborating with the elite 1% of creators."'
    replacement = 'description = "Establish your first workspace node and start collaborating with the elite 1% of creators.\\n\\nCreated by Botla Veerendra & Macha Praveen."'
    
    content = content.replace(target, replacement)
    
    with open(filepath, 'w') as f:
        f.write(content)

fix_onboarding("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt")
