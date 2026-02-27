package Server;

import java.io.*;
import java.util.List;
import java.util.concurrent.locks.Lock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Bank {

    private List<BankAccount> bankAccountList;

    public Bank() {
        this.bankAccountList = readBankAccountsFromFile();
    }

    public synchronized void writeBankAccountsToFile() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(new File("src/main/java/Server/BankAccounts.json"), bankAccountList);
            System.out.println("Lista di account bancari scritta su file BankAccounts.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<BankAccount> readBankAccountsFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        List<BankAccount> bankAccounts = null;
        try {
            bankAccounts = mapper.readValue(new File("src/main/java/Server/BankAccounts.json"), new TypeReference<List<BankAccount>>(){});
            System.out.println("List of bank accounts written on file BankAccounts.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bankAccounts;
    }

    // Cerca un account per ID
    public BankAccount findAccountByUsername(String id) {
        for (BankAccount account : bankAccountList){
            if (account.getUsername().equals(id)){
                return account;
            }
        }
        return null;
    }

    public synchronized String addBankAccount(BankAccount bankAccount) {
        if (findAccountByUsername(bankAccount.getUsername())!=null) {
            throw new RuntimeException("Account ID already existing  !");
        }
        bankAccountList.add(bankAccount);
        writeBankAccountsToFile();
        return "Account created successfully.";
    }

    // Ottiene un elenco di tutti gli account
    public synchronized String getAccountList() {
        StringBuilder result = new StringBuilder();

        try {
            for(BankAccount account : bankAccountList){
                account.getLock().lock();
            }
            if (!bankAccountList.isEmpty()) {
                for (int i = 0; i < bankAccountList.size(); i++) {
                    if (bankAccountList.get(i).getLastTransaction() != null) {
                        result.append(bankAccountList.get(i).getUsername() + " balance: " + bankAccountList.get(i).getAmount() + "\nLast transaction: " + bankAccountList.get(i).getLastTransaction() + "\n");
                    } else {
                        result.append(bankAccountList.get(i).getUsername() + " balance: " + bankAccountList.get(i).getAmount() + "\nAccount has no transactions.\n");
                    }
                }
            }
        }finally{
            for(BankAccount account : bankAccountList){
                account.getLock().unlock();
            }

        }
        return result.toString();
    }

    public boolean transferAmount(BankAccount sender, BankAccount receiver, Integer amount) {
        Lock senderLock = sender.getLock();
        Lock receiverLock = receiver.getLock();

        try {
            senderLock.lock();
            receiverLock.lock();

            if (amount <= 0 || sender.getAmount() < amount) {
                throw new RuntimeException("Impossible to proceed: invalid amount");
            }
            receiver.setAmount(receiver.getAmount() + amount);
            sender.setAmount(sender.getAmount() - amount);
            Transaction transaction = new Transaction(sender.getUsername(), receiver.getUsername(), amount);
            sender.setLastTransaction(transaction);
            receiver.setLastTransaction(transaction);
            writeBankAccountsToFile();
            return true;
        } finally {
            senderLock.unlock();
            receiverLock.unlock();
        }
    }

        /*   VERSIONE PRECEDENTE
        while (true) {
            if (flagCheckTrue(sender,receiver)) {
                if (amount<=0 || sender.getAmount() < amount) {
                    flagCheckFalse(sender,receiver);
                    throw new RuntimeException("Impossible to proceed: invalid amount");
                }
                receiver.setAmount(receiver.getAmount() + amount);
                sender.setAmount(sender.getAmount() - amount);
                Transaction transaction = new Transaction(sender.getUsername(), receiver.getUsername(), amount);
                sender.setLastTransaction(transaction);
                receiver.setLastTransaction(transaction);
                writeBankAccountsToFile();
                flagCheckFalse(sender,receiver);
                return true;
            }
        }
    }*/

    public boolean transfer_iAmount(BankAccount sender, BankAccount receiver, String amountStr){
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            return false;
        }

        if (amount <= 0 || sender.getAmount() < amount) {
            return false;
        }
        receiver.setAmount(receiver.getAmount() + amount);
        sender.setAmount(sender.getAmount() - amount);
        Transaction transaction = new Transaction(sender.getUsername(), receiver.getUsername(), amount);
        sender.setLastTransaction(transaction);
        receiver.setLastTransaction(transaction);
        writeBankAccountsToFile();
        return true;
    }
        /*  VERSIONE PRECEDENTE
        int amount;
        try{
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            return false;
        }
        while (true) {
            if (flagCheckTrue(sender, receiver)) {
                if (amount <= 0 || sender.getAmount() < amount) {
                    flagCheckFalse(sender,receiver);
                    return false;
                }
                receiver.setAmount(receiver.getAmount() + amount);
                sender.setAmount(sender.getAmount() - amount);
                Transaction transaction = new Transaction(sender.getUsername(), receiver.getUsername(), amount);
                sender.setLastTransaction(transaction);
                receiver.setLastTransaction(transaction);
                writeBankAccountsToFile();
                flagCheckFalse(sender, receiver);
                return true;
            }
        }*/

    //TODO: Non più utilizzati nell'ultima versione
    public synchronized boolean flagCheckTrue(BankAccount sender, BankAccount receiver){ //Controllo delle flag e set a true
        if (sender.getTransactionFlag() == false && receiver.getTransactionFlag() == false) {
            sender.setTransactionFlag(true);
            receiver.setTransactionFlag(true);
            return true;
        }else {
            return false;
        }
    }

    public synchronized boolean flagCheckFalse(BankAccount sender, BankAccount receiver){ //Controllo delle flag e set a false
        if (sender.getTransactionFlag() == true && receiver.getTransactionFlag() == true) {
            sender.setTransactionFlag(false);
            receiver.setTransactionFlag(false);
            return true;
        }else {
            return false;
        }
    }

}
