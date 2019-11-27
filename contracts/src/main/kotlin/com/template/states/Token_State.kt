package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.utilities.ProgressTracker
//import com.r3.corda
import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import java.math.BigDecimal
import net.corda.core.contracts.BelongsToContract
import com.template.contracts.Token_Contract
import com.google.common.collect.ImmutableList
import java.util.Objects

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
@BelongsToContract(Token_Contract::class)
class RealEstateEvolvableTokenType(val valuation: BigDecimal,
                                   val maintainer: Party,
                                   override val linearId: UniqueIdentifier,
                                   override val fractionDigits: Int) : EvolvableTokenType() {

    override val maintainers: List<Party>
        get() = ImmutableList.of(maintainer)

    fun getUniqueIdentifier(): UniqueIdentifier {
        return linearId
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as RealEstateEvolvableTokenType?
        return fractionDigits == that!!.fractionDigits &&
                valuation == that.valuation &&
                maintainer == that.maintainer &&
                linearId == that.linearId
    }

    override fun hashCode(): Int {
        return Objects.hash(valuation, maintainer, linearId, fractionDigits)
    }
}