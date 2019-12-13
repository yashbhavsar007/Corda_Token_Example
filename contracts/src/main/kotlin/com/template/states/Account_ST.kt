package com.template.states


import net.corda.core.flows.*
import net.corda.core.identity.Party
import com.template.contracts.Account_Contract
import net.corda.core.contracts.*
import java.util.*
import net.corda.core.identity.AbstractParty

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
@BelongsToContract(Account_Contract::class)
class AccountState( val name: String,
                    val email: String,
                    val phone: Int,
                    val country: String,
                    val dob: Date,
                     val owner : Party,
                    val linearId: UUID) : ContractState {

//    override val participants get() = listOf()
    override val participants: List<AbstractParty> get() = listOf()

}