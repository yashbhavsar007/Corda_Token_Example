package com.template.states

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
class TestTokenType(val valuation: BigDecimal,
                                   val maintainer: Party,
                                   override val linearId: UniqueIdentifier,
                                   override val fractionDigits: Int) : EvolvableTokenType() {

    override val maintainers: List<Party>
        get() = ImmutableList.of(maintainer)



}