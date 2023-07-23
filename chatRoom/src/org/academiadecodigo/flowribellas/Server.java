package org.academiadecodigo.flowribellas;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    //server port used for the client to connect - change if an error of connection occurs
    //if you change in the client, you also have to change it here!!
    public static final int SERVER_PORT = 8080;
    //mimics a client inside the server
    private Vector<Worker> workerList;
    //used to connect to our clients
    private ServerSocket serverSocket;
    //used to keep count of the workers
    private int counter = 0;

    /**
     * constructor for the server
     * sets the workerList and the server socket
     */
    public Server() {

        this.workerList = new Vector<>();

        try {
            this.serverSocket = new ServerSocket(SERVER_PORT);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * method used to connect the server to the workers and consequently the clients
     * also adds workers to the list
     * @throws IOException
     */
    public void connect() throws IOException {

        ExecutorService workerPool = Executors.newCachedThreadPool();

        //adding and counting new workers
        while (serverSocket.isBound()) {

            Socket workerSocket = this.serverSocket.accept();

            Worker worker = new Worker(workerSocket);
            worker.setWorkerId(counter++);
            workerPool.submit(worker);

            this.workerList.add(worker);

            System.out.println("added new person to chat");

        }

    }

    /**
     * method used to broadcast the message to all workers
     * @param message - message that is broadcast
     * @param clientId - sender's id (to avoid echoes)
     * @throws IOException
     */
    public void broadcast(String message, int clientId) throws IOException {

        for (Worker myBeautifulSlave : workerList) {
            if (!(clientId == (myBeautifulSlave.workerId))) {

                myBeautifulSlave.send(message);
            }
        }
    }

    //Worker - class used to mimic the client inside the server
    //implements Runnable interface in order to use the CachedThreadPool in the connect() of the Server
    public class Worker implements Runnable {
        //output
        private final PrintWriter out;
        //input
        private final BufferedReader in;
        //id - used to identify the worker
        private int workerId;

        /**
         * worker's constructor
         * @param workerSocket - receives an accepted socket
         */
        public Worker(Socket workerSocket) {

            try {
                this.out = new PrintWriter(workerSocket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * method used to set the worker id that identifies it
         * @param workerId
         */
        public void setWorkerId(int workerId) {
            this.workerId = workerId;
        }

        @Override
        public void run() {
            while (true) {
                try {

                    String message = in.readLine();

                    if (message != null) {
                        System.out.println(message);
                        broadcast(message, workerId);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * method used to send out a message to the server
         * @param message - message that is sent
         */
        private void send(String message) {

            try {
                out.println(message);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {

        final Server server = new Server();

        try {
            server.connect();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



