import re

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

missing = {
    "Endorsement": "endorsements",
    "WorkspaceMember": "workspace_members",
    "ProductionTask": "production_tasks",
    "TeamAgreement": "team_agreements",
    "AgreementAcknowledgment": "agreement_acknowledgments",
    "Message": "messages",
    "Comment": "comments",
    "ProjectProposal": "project_proposals",
    "TalentPitch": "talent_pitches",
    "DisputeNote": "dispute_notes_table",
    "Referral": "referrals",
    "Deliverable": "deliverables",
    "WorkspaceEvent": "workspace_events"
}

# Also we need to clean up stray '])' or ')' before these data classes.
# The stray tokens look like `) \n ] \n )` or similar.
# It's safer to just regex `\)\s*\]\)\s*data class ClassName` and replace with `@Entity... data class ClassName`

for cls, table in missing.items():
    # Remove any stray brackets/parentheses before the data class
    # We can just look for everything between `@Serializable` and `data class ClassName`
    # or just replace the whole mess before `data class ClassName` if it's not a valid annotation.
    # Actually, all these classes should have @Serializable before them (or at least they did).
    
    # Let's find "data class ClassName"
    # and replace any preceding junk
    pattern = re.compile(r'(\n.*?)\bdata class ' + cls + r'\(', re.DOTALL)
    
    def repl(m):
        prefix = m.group(1)
        # remove stray brackets
        prefix = re.sub(r'[\)\]\,]+', '', prefix)
        return prefix + f'\n@Entity(tableName = "{table}")\ndata class {cls}('
    
    content = pattern.sub(repl, content, count=1)

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(content)
