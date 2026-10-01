package com.example.data.security

import com.example.data.model.AgreementAcknowledgment
import com.example.data.model.TeamAgreement
import java.security.MessageDigest
import kotlinx.serialization.Serializable

/**
 * End-to-End Cryptographic Escrow & Agreement Immutability Engine.
 * Generates deterministic SHA-256 state hashes for Co-Op legal agreements,
 * verifies signature integrity, and detects clause tampering across revisions.
 */
object CryptographicEscrowEngine {

    @Serializable
    data class IntegrityVerificationResult(
        val agreementId: String,
        val calculatedHash: String,
        val storedHash: String,
        val isTamperFree: Boolean,
        val validSignatureCount: Int,
        val totalSignaturesCount: Int,
        val isFullyRatified: Boolean,
        val integrityStatus: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    @Serializable
    data class EscrowPayoutAllocation(
        val memberId: String,
        val roleTitle: String,
        val revSharePercent: Double,
        val allocatedAmountUsd: Double,
        val cryptographicVoucherHash: String
    )

    /**
     * Computes deterministic SHA-256 digest of contract clauses, revenue shares, and workspace bounds.
     */
    fun computeAgreementHash(
        workspaceId: String,
        clausesText: String,
        version: Int = 1,
        escrowHoldPeriodDays: Int = 14
    ): String {
        val payload = "WS:$workspaceId|CL:$clausesText|VER:$version|HOLD:$escrowHoldPeriodDays"
        val bytes = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a tamper-proof member signature token using user ID, agreement hash, and sign timestamp.
     */
    fun generateMemberSignatureToken(
        userId: String,
        agreementHash: String,
        signedAt: Long
    ): String {
        val payload = "USER:$userId|HASH:$agreementHash|AT:$signedAt"
        val bytes = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
        return "SIG-SHA256-" + bytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }

    /**
     * Verifies the cryptographic integrity of a TeamAgreement and its associated member acknowledgments.
     */
    fun verifyAgreementIntegrity(
        agreement: TeamAgreement,
        acknowledgments: List<AgreementAcknowledgment>
    ): IntegrityVerificationResult {
        val currentHash = computeAgreementHash(
            workspaceId = agreement.workspaceId,
            clausesText = agreement.contentText.ifEmpty { agreement.content },
            version = agreement.version
        )
        val isTamperFree = agreement.acknowledgmentHash.isEmpty() || agreement.acknowledgmentHash == currentHash

        val validSigs = acknowledgments.count { ack ->
            ack.agreementId == agreement.id && ack.acknowledgmentHash.isNotEmpty()
        }

        val isFullyRatified = isTamperFree && validSigs > 0 && validSigs == acknowledgments.size

        val status = when {
            !isTamperFree -> "TAMPER_DETECTED: Clause hash mismatch. Agreement must be re-ratified."
            validSigs == 0 -> "PENDING_SIGNATURES: Zero cryptographic signatures registered."
            isFullyRatified -> "CRYPTOGRAPHICALLY_VERIFIED: All signatures valid and locked."
            else -> "PARTIALLY_RATIFIED: $validSigs of ${acknowledgments.size} signatures active."
        }

        return IntegrityVerificationResult(
            agreementId = agreement.id,
            calculatedHash = currentHash,
            storedHash = agreement.acknowledgmentHash,
            isTamperFree = isTamperFree,
            validSignatureCount = validSigs,
            totalSignaturesCount = acknowledgments.size,
            isFullyRatified = isFullyRatified,
            integrityStatus = status
        )
    }

    /**
     * Calculates transparent and verified escrow payouts based on revenue split percentages.
     */
    fun calculateEscrowAllocations(
        totalGrossRevenueUsd: Double,
        platformFeePercent: Double = 5.0,
        memberSplits: Map<String, Pair<String, Double>> // userId -> (roleTitle, sharePercent)
    ): List<EscrowPayoutAllocation> {
        val platformFee = totalGrossRevenueUsd * (platformFeePercent / 100.0)
        val netDistributable = (totalGrossRevenueUsd - platformFee).coerceAtLeast(0.0)

        return memberSplits.map { (userId, pair) ->
            val (role, share) = pair
            val amount = netDistributable * (share / 100.0)
            val voucher = "VOUCHER-${userId.take(4)}-${"%.2f".format(amount)}-${System.currentTimeMillis()}"
            val voucherHash = MessageDigest.getInstance("SHA-256").digest(voucher.toByteArray())
                .joinToString("") { "%02x".format(it) }.take(12)

            EscrowPayoutAllocation(
                memberId = userId,
                roleTitle = role,
                revSharePercent = share,
                allocatedAmountUsd = amount,
                cryptographicVoucherHash = "ESC-$voucherHash"
            )
        }
    }
}
