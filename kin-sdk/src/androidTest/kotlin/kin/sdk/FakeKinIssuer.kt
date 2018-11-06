package kin.sdk


import android.util.Log
import kin.base.Asset
import kin.base.KeyPair
import kin.base.PaymentOperation
import kin.base.Server
import kin.base.Transaction
import kin.sdk.IntegConsts.TEST_NETWORK_URL
import kin.sdk.IntegConsts.URL_CREATE_ACCOUNT
import okhttp3.OkHttpClient
import org.junit.Assert.assertTrue
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Fake issuer for integration test, support creating and funding accounts on stellar test net
 */
internal class FakeKinIssuer @Throws(IOException::class)
constructor() {

    private val server: Server = Server(TEST_NETWORK_URL, TIMEOUT_SEC, TimeUnit.SECONDS)
    private val issuerKeyPair: KeyPair = KeyPair.random()
    private val kinAsset: Asset = Asset.createNonNativeAsset("KIN", issuerKeyPair)

    val accountId: String = issuerKeyPair.accountId

    init {
        createAndFundWithLumens(issuerKeyPair.accountId)
    }

    @Throws(IOException::class)
    private fun createAndFundWithLumens(accountId: String) {
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
                .url(URL_CREATE_ACCOUNT + accountId).build()
        client.newCall(request).execute()?.let {
            if (it.body() == null || it.code() != 200) {
                Log.d("test", "createAndFundWithLumens error, error code = ${it.code()}, message = ${it.message()}")
            }
        }
    }

    @Throws(Exception::class)
    fun createAccount(destinationAccount: String) {
        createAndFundWithLumens(destinationAccount)
    }

    @Throws(Exception::class)
    fun fundWithKin(destinationAccount: String, amount: String) {
        val destinationKeyPair = KeyPair.fromAccountId(destinationAccount)
        val issuerAccountResponse = server.accounts().account(issuerKeyPair)
        val paymentOperation = PaymentOperation.Builder(destinationKeyPair, kinAsset, amount)
                .build()
        val transaction = Transaction.Builder(issuerAccountResponse)
                .addOperation(paymentOperation)
                .build()
        transaction.sign(issuerKeyPair)
        val response = server.submitTransaction(transaction)
        if (!response.isSuccess) {
            throw Utils.createTransactionException(response)
        }
        assertTrue(response.isSuccess)
    }
}

private val TIMEOUT_SEC = 20
