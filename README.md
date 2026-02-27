# Concurrent Banking Service

Java 17 client-server application for managing concurrent bank account transactions.

## Overview
Built for the Operating Systems Lab course at University of Bologna (A.Y. 2022-23).
The system simulates a banking backend where multiple clients connect simultaneously
and perform operations on shared accounts, with full concurrency control.

## Tech Stack
- Java 17
- TCP Sockets
- Multithreading
- Mutex-based mutual exclusion (synchronized blocks)
- MVC pattern

## Features
- Multi-client server with thread-per-client architecture
- Transaction isolation: each account participates in one transaction at a time
- CLI client with commands: `list`, `open`, `transfer`, `close`
- Error handling for edge cases and connection drops

## Requirements
- Java 17
- Maven 3.x

## Build
```bash
mvn compile
Run
Start the server:

bash
mvn exec:java -Dexec.mainClass="Server" -Dexec.args="9000"
Start the client:

bash
mvn exec:java -Dexec.mainClass="Client" -Dexec.args="127.0.0.1 9000"
If exec:java is not available, add the exec-maven-plugin to your pom.xml
or run directly with java -cp target/SO_project-1.0-SNAPSHOT.jar Server 9000

Available Client Commands
Command	Description
list	List all accounts with balance and last transaction
open <Account> <Amount>	Create a new account with initial balance
transfer <Amount> <Account1> <Account2>	Transfer money between two accounts
transfer_i <Account1> <Account2>	Start an interactive transfer session
quit	Disconnect the client
Interactive session commands:

:move <Amount> — move money within the session

:end — close the session and release accounts
```
