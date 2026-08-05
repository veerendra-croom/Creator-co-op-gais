import re

def add_index(filepath, table_name, index_cols):
    with open(filepath, 'r') as f:
        content = f.read()
        
    pattern = r'@Entity\(tableName = "' + table_name + r'"\)'
    cols_str = ', '.join([f'"{c}"' for c in index_cols])
    replacement = f'@Entity(tableName = "{table_name}", indices = [Index(value = [{cols_str}])])'
    
    new_content = re.sub(pattern, replacement, content)
    
    with open(filepath, 'w') as f:
        f.write(new_content)

add_index("app/src/main/java/com/example/data/model/WorkspaceEntities.kt", "workspace_assets", ["workspaceId"])
add_index("app/src/main/java/com/example/data/model/WorkspaceEntities.kt", "workspace_members", ["workspaceId", "userId"])
add_index("app/src/main/java/com/example/data/model/WorkspaceEntities.kt", "workspace_events", ["workspaceId"])
add_index("app/src/main/java/com/example/data/model/ProductionEntities.kt", "production_tasks", ["workspaceId", "assigneeId"])
add_index("app/src/main/java/com/example/data/model/ProductionEntities.kt", "deliverables", ["workspaceId", "taskId"])
add_index("app/src/main/java/com/example/data/model/SocialEntities.kt", "posts", ["authorId"])
add_index("app/src/main/java/com/example/data/model/SocialEntities.kt", "comments", ["postId", "authorId"])
add_index("app/src/main/java/com/example/data/model/AdminEntities.kt", "reports", ["targetId", "status"])

