import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    text = f.read()

def fix_line(m):
    line = m.group(1)
    # if it's missing the closing parens for a method call, we might have `System.currentTimeMillis( -`
    line = line.replace('System.currentTimeMillis( -', 'System.currentTimeMillis() -')
    line = line.replace('System.currentTimeMillis( ', 'System.currentTimeMillis() ')
    line = line.replace('System.currentTimeMillis(', 'System.currentTimeMillis()')
    # If the line already ends with ), do nothing. Otherwise add ).
    if not line.strip().endswith(')'):
        line = line + ')'
    return line + ' : JavaSerializable'

text = re.sub(r'(.*?)\s*:\s*JavaSerializable', fix_line, text)

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(text)

