import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

content = re.sub(r'@Entity\(tableName\s*=\s*"([^"]+)"\s*data class', r'@Entity(tableName = "\1")\ndata class', content)
content = re.sub(r'@SerialName\("([^"]+)"\s*val', r'@SerialName("\1") val', content)
content = re.sub(r'@SerialName\("([^"]+)"\s*var', r'@SerialName("\1") var', content)
content = re.sub(r'@ColumnInfo\(name\s*=\s*"([^"]+)"\s*val', r'@ColumnInfo(name = "\1") val', content)
content = re.sub(r'Index\("([^"]+)"\s*(?=[,\]\)])', r'Index("\1")', content)

def add_comma(m):
    line = m.group(0)
    # Check if there is a comment
    comment_idx = line.find('//')
    if comment_idx != -1:
        before_comment = line[:comment_idx]
        if not before_comment.strip().endswith(','):
            return before_comment.rstrip() + ', ' + line[comment_idx:]
        return line
    else:
        if not line.strip().endswith(',') and not line.strip().endswith('{') and not line.strip().endswith('}'):
            return line + ','
        return line

content = re.sub(r'^(.*?val [^:]+:\s*[^=]+(?:=.*)?)$', add_comma, content, flags=re.MULTILINE)
content = re.sub(r'^(.*?var [^:]+:\s*[^=]+(?:=.*)?)$', add_comma, content, flags=re.MULTILINE)

content = re.sub(r'([^\)])(\s*:\s*JavaSerializable)', r'\1)\2', content)

# Fix empty arrays that were missing ] like `skillsJson: String = "[`
content = re.sub(r'=\s*"\[",', r'= "[]",', content)
content = re.sub(r'=\s*"\["\n', r'= "[]"\n', content)

# Add closing parenthesis for class if missing before @Serializable
content = re.sub(r'(,\s*\n)(\s*@Serializable)', r'\1)\n\2', content)
# Add closing parenthesis for class if missing before @Entity
content = re.sub(r'(,\s*\n)(\s*@Entity)', r'\1)\n\2', content)

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(content)

