with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    stripped = line.strip()
    if stripped.startswith('ForeignKey(') or \
       stripped.startswith('entity =') or \
       stripped.startswith('parentColumns =') or \
       stripped.startswith('childColumns =') or \
       stripped.startswith('onDelete =') or \
       stripped.startswith('indices =') or \
       stripped.startswith('Index(') or \
       stripped == '),' or \
       stripped == ']' or \
       stripped == '],' or \
       stripped == '])' or \
       'ForeignKey' in line:
        continue
    new_lines.append(line)

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.writelines(new_lines)

