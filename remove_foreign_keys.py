import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

# Match 'foreignKeys = [ ... ]' taking into account nested brackets if any (though there usually aren't nested brackets).
# Since regex for balanced brackets can be tricky, we can just match from 'foreignKeys = [' up to the first ']' followed by ')' or just remove the whole block.

# Simple regex: find "foreignKeys = [" and the next "]"
pattern = r"foreignKeys\s*=\s*\[.*?\]"
new_content = re.sub(pattern, "", content, flags=re.DOTALL)

# Cleanup any trailing commas left by removing foreignKeys
new_content = re.sub(r",\s*\)", ")", new_content)
# Cleanup any empty indices array if we want, but it's fine to leave indices

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(new_content)

