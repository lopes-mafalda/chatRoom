package org.academiadecodigo.flowribellas;
import java.awt.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static final int SERVER_PORT = 8000;
    private Vector<Worker> workerList;
    private ServerSocket serverSocket;
    private int counter = 0;


    public Server() throws IOException {
        this.workerList = new Vector<>();
        this.serverSocket = new ServerSocket(SERVER_PORT);
    }

    public void connect() throws IOException {
        ExecutorService workerPool = Executors.newCachedThreadPool();

        //adding new workers
        while (serverSocket.isBound()) {
            Socket workerSocket = this.serverSocket.accept();

            Worker worker = new Worker(workerSocket);
            worker.setClientId(counter++);

            workerPool.submit(worker);

            this.workerList.add(worker);

            System.out.println("added new person to chat");

        }

    }

    public void broadcast(String message, int clientId) throws IOException {
        for(Worker myBeautifulSlave : workerList) {
            if(!(clientId == (myBeautifulSlave.clientId))) {

                myBeautifulSlave.send(message);

            }
        }
    }

    public static void main(String[] args) {

        try {
            final Server server = new Server();
            server.connect();

        } catch (IOException e) {
            System.out.println("error creating server");
        }


    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    public class Worker implements Runnable {
        private final PrintWriter out;
        private final BufferedReader in;
        private int clientId;
        //private final Color textColor;

        public Worker(Socket workerSocket) throws IOException {

            this.out = new PrintWriter(workerSocket.getOutputStream(),true);
            this.in = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));

            //this.out.println("name? ");
            //this.clientName = in.readLine();

            //this.out.println("color? ");
            //this.textColor = getColorFromName(in.readLine());

        }

        public void setClientId(int clientId) {
            this.clientId = clientId;
        }

        @Override
        public void run() {
            while (true) {
                try {

                    String message = in.readLine();

                    if (message != null) {
                        System.out.println(message);
                        broadcast(message, clientId);
                    }
                } catch(IOException e){
                    throw new RuntimeException(e);

                }
            }
        }
        private void send(String message) {

            try {
                out.println(message); // Reset color after the message

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
    }
}



