package com.template.flows


import co.paralleluniverse.fibers.Suspendable
import net.corda.client.rpc.CordaRPCClient

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.NetworkHostAndPort.Companion.parse
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import net.corda.core.serialization.serialize
import com.google.gson.Gson
const val TEST_URL = "http://dummy.restapiexample.com/api/v1/employees"

@InitiatingFlow
@StartableByRPC
class HttpCallFlow(private val case: String, private val flag : Boolean) : FlowLogic<String>() {
    override val progressTracker: ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val httpRequest = Request.Builder().url(TEST_URL).build()

        // BE CAREFUL when making HTTP calls in flows:
        // 1. The request must be executed in a BLOCKING way. Flows don't
        //    currently support suspending to await an HTTP call's response
        // 2. The request must be idempotent. If the flow fails and has to
        //    restart from a checkpoint, the request will also be replayed
        val httpResponse = OkHttpClient().newCall(httpRequest).execute()

        val js = JSONObject(httpResponse)
        val res = httpResponse.body()?.string()
        //val responseObject = json.getJSONObject("body")

        val test = ("{'winCondition':'HIGH_SCORE',"
                + "'name':'Bowling',"
                + "'round':4,"
                + "'lastSaved':1367702411696,"
                + "'dateStarted':1367702378785,"
                + "'players':["
                + "{'name':'" + "yash" + "','history':[10,8,6,7,8],'color':-13388315,'total':39},"
                + "{'name':'" + "yash2" + "','history':[6,10,5,10,10],'color':-48060,'total':41}"
                + "]}")

        val json = JSONObject(test)

//     val responseObject2 = json.getJSONObject("name")
        // val responseObject3 = json.getJSONObject("winCondition")
        if(flag){
            val s = json.getString(case)
            val ln = json.length()
            return " $test OBJ2  length is: $ln and s is: $s"//$responseObject2  OBJ3 $responseObject3
        }
        else{
            val s = json.getJSONArray(case)
            val ln = json.length()
            return " $test OBJ2  length is: $ln and s is: $s"//$responseObject2  OBJ3 $responseObject3

        }

    }
}


@InitiatingFlow
@StartableByRPC
class HttpCallFlow2 : FlowLogic<String>() {
    override val progressTracker: ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val httpRequest = Request.Builder().url(TEST_URL).build()

        // BE CAREFUL when making HTTP calls in flows:
        // 1. The request must be executed in a BLOCKING way. Flows don't
        //    currently support suspending to await an HTTP call's response
        // 2. The request must be idempotent. If the flow fails and has to
        //    restart from a checkpoint, the request will also be replayed
        val httpResponse = OkHttpClient().newCall(httpRequest).execute()

        val js = JSONObject(httpResponse)
//        val j = js.getJSONArray(null)
        val res = httpResponse.body().string()
        return res
    }
}