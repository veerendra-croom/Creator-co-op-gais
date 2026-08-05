import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

# Extract all classes in AppDatabase.kt
with open('app/src/main/java/com/example/data/local/AppDatabase.kt', 'r') as f:
    db_content = f.read()

m = re.search(r'entities\s*=\s*\[(.*?)\]', db_content, re.DOTALL)
entities_str = m.group(1)
entities_list = [x.strip().replace('::class', '') for x in entities_str.split(',')]

for entity in entities_list:
    if not entity: continue
    # check if @Entity is above data class
    pattern = re.compile(r'@Entity[^@]*?data class ' + entity + r'\b', re.DOTALL)
    if not pattern.search(content):
        print("Missing @Entity for:", entity)

