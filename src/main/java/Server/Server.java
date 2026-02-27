package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

import static java.lang.Integer.parseInt;

public class Server {
    private static int PORT_NUMBER;
    private static List<Socket> clientSockets = Collections.synchronizedList(new ArrayList<>());
    private static Bank bank;

    public static void main(String[] args){
        try {
            // Verifica l'esattezza dell' args inserito
            if (args.length == 1) {
                PORT_NUMBER = parseInt(args[0]);
                System.out.println("Door number: " + PORT_NUMBER);
            } else {
                System.out.println("Usage: java Server <port>");
                return;
            }

        } catch (NumberFormatException e) {
            System.err.println("Error: Argument is not a valid integer.\nUsage: java Server <port>");
        }

        bank = new Bank();

        new Thread(() -> keyboardInputListener()).start();

        try{
            //Dichiara il ServerSocket come nella classe Server originale
            ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
            System.out.println("Server listening on the port " + PORT_NUMBER);

            while (true) { //Sostituisce la classe SocketListener
                Socket socket = serverSocket.accept();
                clientSockets.add(socket);
                System.out.println("New client connected");

                // Per ogni nuova connessione, avvia un nuovo thread
                new Thread(() -> clientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error: ServerSocket deformed.");
            throw new RuntimeException(e);
        }

    }

    //Ascolta l'input da tastiera e spegne il server al comando quit
    private static void keyboardInputListener() {
        Scanner keyboardScanner = new Scanner(System.in);
        while (true) {
            String input = keyboardScanner.nextLine();
            if ("quit".equalsIgnoreCase(input)) {
                for (Socket clientSocket : clientSockets) {
                    try {
                        PrintWriter to = new PrintWriter(clientSocket.getOutputStream(), true);
                        to.println("Server shutting down...");
                        to.println("quit");
                        clientSocket.close();
                    } catch (IOException e) {
                        // Handle exception
                    }
                }
                System.exit(0); // or set a flag to shut down the server loop
            }
        }
    }


    //Sostituisce la classe ClientListener
    private static void clientHandler(Socket socket) {
        boolean runningFlag = true;
        Scanner from = null;
        PrintWriter to = null;


        try {
            from = new Scanner(socket.getInputStream());
            to = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Thread " + Thread.currentThread() + " listening ...");
            String menu = "AVAILABLE CONTROLS: \n" + "- open <account> <balance>\n" + "- list \n" + "- transfer <sender> <receiver> <amount>\n" + "- transfer_i <sender> <receiver>\n" + "- info <control>\n" + "- quit";
            to.println(menu);
            //Ciclo while che rimane in loop finchè la connessione del socket non cade oppure il server viene spento
            while (from.hasNextLine() && runningFlag) {
                try {
                    String request = from.nextLine();
                    System.out.println("Request: " + request);
                    String[] parts = request.split(" ");
                    switch (parts[0]) {
                        case "quit":
                            runningFlag = false;
                            System.out.println("Thread " + Thread.currentThread() + " shutting down...");
                            break;
                        case "info":
                            if (parts[1].equals("open")) {
                                to.println("Open a new account.");
                            } else if (parts[1].equals("transfer")) {
                                to.println("Transfer an amount from an account to another one.");
                            } else if (parts[1].equals("transfer_i")) {
                                to.println("Transfer an amount from an account to another one, by creating an interactive session.");
                            } else if (parts[1].equals("list")) {
                                to.println("Print opened accounts list.");
                            } else if (parts[1].equals("all")) {
                                to.println("<open> Open a new account. \n<transfer> Transfer an amount from an account to another one. \n<transfer_i> Transfer an amount from an account to another one, by creating an interactive session." +
                                        "\n<list> Print opened accounts list. \n<quit> Off the connection with server.");
                            } else if (parts[1].equals("quit")) {
                                to.println("Off the connection with server.");
                            }else{
                                to.println("Unknown cmd");
                            }
                            break;
                        case "open":
                            try {
                                if (parts.length == 3) {
                                    to.println("Open command recognized with account: " + parts[1] + " and balance: " + parts[2]);
                                    String result = bank.addBankAccount(new BankAccount(parts[1], Integer.parseInt(parts[2])));
                                    to.println(result);
                                } else {
                                    to.println("Usage: Open <Account> <Amount>");
                                }
                            } catch (NumberFormatException e) {
                                to.println("Amount requires a numeric value");
                            }
                            break;
                        case "list":
                            to.println(bank.getAccountList());
                            break;
                        case "transfer":
                            try {
                                if (parts.length == 4) {
                                    BankAccount sender = bank.findAccountByUsername(parts[1]);
                                    BankAccount receiver = bank.findAccountByUsername(parts[2]);
                                    if (sender == null || receiver == null) {
                                        throw new RuntimeException("Impossible to proceed: account or accounts may not be existing");
                                    }
                                    if (bank.transferAmount(sender, receiver, Integer.parseInt(parts[3])))
                                        to.println("Transaction completed correctly");
                                } else {
                                    to.println("Usage: Transfer <sender> <receiver> <amount>");
                                }
                            } catch (NumberFormatException e) {
                                to.println("Amount requires a numeric value");
                            } catch (RuntimeException e) {
                                to.println(e.getMessage());
                            }
                            break;
                        case "transfer_i":
                            Lock senderLock = null;
                            Lock receiverLock = null;
                            if (parts.length == 3) {
                                BankAccount sender = bank.findAccountByUsername(parts[1]);
                                BankAccount receiver = bank.findAccountByUsername(parts[2]);
                                if (sender == null || receiver == null)
                                    throw new RuntimeException("Impossible to proceed: account or accounts may not be existing");

                                try {
                                    senderLock = sender.getLock();
                                    receiverLock = receiver.getLock();

                                    senderLock.lock();
                                    receiverLock.lock();

                                    do {
                                        to.println("AVAILABLE CONTROLS: \n" + "- :move <Amount>\n" + "- :end");
                                        request = from.nextLine();
                                        parts = request.split(" ");  //sovrascrive il comando iniziale perchè ormai inutile
                                        try {
                                            switch (parts[0]) {
                                                case ":move":
                                                    if (bank.transfer_iAmount(sender, receiver, parts[1])) {
                                                        to.println("Transaction completed correctly");
                                                    } else {
                                                        to.println("Impossible to proceed: invalid amount");
                                                    }
                                                    break;
                                                case ":end":
                                                    to.println("Session ended");
                                                    break;
                                                default:
                                                    to.println("Unknown cmd");
                                                    break;
                                            }

                                        } catch (RuntimeException e) {
                                            to.println(e.getMessage());
                                        }

                                    } while (!parts[0].equals(":end"));
                                } finally {
                                    senderLock.unlock();
                                    receiverLock.unlock();
                                }
                            } else {
                                to.println("Usage: Transfer_i <sender> <receiver>");
                            }
                            break;
                        default:
                            to.println("Unknown control");
                    }
                } catch (RuntimeException e){
                    to.println(e.getMessage());
                }
            }
            to.println("Server disconnected...");
            from.close();
            to.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
