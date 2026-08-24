# Creator Co-Op: Security, Encryption & Compliance Model

---

## 1. Cryptographic Digital Agreement Vault

### 1.1 SHA-256 Content Hashing
Every collaborative team agreement generates a canonical SHA-256 cryptographic digest over its title, structured terms, and revenue/equity percentages:
$$\text{Digest} = \text{SHA-256}(\text{CanonicalTerms})$$

### 1.2 Multi-Party Signing Integrity
When co-op members sign an agreement:
1. The app verifies that the local hash matches the latest agreement version hash:
   ```kotlin
   if (agreement.contentHash != expectedHash && expectedHash.isNotBlank()) {
       rejectSignature("Terms were modified. Please review the latest revision.")
   }
   ```
2. The user's signature generates a unique `signature_hash` bound to their verified User ID and Unix timestamp.
3. If an agreement is edited by the owner, its `version` increments, `content_hash` updates, and all prior signatures are flagged as `SUPERSEDED`, prompting all members to re-acknowledge the new terms.

---

## 2. Executive Authorization Framework

### 2.1 Privileged Identity Enforcement
Platform governance, dispute resolution tools, and sandbox telemetry consoles enforce strict authorization gates:

```kotlin
fun isExecutiveAuthorized(email: String?): Boolean {
    val normalized = email?.trim()?.lowercase() ?: return false
    return normalized == "veerendrabotla@gmail.com" || 
           normalized == "praveenmacha777@gmail.com"
}
```

### 2.2 Executive Attribution & Direct Escalation
- **Founder**: Botla Veerendra (`veerendrabotla@gmail.com`)
- **Co-Founder**: Macha Praveen (`praveenmacha777@gmail.com`)
- In-app support tickets and emergency security escalation triggers generate pre-filled mailto intents directly targeting founder channels.

---

## 3. GDPR & Privacy Compliance: Cascading Account Deletion

To comply with Google Play Data Safety and GDPR requirements, the application implements an atomic cascading account purge:

1. **Local Room Database Purge**:
   - `userDao.deleteUserById(userId)`
   - `workspaceMemberDao.deleteMembershipsForUser(userId)`
   - `agreementDao.deleteAcknowledgmentsForUser(userId)`
   - `messageDao.anonymizeMessagesFromUser(userId)`
2. **Cloud Reconcile Deletion**: An event is dispatched to Supabase PostgREST with RLS cascade delete to permanently wipe remote profile records.
3. **Session Invalidation**: Clears encrypted shared preferences, local cached avatar files, and resets the UI state immediately to the onboarding screen.
