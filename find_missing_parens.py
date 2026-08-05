with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    text = f.read()

# We can split by 'data class ' and see if the parenthesis balance out in each chunk.
chunks = text.split('data class ')
for chunk in chunks[1:]:
    name = chunk.split('(')[0].strip()
    c1 = chunk.count('(')
    c2 = chunk.count(')')
    if c1 != c2:
        print(f"Mismatch in {name}: (={c1} )={c2}")

