# Kin core SDK for Android
Android library responsible for creating a new Stellar account and managing KIN balance and transactions.
![Kin Token](kin_android.png)

## Build

Add this to your module's `build.gradle` file.
```gradle
repositories {
    ...
    maven {
        url 'https://jitpack.io'
    }
}
...
dependencies {
    ...

    compile "com.github.kinfoundation:kin-core-android:<latest release>"
}
```
For latest release version go to https://github.com/kinfoundation/kin-core-android/releases

## Usage
### Connecting to a service provider
Create a new `KinClient` with two arguments: an android `Context` and a `ServiceProvider`. 

A `ServiceProvider` provides details of how to access the Stellar horizon end point.
The example below creates a `ServiceProvider` that will be used to connect to the main (production) Stellar 
network
```java
ServiceProvider horizonProvider =  
    new ServiceProvider("https://horizon.stellar.org", ServiceProvider.NETWORK_ID_MAIN));
KinClient kinClient = new KinClient(context, horizonProvider);
```

To connect to a test Stellar network use the following ServiceProvider:
```java
new ServiceProvider("https://horizon-testnet.stellar.org", ServiceProvider.NETWORK_ID_TEST)
``` 

### Creating and retrieving a KIN account
The first time you use `KinClient` you need to create a new account, 
the details of the created account will be securely stored on the device.
Multiple accounts can be created using `addAccount`.
```java
KinAccount account;
try {
    if (!kinClient.hasAccount()) {
        account = kinClient.addAccount();
    }
} catch (CreateAccountException e) {
    e.printStackTrace();
}
```


Calling `getAccount` with the existing account index, will retrieve the account stored on the device.
```java
if (kinClient.hasAccount()) {
    account = kinClient.getAccount(0);
}
``` 

You can delete your account from the device using `deleteAccount`, 
but beware! you will lose all your existing KIN if you do this.
```java
kinClient.deleteAccount(int index);
``` 

### Onboarding
Before an account can be used, it must be created on Stellar blockchain, by a different entity (Server) that has an account 
on Stellar network.
and then must the account must be activated, before it can receive or send KIN.


```java
Request<Void> activationRequest = account.activate()
activationRequest.run(new ResultCallback<Void>() {
    @Override
    public void onResult(Void result) {
        Log.d("example", "Account is activated");
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
});
``` 
For a complete example of this process, take a look at Sample App `OnBoarding` class.

### Public Address
Your account can be identified via it's public address. To retrieve the account public address use:
```java
account.getPublicAddress();
```


### Retrieving Balance
To retrieve the balance of your account in KIN call the `getBalance` method: 
```java
Request<Balance> balanceRequest = account.getBalance();
balanceRequest.run(new ResultCallback<Balance>() {

    @Override
    public void onResult(Balance result) {
        Log.d("example", "The balance is: " + result.value(2));
    }

    @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
});
```

### Transfering KIN to another account
To transfer KIN to another account, you need the public address of the account you want 
to transfer the KIN to. 

The following code will transfer 20 KIN to account "GDIRGGTBE3H4CUIHNIFZGUECGFQ5MBGIZTPWGUHPIEVOOHFHSCAGMEHO". 
```java
String toAddress = "GDIRGGTBE3H4CUIHNIFZGUECGFQ5MBGIZTPWGUHPIEVOOHFHSCAGMEHO";
BigDecimal amountInKin = new BigDecimal("20");


transactionRequest = account.sendTransaction(toAddress, amountInKin);
transactionRequest.run(new ResultCallback<TransactionId>() {

    @Override
        public void onResult(TransactionId result) {
            Log.d("example", "The transaction id: " + result.toString());
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
});
```

#####Memo
Arbitrary data can be added to a transfer operation using the memo parameter,
the memo is a `String` of up to 28 characters.

```java
String memo = "arbitrary data";
transactionRequest = account.sendTransaction(toAddress, amountInKin, memo);
transactionRequest.run(new ResultCallback<TransactionId>() {

    @Override
        public void onResult(TransactionId result) {
            Log.d("example", "The transaction id: " + result.toString());
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
});
```
### Watching Payments

Ongoing payments in KIN, from or to an account, can be observed. 
<br/>First, Create a `PaymentWatcher`
object:
```java
PaymentWatcher watcher = account.createPaymentWatcher();
```
For start watching, use `start` method, by providing listener of `WatcherListener<PaymentInfo>`:
```java
paymentWatcher.start(new WatcherListener<PaymentInfo>() {
    @Override
    public void onEvent(PaymentInfo payment) {
        Log.d("example", String
            .format("payment event, to = %s, from = %s, amount = %s", payment.sourcePublicKey(),
                payment.destinationPublicKey(), payment.amount().toPlainString());
    }
});
```
When you longer want to watch payment, stop the notifications and unregister the listener using `PaymentWatcher.stop()` method.

### Sync vs Async

Asynchronous requests are supported by our `Request` object. The `request.run()` method will perform the request on a serial 
background thread and notify success/failure using `ResultCallback` on the android main thread. 
In addition, `cancel(boolean)` method can be used to safely cancel requests and detach callbacks.


A synchronous version of these methods is also provided. Make sure you call them in a background thread.

```java
try {
    Balance balance = account.getBalanceSync();
} catch (OperationFailedException e) {
   // something went wrong - check the exception message
}

try {
    TransactionId transactionId = account.sendTransactionSync(toAddress, amountInKin);
} catch (OperationFailedException e){
    // something else went wrong - check the exception message
} 
```

### Sample Application 
For a more detailed example on how to use the library please take a look at our [Sample App](sample/).

## Testing

Both Unit tests and Android tests are provided, Android tests include integration tests that run on the Stellar test network, 
these tests are marked as `@LargeTest`, because they are time consuming, and depends on the network.


## Contributing
Please review our [CONTRIBUTING.md](CONTRIBUTING.md) guide before opening issues and pull requests.

## License
The kin-core-android library is licensed under [MIT license](LICENSE.md).
