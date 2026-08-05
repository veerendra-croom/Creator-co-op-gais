with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    text = f.read()

# Instead of exact string matching, use regex
import re

text = re.sub(r'val allowedEmailDomains: List<String> = emptyList\(\),\n+', 'val allowedEmailDomains: List<String> = emptyList()\n)\n\n', text)
text = re.sub(r'val approvedAuditorIds: List<String> = emptyList\(\),\n+', 'val approvedAuditorIds: List<String> = emptyList()\n)\n\n', text)

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(text)
