with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    text = f.read()

lines = text.split('\n')
for i, line in enumerate(lines):
    if line.strip().startswith('//'): continue
    if line.count('(') != line.count(')'):
        print(f"Line {i+1} mismatch: {line}")

