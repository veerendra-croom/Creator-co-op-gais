import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'data class ProjectProposalWithData\s*\(.*?\)\s*@Serializable', re.DOTALL)

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

@Serializable"""

content = pattern.sub(good_str, content)

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(content)
