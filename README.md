![Kin Token](kin_android.png)
# Kin core SDK for Android
[![Build Status](https://travis-ci.org/kinecosystem/kin-core-android.svg?branch=dev)](https://travis-ci.org/kinecosystem/kin-core-android)
[![codecov](https://codecov.io/gh/kinecosystem/kin-core-android/branch/dev/graph/badge.svg)](https://codecov.io/gh/kinecosystem/kin-core-android)

Android library responsible for creating a new Stellar account and managing KIN balance and transactions.

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

    compile "com.github.kinecosystem:kin-core-android:<latest release>"
}
```
For latest release version go to https://github.com/kinecosystem/kin-core-android/releases

## Usage
### Connecting to a service provider
Create a new `KinClient` with two arguments: an android `Context` and a `ServiceProvider`. 

A `ServiceProvider` provides details of how to access the Stellar horizon end point.
The example below creates a `ServiceProvider` that will be used to connect to the main (production) Stellar 
network
```java
ServiceProvider horizonProvider =  
    new ServiceProvider("https://horizon.stellar.org", ServiceProvider.NETWORK_ID_MAIN);
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
A first step before an account can be used, is to create the account on Stellar blockchain and fund it with native Stellar
asset for fees purposes, this funding should be done by the digital service servers.  
When working against KIN test network, the Fee Faucet Service can be used for this purpose:

```java
Request request = new Request.Builder()
            .url("http://friendbot-kik.kininfrastructure.com/?addr=" + account.getPublicAddress())
            .get()
            .build();
okHttpClient.newCall(request)
    .enqueue(new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            if (response.code() == 200) {
                //account was created successfully, continue to the second phase - account activation
            }
        }
    });
```

The second step is to activate this account on the client side, using `activate` method. The account will not be able to receive or send KIN before activation.


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
 
Third, optional step is to fund the created account with KIN.  
When working against KIN test network, the KIN Faucet Service can be used for this purpose:
```java
Request request = new Request.Builder()
            .url("http://159.65.84.173:5000/fund?account=" + account.getPublicAddress() + "&amount=" + FUND_KIN_AMOUNT)
            .get()
            .build();
okHttpClient.newCall(request)
    .enqueue(new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response)
            throws IOException {
            if (response.code() == 200) {
              //Account was funded with KIN succesfully
            }
        }
    });
```

For a complete example of this process, take a look at Sample App `OnBoarding` class.

#### Query Account Status

Current account status on the blockchain can be queried using `getStatus` method,  
status will be one of the following 3 options:
* `AccountStatus.NOT_CREATED` - Account is not created yet on the blockchain network.
* `AccountStatus.NOT_ACTIVATED` - Account was created but not activated yet, the account cannot send or receive KIN yet.
* `AccountStatus.ACTIVATED` - Account was created and activated, account can send and receive KIN.

```java
Request<Integer> statusRequest = account.getStatus();
statusRequest.run(new ResultCallback<Integer>() {
    @Override
    public void onResult(Integer result) {
        switch (result) {
            case AccountStatus.ACTIVATED:
                //you're good to go!!!
                break;
            case AccountStatus.NOT_ACTIVATED:
                //activate account using account.activate() for sending/receiving KIN
                break;
            case AccountStatus.NOT_CREATED:
                //first create an account on the blockchain, second activate the account using account.activate()
                break;
        }
    }

    @Override
    public void onError(Exception e) {

    }
});
```

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

#### Memo
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
### Listening to payments

Ongoing payments in KIN, from or to an account, can be observed,
by adding payment listener using `BlockchainEvents`:
```java
ListenerRegistration listenerRegistration = account.blockchainEvents()
            .addPaymentListener(new EventListener<PaymentInfo>() {
                @Override
                public void onEvent(PaymentInfo payment) {
                    Log.d("example", String
                        .format("payment event, to = %s, from = %s, amount = %s", payment.sourcePublicKey(),
                            payment.destinationPublicKey(), payment.amount().toPlainString());
                }
            });
```
For unregister the listener use `listenerRegistration.remove()` method.

### Listening to account creation
Account creation on the blockchain network, can be observed, by adding create account listener using `BlockchainEvents`:

```java
ListenerRegistration listenerRegistration = account.blockchainEvents()
            .addAccountCreationListener(new EventListener<Void>() {
                @Override
                public void onEvent(Void result) {
                    Log.d("example", "Account has created.);                     
                }
            });
```
For unregister the listener use `listenerRegistration.remove()` method.

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
The kin-core-android library is licensed under [Kin Ecosystem SDK License](LICENSE.pdf).
