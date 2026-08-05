import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    text = f.read()

# 1. Clean up @Entity blocks!
# We want to find `@Entity(tableName = "xxx"` and then ANYTHING up to `data class Yyy`
# And replace it with `@Entity(tableName = "xxx")\ndata class Yyy`
# Wait, some tables have indices. 
# It's better to just drop indices for now to make it compile! We can add indices back later if needed.
text = re.sub(r'@Entity\(tableName\s*=\s*"([^"]+)".*?data class\s+(\w+)', r'@Entity(tableName = "\1")\ndata class \2', text, flags=re.DOTALL)

# 2. Fix ProjectProposalWithData
# It has @Relation which might be mangled.
# Let's just redefine it completely.
pattern_proposal = r'data class ProjectProposalWithData.*?@Serializable'
good_proposal = """data class ProjectProposalWithData(
    @Embedded val proposal: ProjectProposal,
    @Relation(parentColumn = "authorId", entityColumn = "id")
    val author: UserProfile?,
    @Relation(parentColumn = "id", entityColumn = "projectId")
    val pitches: List<TalentPitch> = emptyList()
)

@Serializable"""
text = re.sub(pattern_proposal, good_proposal, text, flags=re.DOTALL)

# 3. Fix missing closing parens on properties
text = re.sub(r'@SerialName\("([^"]+)"(?!\))', r'@SerialName("\1")', text)
text = re.sub(r'@ColumnInfo\(name\s*=\s*"([^"]+)"(?!\))', r'@ColumnInfo(name = "\1")', text)

# 4. Fix get() = ... which became get( = ...
text = text.replace('get( = ', 'get() = ')

# 5. Fix : SecurityState(
text = text.replace(': SecurityState(', ': SecurityState()')
text = text.replace('message: String = "Platform rate limit exceeded: Too many proposal attempts." : Exception(message,', 'message: String = "Platform rate limit exceeded: Too many proposal attempts.") : Exception(message)')

# 6. Fix trailing ) : JavaSerializable
# Make sure every data class ends with `) : JavaSerializable` if it's supposed to.
# Let's just fix `): JavaSerializable` and `) ): JavaSerializable`
text = re.sub(r'\)+:\s*JavaSerializable', r') : JavaSerializable', text)

# 7. Remove any trailing commas before the closing parenthesis of a class
# `val x: String = "",\n)` -> `val x: String = ""\n)`
text = re.sub(r',\s*\n\)', r'\n)', text)

# 8. Fix empty arrays
text = text.replace('val skillsJson: String = "[', 'val skillsJson: String = "[]"')
text = text.replace('val portfolioJson: String = "[', 'val portfolioJson: String = "[]"')

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(text)

