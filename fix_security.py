import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    text = f.read()

text = text.replace('object Idle : SecurityState())', 'object Idle : SecurityState()')
text = text.replace('object Authenticating : SecurityState())', 'object Authenticating : SecurityState()')
text = text.replace('data class Authenticated(val userId: String : SecurityState())', 'data class Authenticated(val userId: String) : SecurityState()')
text = text.replace('data class Error(val message: String : SecurityState())', 'data class Error(val message: String) : SecurityState()')
text = text.replace('object SybilThrottled : SecurityState())', 'object SybilThrottled : SecurityState()')

# Also fix `[]"]`
text = text.replace('[]"]', '[]"')

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(text)

