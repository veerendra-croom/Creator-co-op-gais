import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

# Fix @Entity(tableName = "users"data class -> @Entity(tableName = "users")\ndata class
content = re.sub(r'@Entity\(tableName\s*=\s*"([^"]+)"\s*data class', r'@Entity(tableName = "\1")\ndata class', content)

# Fix @SerialName("id" val -> @SerialName("id") val
content = re.sub(r'@SerialName\("([^"]+)"\s*val', r'@SerialName("\1") val', content)

# Fix @SerialName("id" var -> @SerialName("id") var
content = re.sub(r'@SerialName\("([^"]+)"\s*var', r'@SerialName("\1") var', content)

# Fix @ColumnInfo(name = "id" val -> @ColumnInfo(name = "id") val
content = re.sub(r'@ColumnInfo\(name\s*=\s*"([^"]+)"\s*val', r'@ColumnInfo(name = "\1") val', content)

# Fix Index("..." -> Index("...")
content = re.sub(r'Index\("([^"]+)"\s*(?=[,\]\)])', r'Index("\1")', content)

def add_comma(m):
    line = m.group(0)
    if not line.strip().endswith(',') and not line.strip().endswith('{') and not line.strip().endswith('}'):
        return line + ','
    return line

content = re.sub(r'^(.*?val [^:]+:\s*[^=]+(?:=.*)?)$', add_comma, content, flags=re.MULTILINE)
content = re.sub(r'^(.*?var [^:]+:\s*[^=]+(?:=.*)?)$', add_comma, content, flags=re.MULTILINE)

content = re.sub(r'([^\)])(\s*:\s*JavaSerializable)', r'\1)\2', content)

# Fix missing closing parenthesis for data class definitions
# Typically data classes end with something like:
#     @SerialName("x") val x: String = ""
# ) : JavaSerializable
# Or just
# )
# But right now they might just end with:
#     @SerialName("x") val x: String = ""
# @Serializable
# @Entity...
# Let's see if we can just append `)` before `@Serializable` or `@Entity` if it's not there.
content = re.sub(r'(,\s*\n)(\s*@Serializable)', r'\1)\n\2', content)

with open('app/src/main/java/com/example/data/model/Entities_rescued.kt', 'w') as f:
    f.write(content)

