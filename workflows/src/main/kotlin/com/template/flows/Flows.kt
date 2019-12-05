package com.template.flows
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import net.corda.core.identity.Party
import net.corda.core.flows.FlowException
import net.corda.core.transactions.SignedTransaction
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import co.paralleluniverse.fibers.Suspendable
import com.google.common.collect.ImmutableList
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import java.math.BigDecimal
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import java.util.*
import com.r3.corda.lib.tokens.workflows.flows.rpc.*;
import com.r3.corda.lib.tokens.contracts.utilities.of
import com.r3.corda.lib.tokens.contracts.utilities.getAttachmentIdForGenericParam
import com.r3.corda.lib.tokens.workflows.types.PartyAndAmount
import com.template.states.TestTokenType
import net.corda.core.contracts.*
import net.corda.core.flows.InitiatingFlow

@InitiatingFlow
@StartableByRPC
class CreateEvolvableFungibleTokenFlow(// valuation property of a house can change hence we are considering house as a evolvable asset
        private val valuation: BigDecimal) : FlowLogic<SignedTransaction>() {

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        //grab the notary
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        requireThat { "Valuation must not be zero!" using (valuation != BigDecimal(0)) }
        requireThat { "The Valuation must be positive" using (valuation > BigDecimal(0)) }

        //create token type
        val evolvableTokenType = TestTokenType(valuation, ourIdentity,UniqueIdentifier(), 0)

        //warp it with transaction state specifying the notary
        val transactionState = TransactionState(evolvableTokenType,notary = notary)

        //call built in sub flow CreateEvolvableTokens. This can be called via rpc or in unit testing
        return subFlow(CreateEvolvableTokens(transactionState))

    }
}

@InitiatingFlow
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

        val stateref = serviceHub.vaultService.queryBy(TestTokenType::class.java,querycriteria).states.get(0)
        val evolvableTokenType = stateref.state.data
        val tokenpointer = evolvableTokenType.toPointer(evolvableTokenType::class.java)
        val issuedTokenType = IssuedTokenType(ourIdentity,tokenpointer)
        val amount = Amount(quantity,issuedTokenType)



        val fungibleToken = FungibleToken(amount,holder,amount.token.tokenType.getAttachmentIdForGenericParam())

        return subFlow(IssueTokens(ImmutableList.of(fungibleToken)))

    }
}

@InitiatingFlow
@StartableByRPC
class MoveEvolvableFungibleTokenFlow( private  val tokenId : String,
                                      private  val quantity : Long,
                                      private  val holder : Party ) : FlowLogic<SignedTransaction>(){
    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val uuid = UUID.fromString(tokenId)
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null,ImmutableList.of(uuid),null,Vault.StateStatus.UNCONSUMED,null)

        val stateRef = serviceHub.vaultService.queryBy(TestTokenType::class.java,queryCriteria).states.get(0)

        val evolvableTokenType = stateRef.state.data

        val tokenpointer = evolvableTokenType.toPointer(evolvableTokenType::class.java)

        val partyAndAmount = PartyAndAmount(holder,quantity of tokenpointer)

        return subFlow(MoveFungibleTokens(partyAndAmount, listOf(holder)))
    }
}

@InitiatingFlow
@StartableByRPC
class RedeemEvolvableFungibleTokenFlow(private  val tokenId : String,
                                       private  val quantity : Long,
                                       private  val issuer : Party ) : FlowLogic<SignedTransaction>(){

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): SignedTransaction {
        val uuid = UUID.fromString(tokenId)
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null,ImmutableList.of(uuid),null,Vault.StateStatus.UNCONSUMED,null)

        val stateRef = serviceHub.vaultService.queryBy(TestTokenType::class.java,queryCriteria).states.get(0)

        val evolvableTokenType = stateRef.state.data

        val tokenPointer = evolvableTokenType.toPointer(evolvableTokenType::class.java)

        return subFlow(RedeemFungibleTokens(quantity of tokenPointer , issuer))
    }
}









