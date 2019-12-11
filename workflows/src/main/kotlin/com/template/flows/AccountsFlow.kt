package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.contracts.StateAndRef
import com.r3.corda.lib.accounts.workflows.accountService
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount
import java.security.PublicKey
import java.util.UUID

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