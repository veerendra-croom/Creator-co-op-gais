import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

# Replace the mangled ProjectProposalWithData
bad_str = """data class ProjectProposalWithData(
    @Embedded val proposal: ProjectProposal
    @Relation(
        parentColumn = "authorId",
        entityColumn = "id"
        val author: UserProfile?
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
        val pitches: List<TalentPitch> = emptyList(,
"""

good_str = """data class ProjectProposalWithData(
    @Embedded val proposal: ProjectProposal,
    @Relation(
        parentColumn = "authorId",
        entityColumn = "id"
    )
    val author: UserProfile?,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val pitches: List<TalentPitch> = emptyList()
)
"""

content = content.replace(bad_str, good_str)
with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(content)
