package com.template.flows
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import net.corda.core.contracts.UniqueIdentifier
import com.template.flows.RealEstateEvolvableTokenType
import net.corda.core.identity.Party
import net.corda.core.flows.FlowException
import net.corda.core.transactions.SignedTransaction
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.template.contracts.Token_Contract
import net.corda.core.contracts.Amount
import java.math.BigDecimal
import net.corda.core.contracts.TransactionState
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import java.util.*
import com.r3.corda.lib.tokens.workflows.flows.rpc.*;
import com.r3.corda.lib.tokens.contracts.utilities.of
import com.r3.corda.lib.tokens.contracts.utilities.TokenUtilities

import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import net.corda.core.node.services.queryBy
import net.corda.core.contracts.StateAndRef

import net.corda.core.utilities.ProgressTracker
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.FlowSession
import com.r3.corda.lib.tokens.contracts.types.TokenPointer





@StartableByRPC
class CreateEvolvableFungibleTokenFlow(// valuation property of a house can change hence we are considering house as a evolvable asset
        private val valuation: BigDecimal) : FlowLogic<SignedTransaction>() {

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        //grab the notary
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        //create token type
        val evolvableTokenType = RealEstateEvolvableTokenType(valuation, ourIdentity,UniqueIdentifier(), 0)

        //warp it with transaction state specifying the notary
        val transactionState = TransactionState(evolvableTokenType,notary = notary)

        //call built in sub flow CreateEvolvableTokens. This can be called via rpc or in unit testing
        return subFlow(CreateEvolvableTokens(transactionState))

    }
}


@StartableByRPC
class IssueEvolvableFungibleTokenFlow(
                                    private  val tokenId : String,
                                    private  val quantity : Long,
                                    private  val holder : Party ) : FlowLogic<SignedTransaction>(){
    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {

        val uuid = UUID.fromString(tokenId)

        val querycriteria = QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(uuid),null,Vault.StateStatus.UNCONSUMED)

        val stateref = serviceHub.vaultService.queryBy(RealEstateEvolvableTokenType::class.java,querycriteria).states.get(0)
        val evolvableTokenType = stateref.state.data
        val tokenpointer = evolvableTokenType.toPointer(evolvableTokenType::class.java)
        val issuedTokenType = IssuedTokenType(ourIdentity,tokenpointer)
        val amount = Amount(quantity,issuedTokenType)



        val fungibleToken = FungibleToken(amount,holder,amount.token.tokenType.getAttachmentIdForGenericParam())

        return subFlow(IssueTokens(ImmutableList.of(fungibleToken)))

    }
}

@StartableByRPC
class MoveEvolvableFungibleTokenFlow( private  val tokenId : String,
                                      private  val quantity : Long,
                                      private  val holder : Party ) : FlowLogic<SignedTransaction>(){
    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val uuid = UUID.fromString(tokenId)
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null,ImmutableList.of(uuid),null,Vault.StateStatus.UNCONSUMED,null)

        val stateRef = serviceHub.vaultService.queryBy(RealEstateEvolvableTokenType::class.java,queryCriteria).states.get(0)

        val evolvableTokenType = stateRef.state.data

        val tokenpointer = evolvableTokenType.toPointer(evolvableTokenType::class.java)

        val partyAndAmount = PartyAndAmount(holder,quantity of tokenpointer)

        return subFlow(MoveFungibleTokens(partyAndAmount, listOf(holder)))
    }
}
