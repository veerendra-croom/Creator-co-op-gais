import re

def fix_onboarding(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    target = '''                }
            }
        }
    ) { padding ->'''
    
    replacement = '''                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Created by Botla Veerendra & Macha Praveen", color = TextSecondary, fontSize = 10.sp)
            }
        }
    ) { padding ->'''
    
    content = content.replace(target, replacement)
    
    with open(filepath, 'w') as f:
        f.write(content)

fix_onboarding("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt")
