package nl.tudelft.trustchain.offlineeuro.entity

import android.net.Uri
import nl.tudelft.trustchain.offlineeuro.communication.ICommunicationProtocol

class EUDIAuthManager(
    private val deepLinkCallback: ((Uri) -> (Unit)),
    private val onAuthSuccess: Runnable,
    private val onAuthFailure: Runnable,
    private val communicationProtocol: ICommunicationProtocol,
    private var status: String = "Not started"
) {

    fun authWith(deepLink: Uri) {
        status = "Started"
        deepLinkCallback(deepLink)
    }

    fun afterUserReturns() {
        status = "Waiting TTP verification"
        communicationProtocol.completeVerification()
    }

    fun authStatusUpdate(newStatus: String) {
        when (newStatus) {
            "Failed" -> {onAuthFailure.run(); status = newStatus}
            "Completed" -> {onAuthSuccess.run(); status = newStatus}
            else -> throw Exception("Unexpected auth status $newStatus")
        }
    }

    fun isPending() : Boolean {
        return (status == "Waiting TTP verification")
    }

    fun isNotStarted() : Boolean {
        return (status == "Not started")
    }

    fun isTerminated() : Boolean {
        return (status == "Failed" || status == "Completed")
    }
}
