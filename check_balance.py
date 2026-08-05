with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    text = f.read()

print("(:", text.count('('))
print("):", text.count(')'))

