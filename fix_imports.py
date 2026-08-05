import re

def fix_imports(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    if 'import androidx.room.Index' not in content:
        content = content.replace('import androidx.room.*', 'import androidx.room.*\nimport androidx.room.Index')
    with open(filepath, 'w') as f:
        f.write(content)

fix_imports("app/src/main/java/com/example/data/model/AdminEntities.kt")
fix_imports("app/src/main/java/com/example/data/model/ProductionEntities.kt")
fix_imports("app/src/main/java/com/example/data/model/SocialEntities.kt")
fix_imports("app/src/main/java/com/example/data/model/WorkspaceEntities.kt")
