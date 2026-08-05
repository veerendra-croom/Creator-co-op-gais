import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

# We need to find @Entity(...) and replace it.
# Because the contents of @Entity(...) can span multiple lines and have nested brackets,
# we can find the start of @Entity( and then find the matching closing parenthesis.

def fix_entity(match):
    inner = match.group(1)
    
    # extract tableName if present
    table_name_match = re.search(r'tableName\s*=\s*"([^"]+)"', inner)
    table_name = table_name_match.group(1) if table_name_match else None
    
    # extract indices if present. It might be complex, let's just find `indices = \[.*?\]` (assuming no nested brackets in indices? wait, Index("..") has no brackets).
    # But wait, indices might have been mangled too? No, indices was after foreignKeys sometimes.
    indices_match = re.search(r'indices\s*=\s*\[(.*?)\]', inner, re.DOTALL)
    indices_str = indices_match.group(1) if indices_match else ""
    
    if table_name and indices_str:
        return f'@Entity(tableName = "{table_name}", indices = [{indices_str.strip()}])'
    elif table_name:
        return f'@Entity(tableName = "{table_name}")'
    else:
        return '@Entity'

new_content = re.sub(r'@Entity\((.*?)\)', fix_entity, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(new_content)
