package kin.core

import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import io.reactivex.Observable
import kin.core.IntegConsts.TEST_NETWORK_ID
import kin.core.IntegConsts.TEST_NETWORK_URL
import kin.core.exception.AccountNotActivatedException
import kin.core.exception.AccountNotFoundException
import kin.core.exception.InsufficientKinException
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.rules.ExpectedException
import org.stellar.sdk.Memo
import org.stellar.sdk.MemoText
import org.stellar.sdk.Server
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.fail

@Suppress("FunctionName")
class KinAccountIntegrationTest {
    private lateinit var kinClient: KinClient

    @Rule
    @JvmField
    val expectedEx: ExpectedException = ExpectedException.none()

    private inner class TestServiceProvider internal constructor() : ServiceProvider(TEST_NETWORK_URL, TEST_NETWORK_ID) {

        override fun getIssuerAccountId(): String {
            return fakeKinIssuer.accountId
        }
    }

    @Before
    @Throws(IOException::class)
    fun setup() {
        val serviceProvider = TestServiceProvider()
        kinClient = KinClient(InstrumentationRegistry.getTargetContext(), serviceProvider)
        kinClient.clearAllAccounts()
    }

    @After
    fun teardown() {
        if (::kinClient.isInitialized) {
            kinClient.clearAllAccounts()
        }
    }

    @Test
    @LargeTest
    fun getBalanceSync_AccountNotCreated_AccountNotFoundException() {
        val kinAccount = kinClient.addAccount()

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccount.publicAddress.orEmpty())
        kinAccount.balanceSync
    }

    @Test
    @LargeTest
    fun getStatusSync_AccountNotCreated_StatusNotCreated() {
        val kinAccount = kinClient.addAccount()

        val status = kinAccount.statusSync
        assertThat(status, equalTo(AccountStatus.NOT_CREATED))
    }

    @Test
    @LargeTest
    fun getBalanceSync_AccountNotActivated_AccountNotActivatedException() {
        val kinAccount = kinClient.addAccount()
        fakeKinIssuer.createAccount(kinAccount.publicAddress.orEmpty())

        expectedEx.expect(AccountNotActivatedException::class.java)
        expectedEx.expectMessage(kinAccount.publicAddress.orEmpty())
        kinAccount.balanceSync
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun getStatusSync_AccountNotActivated_StatusNotActivated() {
        val kinAccount = kinClient.addAccount()
        fakeKinIssuer.createAccount(kinAccount.publicAddress.orEmpty())

        val status = kinAccount.statusSync
        assertThat(status, equalTo(AccountStatus.NOT_ACTIVATED))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun getBalanceSync_FundedAccount_GotBalance() {
        val kinAccount = kinClient.addAccount()
        fakeKinIssuer.createAccount(kinAccount.publicAddress.orEmpty())

        kinAccount.activateSync()
        assertThat(kinAccount.balanceSync.value(), equalTo(BigDecimal("0.0000000")))

        fakeKinIssuer.fundWithKin(kinAccount.publicAddress.orEmpty(), "3.1415926")
        assertThat(kinAccount.balanceSync.value(), equalTo(BigDecimal("3.1415926")))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun getStatusSync_CreateAndActivateAccount_StatusActivated() {
        val kinAccount = kinClient.addAccount()
        fakeKinIssuer.createAccount(kinAccount.publicAddress.orEmpty())

        kinAccount.activateSync()
        assertThat(kinAccount.balanceSync.value(), equalTo(BigDecimal("0.0000000")))
        val status = kinAccount.statusSync
        assertThat(status, equalTo(AccountStatus.ACTIVATED))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun activateSync_AccountNotCreated_AccountNotFoundException() {
        val kinAccount = kinClient.addAccount()

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccount.publicAddress.orEmpty())
        kinAccount.activateSync()
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_WithMemo() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(senderFundAmount = 100)

        val expectedMemo = "fake memo"

        val latch = CountDownLatch(1)
        val listenerRegistration = kinAccountReceiver.blockchainEvents()
                .addPaymentListener { _ -> latch.countDown() }

        val transactionId = kinAccountSender
                .sendTransactionSync(kinAccountReceiver.publicAddress!!, BigDecimal("21.123"),
                        expectedMemo)
        assertThat(kinAccountSender.balanceSync.value(), equalTo(BigDecimal("78.8770000")))
        assertThat(kinAccountReceiver.balanceSync.value(), equalTo(BigDecimal("21.1230000")))

        latch.await(10, TimeUnit.SECONDS)
        listenerRegistration.remove()

        val server = Server(TEST_NETWORK_URL)
        val transaction = server.transactions().transaction(transactionId.id())
        val actualMemo = transaction.memo
        assertThat<Memo>(actualMemo, `is`<Memo>(instanceOf<Memo>(MemoText::class.java)))
        assertThat((actualMemo as MemoText).text, equalTo(expectedMemo))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_ReceiverAccountNotCreated_AccountNotFoundException() {
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()
        fakeKinIssuer.createAccount(kinAccountSender.publicAddress.orEmpty())

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccountReceiver.publicAddress)
        kinAccountSender
                .sendTransactionSync(kinAccountReceiver.publicAddress!!, BigDecimal("21.123"))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_SenderAccountNotCreated_AccountNotFoundException() {
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()
        fakeKinIssuer.createAccount(kinAccountReceiver.publicAddress.orEmpty())
        kinAccountReceiver.activateSync()

        expectedEx.expect(AccountNotFoundException::class.java)
        expectedEx.expectMessage(kinAccountSender.publicAddress)
        kinAccountSender
                .sendTransactionSync(kinAccountReceiver.publicAddress!!, BigDecimal("21.123"))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_ReceiverAccountNotActivated_AccountNotFoundException() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(activateSender = true, activateReceiver = false)

        expectedEx.expect(AccountNotActivatedException::class.java)
        expectedEx.expectMessage(kinAccountReceiver.publicAddress)
        kinAccountSender
                .sendTransactionSync(kinAccountReceiver.publicAddress!!, BigDecimal("21.123"))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_SenderAccountNotActivated_AccountNotFoundException() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(activateSender = false, activateReceiver = true)

        fakeKinIssuer.createAccount(kinAccountSender.publicAddress.orEmpty())
        fakeKinIssuer.createAccount(kinAccountReceiver.publicAddress.orEmpty())

        kinAccountReceiver.activateSync()

        expectedEx.expect(AccountNotActivatedException::class.java)
        expectedEx.expectMessage(kinAccountSender.publicAddress)
        kinAccountSender
                .sendTransactionSync(kinAccountReceiver.publicAddress!!, BigDecimal("21.123"))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun createPaymentListener_ListenToReceiver_PaymentEvent() {
        listenToPayments(false)
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun createPaymentListener_ListenToSender_PaymentEvent() {
        listenToPayments(true)
    }

    @Throws(Exception::class)
    private fun listenToPayments(sender: Boolean) {
        //create and sets 2 accounts (receiver/sender), fund one account, and then
        //send transaction from the funded account to the other, observe this transaction using listeners
        val fundingAmount = BigDecimal("100")
        val transactionAmount = BigDecimal("21.123")

        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(true, true, 0, 0)

        //register listeners for testing
        val actualPaymentsResults = ArrayList<PaymentInfo>()
        val actualBalanceResults = ArrayList<Balance>()
        val accountToListen = if (sender) kinAccountSender else kinAccountReceiver

        val eventsCount = if (sender) 4 else 2 ///in case of observing the sender we'll get 2 events (1 for funding 1 for the
        //transaction) in case of receiver - only 1 event. multiply by 2, as we 2 listeners (balance and payment)
        val latch = CountDownLatch(eventsCount)
        accountToListen.blockchainEvents().addPaymentListener { data ->
            actualPaymentsResults.add(data)
            latch.countDown()
        }
        accountToListen.blockchainEvents().addBalanceListener { data ->
            actualBalanceResults.add(data)
            latch.countDown()
        }

        //send the transaction we want to observe
        fakeKinIssuer.fundWithKin(kinAccountSender.publicAddress.orEmpty(), "100")
        val expectedMemo = "memo"
        val expectedTransactionId = kinAccountSender
                .sendTransactionSync(kinAccountReceiver.publicAddress!!, transactionAmount, expectedMemo)

        //verify data notified by listeners
        val transactionIndex = if (sender) 1 else 0 //in case of observing the sender we'll get 2 events (1 for funding 1 for the
        //transaction) in case of receiver - only 1 event
        latch.await(10, TimeUnit.SECONDS)
        val paymentInfo = actualPaymentsResults[transactionIndex]
        assertThat(paymentInfo.amount(), equalTo(transactionAmount))
        assertThat(paymentInfo.destinationPublicKey(), equalTo(kinAccountReceiver.publicAddress))
        assertThat(paymentInfo.sourcePublicKey(), equalTo(kinAccountSender.publicAddress))
        assertThat(paymentInfo.memo(), equalTo(expectedMemo))
        assertThat(paymentInfo.hash().id(), equalTo(expectedTransactionId.id()))

        val balance = actualBalanceResults[transactionIndex]
        assertThat(balance.value(),
                equalTo(if (sender) fundingAmount.subtract(transactionAmount) else transactionAmount))
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun createPaymentListener_RemoveListener_NoEvents() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts(senderFundAmount = 100)

        val latch = CountDownLatch(1)
        val blockchainEvents = kinAccountReceiver.blockchainEvents()
        val listenerRegistration = blockchainEvents
                .addPaymentListener {
                    fail("should not get eny event!")
                }
        listenerRegistration.remove()

        kinAccountSender
                .sendTransactionSync(kinAccountReceiver.publicAddress.orEmpty(), BigDecimal("21.123"), null)

        latch.await(15, TimeUnit.SECONDS)
    }

    @Test
    @LargeTest
    @Throws(Exception::class)
    fun sendTransaction_NotEnoughKin_TransactionFailedException() {
        val (kinAccountSender, kinAccountReceiver) = onboardAccounts()

        expectedEx.expect(InsufficientKinException::class.java)
        kinAccountSender
                .sendTransactionSync(kinAccountReceiver.publicAddress!!, BigDecimal("21.123"))
    }

    private fun runTasksInParralelAndWait(vararg tasks: () -> (Unit)) {
        //use rx-java to parralelize tasks that are not dependant on each other
        Observable.zip(tasks.map { taskObservable(it) }) { objects -> objects[0] }
                .ignoreElements()
                .blockingAwait()
    }

    private fun taskObservable(task: () -> (Unit)): Observable<Any> {
        return Observable.fromCallable {
            task.invoke()
            Any()
        }
    }

    private fun onboardAccounts(activateSender: Boolean = true, activateReceiver: Boolean = true, senderFundAmount: Int = 0,
                                receiverFundAmount: Int = 0): Pair<KinAccount, KinAccount> {
        val kinAccountSender = kinClient.addAccount()
        val kinAccountReceiver = kinClient.addAccount()
        runTasksInParralelAndWait(
                { onboardSingleAccount(kinAccountSender, activateSender, senderFundAmount) },
                { onboardSingleAccount(kinAccountReceiver, activateReceiver, receiverFundAmount) })
        return Pair(kinAccountSender, kinAccountReceiver)
    }

    private fun onboardSingleAccount(account: KinAccount, shouldActivate: Boolean, fundAmount: Int) {
        fakeKinIssuer.createAccount(account.publicAddress.orEmpty())
        if (shouldActivate) {
            account.activateSync()
        }
        if (fundAmount > 0) {
            fakeKinIssuer.fundWithKin(account.publicAddress.orEmpty(), fundAmount.toString())
        }
    }

    companion object {

        private lateinit var fakeKinIssuer: FakeKinIssuer

        @BeforeClass
        @JvmStatic
        @Throws(IOException::class)
        fun setupKinIssuer() {
            fakeKinIssuer = FakeKinIssuer()
        }
    }
}
