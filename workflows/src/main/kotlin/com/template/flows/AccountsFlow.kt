package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount
import net.corda.core.contracts.StateAndRef
import com.r3.corda.lib.accounts.workflows.accountService
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount
import com.template.contracts.Account_Contract
import com.template.states.AccountState
import net.corda.core.contracts.Command
import net.corda.core.contracts.TransactionState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import java.security.PublicKey
import java.util.*
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.transactions.SignedTransaction
import net.corda.core.contracts.requireThat

@InitiatingFlow
@StartableByRPC
class AccountGet(private val name: String) : FlowLogic<AccountInfo>(){

    @Suspendable
    @Throws(FlowException::class)
    override fun call() : AccountInfo {
//        fun accountInfo(name: String): List<StateAndRef<AccountInfo>>?
        val myAccount = accountService.accountInfo(name).single().state.data


        return myAccount
    }
}

@InitiatingFlow
@StartableByRPC
class AccountFlow(private val name: String) : FlowLogic<AccountInfo>(){

    @Suspendable
    @Throws(FlowException::class)
    override fun call() : AccountInfo{

        val newAccount = subFlow(CreateAccount(name))
        //accountService.createAccount(name = name).toCompletableFuture().getOrThrow()
        val acct = newAccount.state.data
        return acct
    }
}


@InitiatingFlow
@StartableByRPC
class AccountPub(private val name: String) : FlowLogic<List<PublicKey>>(){

    @Suspendable
    @Throws(FlowException::class)
    override fun call(): List<PublicKey> {
        val myAccount = accountService.accountInfo(name).single().state.data

        if(accountService.accountKeys(myAccount.identifier.id).isEmpty()) {
            val keyGen = subFlow(RequestKeyForAccount(myAccount))

        }

        val Account = accountService.accountKeys(myAccount.identifier.id)

        return Account


    }
}


@InitiatingFlow
@StartableByRPC
class AccountRegister(private val name: String,
                      private  val email: String,
                      private  val phone: Int,
                      private val country: String,
                      private val dob: Date) : FlowLogic<TransactionBuilder>(){

    companion object {
        object GENERATING_TRANSACTION : Step("Generating transaction.")
        object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                FINALISING_TRANSACTION
        )

    }
    override val progressTracker = tracker()

    @Suspendable
    override fun call(): TransactionBuilder {
//        val AcReg = subFlow(CreateAccount(name))
//        val myAccount = accountService.accountInfo(name).single().state.data
//        progressTracker.currentStep = GENERATING_TRANSACTION
//
//        if(accountService.accountKeys(myAccount.identifier.id).isEmpty()) {
//            val keyGen = subFlow(RequestKeyForAccount(myAccount))
//        }
        val notary = serviceHub.networkMapCache.notaryIdentities[0]

        val opState = AccountState(name,email,phone,country,dob,ourIdentity,UniqueIdentifier(name).id)
      //  val CheckAccount = Command(Account_Contract.Commands.Check_Account(), listOf(ourIdentity.owningKey))

        val cmd2 = Command(Account_Contract.Commands.Check_Account(), listOf(ourIdentity.owningKey))
        val txBuilder = TransactionBuilder(notary = notary)
                .addOutputState(opState)
                .addCommand(cmd2)

        txBuilder.verify(serviceHub)

//        val signedTx = serviceHub.signInitialTransaction(txBuilder)
//        val flowsession = initiateFlow(ourIdentity)
//
//      //  val allSignedTransaction = subFlow(CollectSignaturesFlow(signedTx , listOf(flowsession),CollectSignaturesFlow.tracker()))
//        val tr = subFlow(FinalityFlow(signedTx, listOf(flowsession)))
//        progressTracker.currentStep = FINALISING_TRANSACTION

        return txBuilder

    }

}

//
//@InitiatedBy(AccountRegister::class)
//class AccountRegisterResponder(private val flowsession : FlowSession) : FlowLogic<Unit>() {
//
//    @Suspendable
//    override fun call() {
//        val signedTransactionFlow = object : SignTransactionFlow(flowsession){
//            override fun checkTransaction(stx: SignedTransaction) =
//                    requireThat {
//                        val output = stx.tx.outputs.single().data
//                        "This must be an IOU transaction " using (output is AccountState)
//                    }
//
//        }
//        val expectedTxId = subFlow(signedTransactionFlow).id
//        subFlow(ReceiveFinalityFlow(flowsession , expectedTxId))
//    }
//}
