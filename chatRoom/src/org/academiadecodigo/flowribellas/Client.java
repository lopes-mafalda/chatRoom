package org.academiadecodigo.flowribellas;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Client that connects to the Server
public class Client {

    //server port used for the client to connect - change if an error of connection occurs
    //if you change in the server, you also have to change it here!!
    private final int SERVER_PORT = 8080;
    //server's ip address - change to your server's ip address to test
    private final String HOST = "192.168.2.228";
    //client's socket used to make the connection
    private final Socket clientSocket = new Socket(HOST, SERVER_PORT);
    //client's name
    private final String clientName;
    //client's chosen color
    private final Color clientColor;


    /**
     * Client's constructor - used to map the client's name and chosen color and to submit both the thread pools
     * @throws IOException
     */
    public Client() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("userName? ");
        this.clientName = reader.readLine();

        System.out.println("color? ");
        this.clientColor = getColorFromName(reader.readLine());

        poolStart(reader);
    }

    /**
     * Method to start both necessary pools - to send and to receive so they can be both done concurrently
     * @param reader - reader of the input of the client
     * @throws IOException
     */
    private void poolStart(BufferedReader reader) throws IOException {

        ExecutorService sendPool = Executors.newSingleThreadExecutor();
        ExecutorService receivePool = Executors.newSingleThreadExecutor();

        sendPool.submit(new Send(reader, new PrintWriter(clientSocket.getOutputStream(), true) ));
        receivePool.submit(new Receive(new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))));

    }

    /**
     * method to discover transform String into Color
     * @param colorName - color input
     * @return - the corresponding color, if there is one or the default
     */
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

    //Nested class that represents the thread pool that sends messages
    private class Send implements Runnable {

        //input
        private final BufferedReader reader;
        //output
        private final PrintWriter writer;
        //constant used in the color selection
        private final String START_COLOR = "\u001B[38;2;";
        //constant used in the color selection
        private final String RESET_COLOR = "\u001B[0m";

        /**
         * Send's constructor - used to set both the reader and the writer
         * @param reader
         * @param writer
         */
        private Send(BufferedReader reader, PrintWriter writer) {
            this.reader = reader;
            this.writer = writer;
        }

        /**
         * used to transform the Color into code the terminal understands
         * @return the string of corresponding color
         */
        private String mapColor() {
            StringBuilder builder = new StringBuilder();
            builder.append(START_COLOR)
                    .append(clientColor.getRed())
                    .append(";")
                    .append(clientColor.getGreen())
                    .append(";")
                    .append(clientColor.getBlue())
                    .append("m");
            return builder.toString();
        }

        /**
         * method used to send messages with all the relevant information
         */
        @Override
        public void run() {

            while (true) {
                try {
                    String line = reader.readLine(); //reads line by line what the client writes

                    StringBuilder builder = new StringBuilder();

                    builder.append(mapColor()) //add color
                            .append(clientName) //add name
                            .append(": ") //separation
                            .append(line) //message
                            .append(RESET_COLOR); //resets color

                    String message = builder.toString();
                    writer.println(message); //output of the message

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        private boolean isQuiting(String line) {
            return line.equals("/quit") ? true : false;
        }
    }

    //Nested class that represents the thread pool that receives messages
    private class Receive implements Runnable {

        //input
        private final BufferedReader reader;

        /**
         * Receive's constructor
         * @param reader - reader used in the reading of the output of the server
         */
        public Receive(BufferedReader reader) {
            this.reader = reader;
        }

        /**
         * method used to receive messages
         */
        @Override
        public void run() {

            while (true) {

                try {
                    String line = reader.readLine();

                    //used to close all connections if server closes
                    if (line == null) {
                        this.reader.close();
                        clientSocket.close();
                        System.exit(0);
                        return;
                    }
                    System.out.println(line); //print the message

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }


    public static void main(String[] args) {

        try {
            Client client = new Client();

        } catch (IOException e) {
            System.out.println("error creating client: " + e.getMessage());
        }
    }
}
