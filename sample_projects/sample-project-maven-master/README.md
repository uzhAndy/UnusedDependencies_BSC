# web3j Sample Project

This project provides a very simple starter application for working with web3j and the test 
Ethereum network (testnet) known as Rinkeby.

The demo deploys a *hello world* smart contract which contains a greeting message. The current 
greeting message can be read and modified in the smart contract.  

In order to run this demo, three pieces of information are required:

1. Ethereum client (or node) endpoint. The simplest thing to do is 
[request a free access token](https://infura.io/register.html) from Infura.
2. A wallet file. This can be generated using the web3j 
[command line tools](https://docs.web3j.io/command_line.html) by running the command:
3.  Some Ether. This can be requested from the [Rinkeby Faucet](https://www.rinkeby.io/#faucet).

- To create a wallet file, run the command:
```aidl
web3j wallet create
```

- You will be asked to secure your wallet with a password. You will have the option
of choosing a destination directory for your wallet file.
In the `Application.java` class under the package `org.web3j.sample`,
on line `66-67` replace the placeholders with your wallet file password and directory path.

- With the access token retrieved from Infura, on line `58` replace the placeholder 
in the url with your token. You can find this under the `Endpoint` section:

![Alt text](Artboard.png)

Once these details have been added to the main Application class, you'll be good to go.

### Run the project
- Make sure to be in the root directory
- First we need to create the smart contracts deriving from the solidity file `Greeter.sol` by running:
```aidl
mvn web3j:generate-sources
```
- This will create the Greeter and Mortal contract under the package `org.web3j.model`
- Clean and build the project, run the command:
```aidl
mvn install
```
This will also generate a jar file with all the dependencies which we will run once
it has been created.
- Run the `Main` method in `Application.java` by running 
```
mvn exec:java
```
- Alternatively, you can run the `main` method in `Application.java` in your chosen IDE, e.g. `IntelliJ`

## Functionality

This application demonstrates a number of core features of web3j and Ethereum:

- Connecting to a node on the Ethereum network
- Loading an Ethereum wallet file
- Sending Ether from one address to another
- Deploying a smart contract to the network
- Reading a value from the deployed smart contract
- Updating a value in the deployed smart contract
- Viewing an event logged by the smart contract


## Background

This application has purposefully been kept as simple as possible with the aim of demonstrating 
how to deploy and work with a smart contract on the decentralised Ethereum network.

If you require further background information about the above configuration items for the project
you can refer to the below.

For a more comprehensive overview, I recommend you read the 
[Java Magazine Article](https://web3j.io/articles/web3j%20article%20-%20Java%20Magazine%20JanuaryFebruary%202017.pdf), watch 
the 
[YouTube](https://youtube.com/watch?v=ea3miXs_P6Y) talk, and read all of the 
[documentation](https://docs.web3j.io).

### Smart contracts

The contained smart contract is based on the 
[Greeter contract example](https://www.ethereum.org/greeter), with the addition that the value 
stored in the Greeter can be modified.

The associated Java smart contract wrapper is named Greeter.

For more background on smart contracts, refer to the 
[smart contracts](https://docs.web3j.io/smart_contracts.html) section of the docs.


### Infura

If you don't want to sign up to Infura, and would like to run a node yourself, there are 
instructions in the [getting started](https://docs.web3j.io/getting_started.html#start-a-client) 
section of the docs.

### Testnets

For more information on the different Ethereum test networks (or testnets), refer 
to the 
[testnet overview](https://docs.web3j.io/transactions.html#ethereum-testnets) in the docs.

### Ether

For background on Ether, refer to the transactions 
[overview](https://docs.web3j.io/transactions.html#transactions) section of the docs.

### Wallets and transaction signing

For a more technical overview of working with wallet files, refer to the 
[Offline transaction signing](https://docs.web3j.io/transactions.html#offline-transaction-signing)
section of the docs.
 

## Viewing requests

A logback configuration is included in the project dependencies allowing the logging of protocol 
messages if required. To enable, simply change the following [line]() in the logback configuration
to read:

```xml
<logger name="org.web3j.protocol" level="DEBUG"/>
```
