package Server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {
    private String username;
    private int amount;
    private Transaction lastTransaction;
    @JsonIgnore
    private Boolean transactionFlag;
    private Lock accountLock = new ReentrantLock();

    @JsonCreator
    public BankAccount(@JsonProperty("username") String username,
                       @JsonProperty("amount") int amount,
                       @JsonProperty("lastTransaction") Transaction lastTransaction) {
        this.username = username;
        this.amount = amount;
        this.lastTransaction = lastTransaction;
        // Impostiamo transactionFlag su false di default
        this.transactionFlag = false;
    }
    @JsonIgnore
    public BankAccount(String username, int amount) {
        this.username=username;
        this.amount=amount;
        this.lastTransaction = null;
        this.transactionFlag = false;
    }

    // Getter e setter per username, amount e lastTransaction

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Transaction getLastTransaction() {
        return lastTransaction;
    }

    public void setLastTransaction(Transaction lastTransaction) {
        this.lastTransaction = lastTransaction;
    }

    @JsonIgnore
    public Boolean getTransactionFlag() {
        return transactionFlag;
    }

    public void setTransactionFlag(Boolean transactionFlag) {
        this.transactionFlag = transactionFlag;
    }

    public Lock getLock(){
        return accountLock;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "username='" + username + '\'' +
                ", amount=" + amount +
                ", lastTransaction=" + lastTransaction +
                ", transactionFlag=" + transactionFlag +
                '}';
    }
}

