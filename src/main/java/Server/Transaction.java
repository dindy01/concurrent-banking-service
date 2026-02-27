package Server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transaction {
    private String sender;
    private String receiver;
    private int amount;

    @JsonCreator
    public Transaction(@JsonProperty("sender") String sender,
                       @JsonProperty("receiver") String receiver,
                       @JsonProperty("amount") int amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    // Getter e setter per sender, receiver e amount

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", amount=" + amount +
                '}';
    }
}

