import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    text = f.read()

# Fix @SerialName("...")
text = re.sub(r'@SerialName\("([^"]+)"(?!\))', r'@SerialName("\1")', text)

# Fix @ColumnInfo(name = "...")
text = re.sub(r'@ColumnInfo\(name\s*=\s*"([^"]+)"(?!\))', r'@ColumnInfo(name = "\1")', text)

# Fix Index("...")
text = re.sub(r'Index\("([^"]+)"(?!\))', r'Index("\1")', text)

# Fix System.currentTimeMillis(,
text = text.replace('System.currentTimeMillis(,', 'System.currentTimeMillis(),')

# Fix : SecurityState(
text = text.replace(': SecurityState(', ': SecurityState()')

# Fix emptyList(,
text = text.replace('emptyList(,{', 'emptyList(),')
text = text.replace('emptyList(,', 'emptyList(),')

# Fix PENDING ACCEPTED DECLINED)
text = text.replace('PENDING ACCEPTED DECLINED)', 'PENDING, ACCEPTED, DECLINED')

# Fix Index(value = ["workspaceId" "eventType" "entityId" unique = true
text = text.replace('Index(value = ["workspaceId" "eventType" "entityId" unique = true', 'Index(value = ["workspaceId", "eventType", "entityId"], unique = true)')

# Fix indices = [Index("agreementId" Index("userId"
text = text.replace('Index("agreementId" Index("userId")', 'Index("agreementId"), Index("userId")')
text = text.replace('Index("workspaceId" Index("authorId" Index("targetUserId"', 'Index("workspaceId"), Index("authorId"), Index("targetUserId")')
text = text.replace('Index("referrerId" Index("referredUserId"', 'Index("referrerId"), Index("referredUserId")')
text = text.replace('Index("taskId" Index("submitterId"', 'Index("taskId"), Index("submitterId")')

# Replace rogue empty lines
text = text.replace('\n\n\n', '\n')

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(text)

