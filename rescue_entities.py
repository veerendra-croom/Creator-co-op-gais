import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

# Fix @Entity(tableName = "..."data class -> @Entity(tableName = "...")\ndata class
content = re.sub(r'@Entity\(tableName = "([^"]+)"data class', r'@Entity(tableName = "\1")\ndata class', content)

# Fix @SerialName("...") val -> @SerialName("...") val
content = re.sub(r'@SerialName\("([^"]+)"\s*val', r'@SerialName("\1") val', content)

# Fix @SerialName("...") var -> @SerialName("...") var
content = re.sub(r'@SerialName\("([^"]+)"\s*var', r'@SerialName("\1") var', content)

# Fix @ColumnInfo(name = "..." val -> @ColumnInfo(name = "...") val
content = re.sub(r'@ColumnInfo\(name = "([^"]+)"\s*val', r'@ColumnInfo(name = "\1") val', content)

# Every line that has `val ` or `var ` inside a data class should probably have a comma at the end, 
# UNLESS it's the last property before a closing parenthesis or the class body.
# But Kotlin data classes can have trailing commas!
# So we can just add a comma to the end of every line that declares a property.
# A property line looks like `    val name: Type = Default` or `@SerialName(...) val ...`
# Let's find all such lines and append a comma if it doesn't have one and doesn't end with `{` or `}`.

def add_comma(m):
    line = m.group(0)
    if not line.strip().endswith(',') and not line.strip().endswith('{') and not line.strip().endswith('}'):
        return line + ','
    return line

# Actually, just finding lines that define a property:
content = re.sub(r'^(.*?val [^:]+:\s*[^=]+(?:=.*)?)$', add_comma, content, flags=re.MULTILINE)
content = re.sub(r'^(.*?var [^:]+:\s*[^=]+(?:=.*)?)$', add_comma, content, flags=re.MULTILINE)

# Also fix `) : JavaSerializable` if it got stripped? 
# In the original file, we had `) : JavaSerializable` but maybe the `)` got stripped!
# So we have ` : JavaSerializable`. We need to prepend `)` if it's missing.
content = re.sub(r'([^\)])(\s*:\s*JavaSerializable)', r'\1)\2', content)

with open('app/src/main/java/com/example/data/model/Entities_rescued.kt', 'w') as f:
    f.write(content)

