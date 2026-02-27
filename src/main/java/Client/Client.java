package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Client <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try {
            Socket s = new Socket(host, port);
            System.out.println("Connected to server");

            //Delega la gestione di input/output a due thread separati, uno per inviare messaggi e uno per leggerli
            new Thread(() -> sender(s)).start();
            new Thread(() -> receiver(s)).start();

        } catch (IOException e) {
            System.out.println("Connection refused: server not existing.");
        }
    }

    public static void sender(Socket s){
        Scanner userInput = new Scanner(System.in);
        PrintWriter to = null;

        try {
            to = new PrintWriter(s.getOutputStream(), true);
            while (true) {
                String request = userInput.nextLine();
                if (request.equals("quit")) {
                    break;
                }
                to.println(request);
            }
            System.out.println("Sender closed.");
        } catch (IOException e) {
            // Handle exception
        } finally {
            if (to != null) {
                to.close();
            }
            userInput.close();
        }
    }


    public static void receiver(Socket s){
        try {
            Scanner from = new Scanner(s.getInputStream());
            while (true) {
                String response = from.nextLine();
                System.out.println("Received: " + response);
                if (response.equals("quit")) {
                    System.exit(0);
                    return;
                }

            }
        } catch (IOException e) {
            //System.err.println("IOException caught: " + e);

        } catch (NoSuchElementException e){

        } finally {
            System.out.println("Receiver closed.");

        }
    }
}