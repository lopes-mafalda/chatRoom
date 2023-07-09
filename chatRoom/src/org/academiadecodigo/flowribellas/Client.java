package org.academiadecodigo.flowribellas;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private final Socket clientSocket = new Socket("192.168.2.228",8000);
    private final BufferedReader inSystem = new BufferedReader(new InputStreamReader(System.in));
    private final String clientName;
    private final Color textColor;
    private final ExecutorService sendPool = Executors.newSingleThreadExecutor();
    private final ExecutorService receivePool = Executors.newSingleThreadExecutor();


    public Client() throws IOException {
        System.out.println("userName? ");
        this.clientName = inSystem.readLine();

        System.out.println("color? ");
        this.textColor = getColorFromName(inSystem.readLine());

        this.sendPool.submit(new Send(inSystem,new PrintWriter(clientSocket.getOutputStream(), true) ));
        this.receivePool.submit(new Receive(new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))));
    }


    private Color getColorFromName(String colorName) {
        switch (colorName.toUpperCase()) {
            case "BLACK":
                return Color.BLACK;
            case "RED":
                return Color.RED;
            case "GREEN":
                return Color.GREEN;
            case "YELLOW":
                return Color.YELLOW;
            case "BLUE":
                return Color.BLUE;
            case "MAGENTA":
                return Color.MAGENTA;
            case "CYAN":
                return Color.CYAN;
            case "WHITE":
                return Color.WHITE;
            default:
                return Color.WHITE;
        }
    }



    private class Send implements Runnable {

        private final BufferedReader inSystem;
        private final PrintWriter outServer;

        public Send(BufferedReader inSystem, PrintWriter outServer) {
            this.inSystem = inSystem;
            this.outServer = outServer;
        }

        @Override
        public void run() {

            while (true) {
                try {
                    String line = inSystem.readLine();

                    String message = clientName + ": " + line;

                    String colorCode = "\u001B[38;2;" + textColor.getRed() + ";" + textColor.getGreen() + ";" + textColor.getBlue() + "m";
                    outServer.println(colorCode + message + "\u001B[0m"); // Reset color after the message

                } catch (IOException e) {
                    System.out.println("error sending");
                }
            }
        }

        private boolean isQuiting(String line) {
            return line.equals("/quit") ? true : false;
        }
    }

    private class Receive implements Runnable {

        private final BufferedReader inServer;
        public Receive(BufferedReader inServer) {
            this.inServer = inServer;
        }

        @Override
        public void run() {

            while (true) {

                try {
                    String line = inServer.readLine();

                    if (!line.equals(null)) {
                        System.out.println(line); //print it to my console
                    } else {
                        inServer.close();
                    }

                } catch (IOException e) {
                    System.out.println("receiving");
                }

            }
        }
    }


    public static void main(String[] args) {

        Client client = null;
        try {
            client = new Client();
        } catch (IOException e) {
            System.out.println("error creating client: " + e.getMessage());
        }
    }
}
