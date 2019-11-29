package com.template.contracts

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import java.math.BigDecimal
// ************
// * Contract *
// ************
class Token_Contract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.TemplateContract"
    }

    interface Commands : CommandData {
       // class issueCommand : TypeOnlyCommandData(), Commands
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
//        val command = tx.commands.requireSingleCommand<Commands>()
//
//        when (command.value){
//            is Commands.issueCommand ->{
//
//            }
//        }
    }


}

    // Used to indicate the transaction's intent.
